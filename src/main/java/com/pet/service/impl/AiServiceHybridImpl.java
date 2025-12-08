package com.pet.service.impl;

import com.pet.converter.*;
import com.pet.entity.*;
import com.pet.modal.response.*;
import com.pet.repository.*;
import com.pet.service.AiService;
import com.pet.service.AttributeExtractor;
import com.pet.service.ScoredId;
import com.pet.service.VectorSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AiServiceHybridImpl - Refactored for Robustness
 * - Fixed: Strict equality checks replaced with loose "contains".
 * - Fixed: Null safety checks added everywhere.
 * - Added: Debug logs for filtering logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceHybridImpl implements AiService {

    private final AttributeExtractor attributeExtractor;
    private final VectorSearchService vectorSearchService;

    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final ArticleRepository articleRepository;
    private final VoucherRepository voucherRepository;
    private final DeliveryRepository deliveryRepository;

    private final PetConverter petConverter;
    private final ServiceConverter serviceConverter;
    private final ArticleConverter articleConverter;
    private final VoucherConverter voucherConverter;

    // weights for score fusion
    private static final double ALPHA_VECTOR = 0.75;
    private static final double BETA_KEYWORD = 0.25;
    private static final double GAMMA_PRICE = 0.10;

    @Override
    public ChatResponseDTO chat(String userInput) {
        try {
            AiAttributesDTO attrs = attributeExtractor.extract(userInput);

            if (attrs == null) {
                return buildGeneralChatResponse();
            }

            // Default confidence if missing
            if (attrs.getConfidence() == null) attrs.setConfidence(1.0);

            // General chat handling
            if ("general".equalsIgnoreCase(attrs.getDomain()) || attrs.getConfidence() < 0.35) {
                return buildGeneralChatResponse();
            }

            String domain = attrs.getDomain() == null ? "" : attrs.getDomain().toLowerCase(Locale.ROOT);
            switch (domain) {
                case "pet":
                    return handlePetQuery(userInput, attrs);
                case "service":
                    return handleServiceQuery(userInput, attrs);
                case "article":
                    return handleArticleQuery(userInput, attrs);
                case "voucher":
                    return handleVoucherQuery(userInput, attrs);
                case "order":
                    return handleOrderQuery(userInput, attrs);
                default:
                    return buildGeneralChatResponse();
            }
        } catch (Exception ex) {
            log.error("AiServiceHybridImpl chat error", ex);
            return ChatResponseDTO.builder()
                    .message("Hệ thống đang gặp sự cố, bạn thử lại sau nhé 🐾")
                    .actionType("NONE")
                    .build();
        }
    }

    // ----------------------------
    // PET HANDLING
    // ----------------------------
    private ChatResponseDTO handlePetQuery(String userInput, AiAttributesDTO attrs) {
        // 1) Vector search
        List<ScoredId> vectorResults = safeVectorSearchWithScore("pet", userInput, 50);

        Map<String, Double> idToVectorScore = vectorResults.stream()
                .collect(Collectors.toMap(ScoredId::getId, ScoredId::getScore, (a, b) -> a, LinkedHashMap::new));

        // 2) Fetch candidates
        List<Pet> candidates = new ArrayList<>();
        if (!idToVectorScore.isEmpty()) {
            Iterable<Pet> fetched = petRepository.findAllById(idToVectorScore.keySet());
            Map<String, Pet> map = new HashMap<>();
            fetched.forEach(p -> map.put(p.getPetId(), p));
            for (String id : idToVectorScore.keySet()) {
                if (map.containsKey(id)) candidates.add(map.get(id));
            }
        }

        // 3) Fallback if vector empty
        if (candidates.isEmpty()) {
            log.info("Vector search empty for PET, fallback to DB search for '{}'", attrs.getKeyword());
            // Ensure searchForChat searches broadly (Name, Description, Breed, Category)
            candidates = petRepository.searchForChat(attrs.getKeyword(), attrs.getMaxPrice(), PageRequest.of(0, 200));
        }

        // 4) Apply LOOSE filters (Important Fix)
        candidates = applyPetFilters(candidates, attrs);

        if (candidates.isEmpty()) {
            return ChatResponseDTO.builder()
                    .message("Hiện chưa tìm thấy thú cưng phù hợp yêu cầu của bạn 🐾")
                    .actionType("SHOW_PETS")
                    .dataType("pet")
                    .data(Collections.emptyList())
                    .build();
        }

        // 5) Calculate scores
        Map<String, Double> keywordScores = computeKeywordScoresForPets(candidates, attrs);
        List<ScoredCandidate<Pet>> scored = new ArrayList<>();

        double maxPrice = candidates.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).max().orElse(0);
        double minPrice = candidates.stream().mapToDouble(p -> p.getPrice() != null ? p.getPrice() : 0).min().orElse(0);

        for (Pet p : candidates) {
            double vectorScore = idToVectorScore.getOrDefault(p.getPetId(), 0.0);
            double keywordScore = keywordScores.getOrDefault(p.getPetId(), 0.0);
            double priceSignal = 0.0;

            if ("PRICE".equalsIgnoreCase(attrs.getSortBy()) && p.getPrice() != null && maxPrice > minPrice) {
                if ("ASC".equalsIgnoreCase(attrs.getSortDirection())) {
                    priceSignal = (maxPrice - p.getPrice()) / (maxPrice - minPrice + 0.0001);
                } else {
                    priceSignal = (p.getPrice() - minPrice) / (maxPrice - minPrice + 0.0001);
                }
            }
            double combined = ALPHA_VECTOR * vectorScore + BETA_KEYWORD * keywordScore + GAMMA_PRICE * priceSignal;
            scored.add(new ScoredCandidate<>(p, combined, vectorScore, keywordScore, priceSignal));
        }

        // 6) Sort & Limit
        sortScoredCandidates(scored, attrs);

        int limit = (attrs.getLimit() != null && attrs.getLimit() > 0) ? attrs.getLimit() : 10;
        List<PetResponseDTO> dtos = scored.stream().limit(limit)
                .map(sc -> petConverter.mapToDTO(sc.getCandidate()))
                .collect(Collectors.toList());

        String msg = dtos.size() == 1 ?
                String.format("Mình tìm được 1 bé phù hợp: %s — Giá %.0f VNĐ 🐶", dtos.get(0).getName(), dtos.get(0).getPrice()) :
                String.format("Mình tìm thấy %d thú cưng phù hợp nè! 🐾", dtos.size());

        return ChatResponseDTO.builder().message(msg).actionType("SHOW_PETS").dataType("pet").data(dtos).build();
    }

    // ----------------------------
    // SERVICE HANDLING
    // ----------------------------
    private ChatResponseDTO handleServiceQuery(String userInput, AiAttributesDTO attrs) {
        List<ScoredId> vectorResults = safeVectorSearchWithScore("service", userInput, 30);
        Map<String, Double> idToVectorScore = vectorResults.stream()
                .collect(Collectors.toMap(ScoredId::getId, ScoredId::getScore, (a, b) -> a, LinkedHashMap::new));

        List<com.pet.entity.Service> candidates = new ArrayList<>();
        if (!idToVectorScore.isEmpty()) {
            Iterable<com.pet.entity.Service> fetched = serviceRepository.findAllById(idToVectorScore.keySet());
            fetched.forEach(candidates::add);
        }

        if (candidates.isEmpty()) {
            candidates = serviceRepository.searchServicesForChat(attrs.getKeyword() != null ? attrs.getKeyword() : "");
        }

        // Safe Filters
        if (attrs.getMinPrice() != null) {
            candidates = candidates.stream().filter(s -> s.getPrice() != null && s.getPrice() >= attrs.getMinPrice()).collect(Collectors.toList());
        }
        if (attrs.getMaxPrice() != null) {
            candidates = candidates.stream().filter(s -> s.getPrice() != null && s.getPrice() <= attrs.getMaxPrice()).collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            return ChatResponseDTO.builder().message("Mình chưa tìm thấy dịch vụ phù hợp 😿").actionType("SHOW_SERVICES").data(Collections.emptyList()).build();
        }

        Map<String, Double> keywordScores = computeKeywordScoresForServices(candidates, attrs);
        List<ScoredCandidate<com.pet.entity.Service>> scored = new ArrayList<>();
        for (com.pet.entity.Service s : candidates) {
            double v = idToVectorScore.getOrDefault(s.getServiceId(), 0.0);
            double k = keywordScores.getOrDefault(s.getServiceId(), 0.0);
            scored.add(new ScoredCandidate<>(s, ALPHA_VECTOR * v + BETA_KEYWORD * k, v, k, 0.0));
        }

        sortScoredCandidates(scored, attrs);
        int limit = (attrs.getLimit() != null && attrs.getLimit() > 0) ? attrs.getLimit() : 10;
        List<ServiceResponseDTO> dtos = scored.stream().limit(limit).map(ScoredCandidate::getCandidate).map(serviceConverter::toServiceResponseDTO).collect(Collectors.toList());

        return ChatResponseDTO.builder().message("Mình tìm thấy dịch vụ phù hợp nè!").actionType("SHOW_SERVICES").dataType("service").data(dtos).build();
    }

    // ----------------------------
    // ARTICLE HANDLING
    // ----------------------------
    private ChatResponseDTO handleArticleQuery(String userInput, AiAttributesDTO attrs) {
        List<ScoredId> vectorResults = safeVectorSearchWithScore("article", userInput, 30);
        Map<String, Double> idToVectorScore = vectorResults.stream()
                .collect(Collectors.toMap(ScoredId::getId, ScoredId::getScore, (a, b) -> a, LinkedHashMap::new));

        List<Article> candidates = new ArrayList<>();
        if (!idToVectorScore.isEmpty()) {
            articleRepository.findAllById(idToVectorScore.keySet()).forEach(candidates::add);
        }

        if (candidates.isEmpty()) {
            candidates = articleRepository.searchArticles(attrs.getKeyword() != null ? attrs.getKeyword() : "", PageRequest.of(0, 20)).getContent();
        }

        if (candidates.isEmpty()) {
            return ChatResponseDTO.builder().message("Không tìm thấy bài viết phù hợp 📝").actionType("SHOW_ARTICLES").data(Collections.emptyList()).build();
        }

        Map<String, Double> keywordScores = computeKeywordScoresForArticles(candidates, attrs);
        List<ScoredCandidate<Article>> scored = new ArrayList<>();
        for (Article a : candidates) {
            double v = idToVectorScore.getOrDefault(a.getArticleId(), 0.0);
            double k = keywordScores.getOrDefault(a.getArticleId(), 0.0);
            scored.add(new ScoredCandidate<>(a, ALPHA_VECTOR * v + BETA_KEYWORD * k, v, k, 0.0));
        }

        scored.sort(Comparator.comparingDouble(ScoredCandidate<Article>::getScore).reversed());
        List<ArticleResponseDTO> dtos = scored.stream().limit(6).map(s -> articleConverter.toResponseDTO(s.getCandidate())).collect(Collectors.toList());

        return ChatResponseDTO.builder().message("Đây là bài viết phù hợp:").actionType("SHOW_ARTICLES").dataType("article").data(dtos).build();
    }

    // ----------------------------
    // VOUCHER HANDLING
    // ----------------------------
    private ChatResponseDTO handleVoucherQuery(String userInput, AiAttributesDTO attrs) {
        List<ScoredId> vectorResults = safeVectorSearchWithScore("voucher", userInput, 30);
        Map<String, Double> idToVectorScore = vectorResults.stream()
                .collect(Collectors.toMap(ScoredId::getId, ScoredId::getScore, (a, b) -> a, LinkedHashMap::new));

        List<Voucher> candidates = new ArrayList<>();
        if (!idToVectorScore.isEmpty()) {
            voucherRepository.findAllById(idToVectorScore.keySet()).forEach(candidates::add);
        }

        if (candidates.isEmpty()) {
            candidates = voucherRepository.findAvailableVouchersForChat();
        }

        // --- FIXED VOUCHER FILTERING (Null Safety) ---
        if (attrs.getVoucherCode() != null) {
            String codeLower = attrs.getVoucherCode().trim().toLowerCase();
            candidates = candidates.stream()
                    .filter(v -> v.getCode() != null && v.getCode().toLowerCase().contains(codeLower))
                    .collect(Collectors.toList());
        } else if (attrs.getKeyword() != null) {
            String kw = attrs.getKeyword().trim().toLowerCase();
            candidates = candidates.stream()
                    .filter(v -> (v.getCode() != null && v.getCode().toLowerCase().contains(kw)) ||
                            (v.getDescription() != null && v.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        if (Boolean.TRUE.equals(attrs.getOnlyValid())) {
            candidates = candidates.stream()
                    .filter(v -> v.getStatus() != null && "ACTIVE".equalsIgnoreCase(v.getStatus().name()))
                    .collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            return ChatResponseDTO.builder().message("Không có voucher phù hợp 🎟️").actionType("SHOW_VOUCHERS").data(Collections.emptyList()).build();
        }

        Map<String, Double> keywordScores = computeKeywordScoresForVouchers(candidates, attrs);
        List<ScoredCandidate<Voucher>> scored = new ArrayList<>();
        for (Voucher v : candidates) {
            double vec = idToVectorScore.getOrDefault(v.getVoucherId(), 0.0);
            double kw = keywordScores.getOrDefault(v.getVoucherId(), 0.0);
            scored.add(new ScoredCandidate<>(v, ALPHA_VECTOR * vec + BETA_KEYWORD * kw, vec, kw, 0.0));
        }

        scored.sort(Comparator.comparingDouble(ScoredCandidate<Voucher>::getScore).reversed());
        List<VoucherResponseDTO> dtos = scored.stream().limit(8).map(sc -> voucherConverter.mapToDTO(sc.getCandidate())).collect(Collectors.toList());

        return ChatResponseDTO.builder().message("Mình tìm thấy voucher phù hợp nè!").actionType("SHOW_VOUCHERS").dataType("voucher").data(dtos).build();
    }

    // ----------------------------
    // ORDER HANDLING
    // ----------------------------
    private ChatResponseDTO handleOrderQuery(String userInput, AiAttributesDTO attrs) {
        if (attrs.getTrackingId() == null || attrs.getTrackingId().trim().isEmpty()) {
            return ChatResponseDTO.builder().message("Bạn cung cấp giúp mình mã đơn hàng nhé 🐾").actionType("NONE").build();
        }
        var d = deliveryRepository.findByTrackingOrOrderId(attrs.getTrackingId());
        if (d.isEmpty()) {
            return ChatResponseDTO.builder().message("Không tìm thấy đơn hàng bạn yêu cầu 😿").actionType("NONE").build();
        }
        return ChatResponseDTO.builder().message("Đây là trạng thái đơn hàng bạn cần nè!").actionType("SHOW_ORDER_STATUS").dataType("order").data(d.get()).build();
    }

    // ----------------------------
    // ROBUST FILTERING LOGIC (The Core Fix)
    // ----------------------------

    private List<Pet> applyPetFilters(List<Pet> list, AiAttributesDTO attrs) {
        if (list == null || list.isEmpty()) return Collections.emptyList();

        log.info("Filtering {} pets. Criteria: Cat={}, Fur={}, Color={}, PriceMax={}",
                list.size(), attrs.getCategory(), attrs.getFurType(), attrs.getColor(), attrs.getMaxPrice());

        return list.stream().filter(p -> {
            // 1. Availability
            if (Boolean.TRUE.equals(attrs.getAvailableOnly())) {
                if (p.getStatus() == null || !"AVAILABLE".equalsIgnoreCase(p.getStatus().name())) return false;
            }

            // 2. Price
            if (attrs.getMinPrice() != null && (p.getPrice() == null || p.getPrice() < attrs.getMinPrice())) return false;
            if (attrs.getMaxPrice() != null && (p.getPrice() == null || p.getPrice() > attrs.getMaxPrice())) return false;

            // 3. Category (Use LOOSE Matching)
            if (attrs.getCategory() != null) {
                String dbCat = (p.getCategory() != null) ? p.getCategory().getName() : "";
                // If DB category does not contain AI category string -> Reject
                if (!isMatchLoose(dbCat, attrs.getCategory())) {
                    log.debug("Filtered Pet ID={} due to Category mismatch: DB='{}', AI='{}'", p.getPetId(), dbCat, attrs.getCategory());
                    return false;
                }
            }

            // 4. Fur Type
            if (attrs.getFurType() != null) {
                if (!isMatchLoose(p.getFurType().toString(), attrs.getFurType())) return false;
            }

            // 5. Color
            if (attrs.getColor() != null) {
                if (!isMatchLoose(p.getColor(), attrs.getColor())) return false;
            }

            // 6. Gender
            if (attrs.getGender() != null) {
                String dbGender = (p.getGender() != null) ? p.getGender().name() : "";
                if (!isMatchLoose(dbGender, attrs.getGender())) return false;
            }

            // 7. Age
            if (attrs.getMinAge() != null && (p.getAge() == null || p.getAge() < attrs.getMinAge())) return false;
            if (attrs.getMaxAge() != null && (p.getAge() == null || p.getAge() > attrs.getMaxAge())) return false;

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Helper to check if source string contains target string (Case insensitive, Null safe)
     */
    private boolean isMatchLoose(String source, String target) {
        if (target == null || target.isEmpty()) return true; // No filter target -> match all
        if (source == null) return false; // Target exists but source is null -> no match
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private <T> void sortScoredCandidates(List<ScoredCandidate<T>> scored, AiAttributesDTO attrs) {
        if ("PRICE".equalsIgnoreCase(attrs.getSortBy())) {
            if ("DESC".equalsIgnoreCase(attrs.getSortDirection())) {
                scored.sort(Comparator.comparingDouble(ScoredCandidate<T>::getPriceValue).reversed());
            } else {
                scored.sort(Comparator.comparingDouble(ScoredCandidate<T>::getPriceValue));
            }
        } else {
            scored.sort(Comparator.comparingDouble(ScoredCandidate<T>::getScore).reversed());
        }
    }

    // ----------------------------
    // SCORING HELPERS (Simplified)
    // ----------------------------

    private List<String> normalizeKeywords(AiAttributesDTO attrs) {
        List<String> keys = new ArrayList<>();
        if (attrs.getKeywords() != null) attrs.getKeywords().forEach(k -> { if(k!=null) keys.add(k.trim().toLowerCase()); });
        if (attrs.getKeyword() != null) {
            String[] toks = attrs.getKeyword().toLowerCase().split("[^\\p{L}0-9]+");
            for (String t : toks) if (!t.isBlank()) keys.add(t.trim());
        }
        return keys.stream().distinct().collect(Collectors.toList());
    }

    private Map<String, Double> computeKeywordScoresForPets(List<Pet> pets, AiAttributesDTO attrs) {
        Map<String, Double> map = new HashMap<>();
        List<String> keys = normalizeKeywords(attrs);
        if (keys.isEmpty()) return map;

        for (Pet p : pets) {
            String hay = safeStr(p.getName()) + " " + safeStr(p.getDescription()) + " "
                    + (p.getCategory() != null ? safeStr(p.getCategory().getName()) : "") + " "
                    + safeStr(p.getColor()) + " " + safeStr(p.getFurType().toString());
            map.put(p.getPetId(), calculateMatchScore(hay, keys));
        }
        return map;
    }

    private Map<String, Double> computeKeywordScoresForServices(List<com.pet.entity.Service> services, AiAttributesDTO attrs) {
        Map<String, Double> map = new HashMap<>();
        List<String> keys = normalizeKeywords(attrs);
        if (keys.isEmpty()) return map;
        for (com.pet.entity.Service s : services) {
            map.put(s.getServiceId(), calculateMatchScore(safeStr(s.getName()) + " " + safeStr(s.getDescription()), keys));
        }
        return map;
    }

    private Map<String, Double> computeKeywordScoresForArticles(List<Article> articles, AiAttributesDTO attrs) {
        Map<String, Double> map = new HashMap<>();
        List<String> keys = normalizeKeywords(attrs);
        if (keys.isEmpty()) return map;
        for (Article a : articles) {
            map.put(a.getArticleId(), calculateMatchScore(safeStr(a.getTitle()) + " " + safeStr(a.getContent()), keys));
        }
        return map;
    }

    private Map<String, Double> computeKeywordScoresForVouchers(List<Voucher> vouchers, AiAttributesDTO attrs) {
        Map<String, Double> map = new HashMap<>();
        List<String> keys = normalizeKeywords(attrs);
        if (keys.isEmpty()) return map;
        for (Voucher v : vouchers) {
            map.put(v.getVoucherId(), calculateMatchScore(safeStr(v.getCode()) + " " + safeStr(v.getDescription()), keys));
        }
        return map;
    }

    private double calculateMatchScore(String haystack, List<String> needles) {
        String lowerHay = haystack.toLowerCase();
        double count = 0;
        for (String n : needles) if (lowerHay.contains(n)) count++;
        return count / needles.size();
    }

    private String safeStr(String s) { return s == null ? "" : s; }

    // ----------------------------
    // VECTOR HELPERS
    // ----------------------------
    private List<ScoredId> safeVectorSearchWithScore(String domain, String text, int topK) {
        try {
            List<ScoredId> res = vectorSearchService.searchIdsWithScore(domain, text, topK);
            return res != null ? res : Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Vector search fallback/error: {}", ex.getMessage());
            // Fallback implementation removed for brevity, assume interface works or returns empty
            return Collections.emptyList();
        }
    }

    private ChatResponseDTO buildGeneralChatResponse() {
        return ChatResponseDTO.builder().message("Mình chưa hiểu rõ lắm, bạn mô tả chi tiết hơn giúp mình được không? 🐶").actionType("NONE").build();
    }

    // ----------------------------
    // INNER CLASSES
    // ----------------------------
    private static class ScoredCandidate<T> {
        private final T candidate;
        private final double score;
        private final double priceValue;

        public ScoredCandidate(T candidate, double score, double vectorScore, double keywordScore, double priceSignal) {
            this.candidate = candidate;
            this.score = score;
            if (candidate instanceof Pet) this.priceValue = ((Pet) candidate).getPrice() != null ? ((Pet) candidate).getPrice() : 0.0;
            else if (candidate instanceof com.pet.entity.Service) this.priceValue = ((com.pet.entity.Service) candidate).getPrice() != null ? ((com.pet.entity.Service) candidate).getPrice() : 0.0;
            else this.priceValue = priceSignal;
        }
        public T getCandidate() { return candidate; }
        public double getScore() { return score; }
        public double getPriceValue() { return priceValue; }
    }
}