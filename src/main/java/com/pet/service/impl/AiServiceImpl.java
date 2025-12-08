package com.pet.service.impl;

import com.google.gson.Gson;
import com.pet.entity.*;
import com.pet.modal.response.AiIntentDTO;
import com.pet.modal.response.ChatResponseDTO;
import com.pet.repository.*;
import com.pet.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl  {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final ArticleRepository articleRepository;
    private final VoucherRepository voucherRepository;
    private final DeliveryRepository deliveryRepository;

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private final RestClient restClient = RestClient.create();
    private final Gson gson = new Gson();

//    @Override
//    public ChatResponseDTO chat(String userInput) {
//        try {
//            // 1. PHÂN TÍCH Ý ĐỊNH (NLU)
//            AiIntentDTO intent = analyzeIntent(userInput);
//
//            if (intent == null) {
//                intent = new AiIntentDTO("GENERAL_CHAT", null, null, null, null, null, null, null, null, null, null, null);
//            }
//
//            // 2. Nếu confidence quá thấp hoặc intent là GENERAL_CHAT -> offload to general chat
//            if ("GENERAL_CHAT".equalsIgnoreCase(intent.getIntent()) || (intent.getConfidence() != null && intent.getConfidence() < 0.45)) {
//                return handleGeneralChat(userInput);
//            }
//
//            // 3. PROCESS & GENERATE
//            return processIntentAndGenerateResponse(intent, userInput);
//
//        } catch (Exception e) {
//            log.error("AI Error", e);
//            return ChatResponseDTO.builder()
//                    .message("Hệ thống đang quá tải, bạn quay lại sau nhé 🐾")
//                    .actionType("NONE")
//                    .dataType(null)
//                    .data(null)
//                    .build();
//        }
//    }

    private ChatResponseDTO processIntentAndGenerateResponse(AiIntentDTO intent, String userQuestion) {

        StringBuilder promptContext = new StringBuilder();
        Object rawData = null;
        String actionType = "NONE";

        switch (intent.getIntent()) {

            case "SEARCH_PET":
                // Build pageable based on wantsBest / sort
                Pageable pageable = buildPetPageable(intent);

                // Prefer repository method that supports filters (assume exists). Fallback: general search and then in-memory filter.
                List<Pet> pets;
                try {
                    // Try a repo method that accepts filters (if you implement it)
                    pets = petRepository.searchForChat(intent.getKeyword(), intent.getMax_price(), pageable);
                } catch (Exception ex) {
                    // Fallback: get a broader list and filter in Java (safer)
                    pets = petRepository.findAll(); // WARNING: if DB large, replace with spec / paged query
                }

                // In-memory additional filters (furType, min_price, color, size)
                pets = applyPetFilters(pets, intent);

                // If wantsBest and list non-empty, reduce to top 1
                if (Boolean.TRUE.equals(intent.getWantsBest()) && !pets.isEmpty()) {
                    pets = pets.stream()
                            .sorted(getPetComparator(intent))
                            .limit(1)
                            .collect(Collectors.toList());
                } else {
                    // apply sort if requested
                    if (intent.getSortBy() != null) {
                        pets = pets.stream()
                                .sorted(getPetComparator(intent))
                                .collect(Collectors.toList());
                    }
                }

                if (!pets.isEmpty()) {
                    rawData = pets;
                    actionType = "SHOW_PETS";
                    pets.forEach(p ->
                            promptContext.append(String.format("- %s (%s): %.0f VNĐ\n",
                                    p.getName(), p.getCategory() != null ? p.getCategory().getName() : "N/A", p.getPrice()))
                    );
                } else {
                    promptContext.append("Không có thú cưng phù hợp.");
                }

                break;

            case "SEARCH_SERVICE":
                List<com.pet.entity.Service> services = serviceRepository.searchServicesForChat(intent.getKeyword());
                // optionally apply price filters if provided
                services = applyServiceFilters(services, intent);
                if (!services.isEmpty()) {
                    rawData = services;
                    actionType = "SHOW_SERVICES";
                    services.forEach(s ->
                            promptContext.append(String.format("- %s: %.0f VNĐ\n", s.getName(), s.getPrice()))
                    );
                } else {
                    promptContext.append("Không có dịch vụ phù hợp.");
                }
                break;

            case "SEARCH_ARTICLE":
                Page<Article> articlePage = articleRepository.searchArticles(intent.getKeyword() != null ? intent.getKeyword() : "", PageRequest.of(0, 5));
                if (articlePage.hasContent()) {
                    rawData = articlePage.getContent();
                    actionType = "SHOW_ARTICLES";
                    articlePage.forEach(a -> promptContext.append("- ").append(a.getTitle()).append("\n"));
                } else {
                    promptContext.append("Không có bài viết nào.");
                }
                break;

            case "CHECK_VOUCHER":
                List<Voucher> vouchers = voucherRepository.findAvailableVouchersForChat();
                if (!vouchers.isEmpty()) {
                    // Optionally filter by keyword
                    if (intent.getKeyword() != null) {
                        vouchers = vouchers.stream()
                                .filter(v -> v.getCode().toLowerCase().contains(intent.getKeyword().toLowerCase())
                                        || (v.getDescription() != null && v.getDescription().toLowerCase().contains(intent.getKeyword().toLowerCase())))
                                .collect(Collectors.toList());
                    }

                    rawData = vouchers;
                    actionType = "SHOW_VOUCHERS";
                    vouchers.forEach(v -> promptContext.append(String.format("- %s (Giảm %.0f)\n", v.getCode(), v.getDiscountValue())));
                } else {
                    promptContext.append("Không có voucher nào.");
                }
                break;

            case "CHECK_ORDER":
                if (intent.getTracking_id() == null || intent.getTracking_id().isEmpty()) {
                    // yêu cầu mã đơn hàng
                    promptContext.append("Khách hỏi đơn hàng nhưng thiếu mã. Hãy hỏi lại khách mã đơn hàng.");
                } else {
                    Optional<Delivery> d = deliveryRepository.findByTrackingOrOrderId(intent.getTracking_id());
                    if (d.isPresent()) {
                        rawData = d.get();
                        actionType = "SHOW_ORDER_STATUS";
                        promptContext.append(String.format("Đơn %s: Trạng thái %s, Dự kiến %s",
                                d.get().getOrder().getOrderId(), d.get().getDeliveryStatus(), d.get().getEstimatedDeliveryDate()));
                    } else {
                        promptContext.append("Không tìm thấy đơn hàng: ").append(intent.getTracking_id());
                    }
                }
                break;

            default:
                promptContext.append("Đây là cuộc trò chuyện chung.");
        }

        // BƯỚC: GENERATE FINAL MESSAGE
        String aiMessage = generateFinalResponse(userQuestion, promptContext.toString());

        return ChatResponseDTO.builder()
                .message(aiMessage)
                .actionType(actionType)
                .dataType(mapActionToDataType(actionType))
                .data(rawData)
                .build();
    }

    // -------------------------
    // Helper: build pageable for pet based on intent
    // -------------------------
    private Pageable buildPetPageable(AiIntentDTO intent) {
        if (Boolean.TRUE.equals(intent.getWantsBest())) {
            // If user wants the best (e.g., rẻ nhất), return single item page
            Sort sort = Sort.unsorted();
            if ("PRICE".equalsIgnoreCase(intent.getSortBy()) || intent.getSortBy() == null) {
                // default for wantsBest is price ascending
                sort = Sort.by("price").ascending();
            } else if ("DATE".equalsIgnoreCase(intent.getSortBy())) {
                sort = Sort.by("createdAt").descending();
            }
            return PageRequest.of(0, 1, sort);
        } else {
            if (intent.getSortBy() != null) {
                Sort sort;
                if ("PRICE".equalsIgnoreCase(intent.getSortBy())) {
                    sort = "ASC".equalsIgnoreCase(intent.getSortDirection()) ? Sort.by("price").ascending() : Sort.by("price").descending();
                } else if ("DATE".equalsIgnoreCase(intent.getSortBy())) {
                    sort = "ASC".equalsIgnoreCase(intent.getSortDirection()) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
                } else {
                    sort = Sort.unsorted();
                }
                return PageRequest.of(0, 10, sort);
            } else {
                return PageRequest.of(0, 10);
            }
        }
    }

    // -------------------------
    // Helper: apply in-memory pet filters if repo not supporting them directly
    // -------------------------
    private List<Pet> applyPetFilters(List<Pet> pets, AiIntentDTO intent) {
        if (pets == null) return Collections.emptyList();

        return pets.stream()
                .filter(p -> {
                    if (intent.getMax_price() != null && p.getPrice() > intent.getMax_price()) return false;
                    if (intent.getMin_price() != null && p.getPrice() < intent.getMin_price()) return false;
                    if (intent.getFurType() != null && (p.getFurType() == null || !p.getFurType().equals(intent.getFurType()))) return false;
                    if (intent.getColor() != null && (p.getColor() == null || !p.getColor().equalsIgnoreCase(intent.getColor()))) return false;
                    if (intent.getKeyword() != null) {
                        String kw = intent.getKeyword().toLowerCase();
                        boolean match = (p.getName() != null && p.getName().toLowerCase().contains(kw))
                                || (p.getDescription() != null && p.getDescription().toLowerCase().contains(kw))
                                || (p.getCategory() != null && p.getCategory().getName().toLowerCase().contains(kw));
                        if (!match) return false;
                    }
                    return "AVAILABLE".equalsIgnoreCase(p.getStatus().name()); // only available pets
                })
                .collect(Collectors.toList());
    }

    // -------------------------
    // Helper: comparator for pet sorting based on intent
    // -------------------------
    private Comparator<Pet> getPetComparator(AiIntentDTO intent) {
        if ("PRICE".equalsIgnoreCase(intent.getSortBy())) {
            if ("DESC".equalsIgnoreCase(intent.getSortDirection())) {
                return Comparator.comparing(Pet::getPrice).reversed();
            } else {
                return Comparator.comparing(Pet::getPrice);
            }
        } else if ("DATE".equalsIgnoreCase(intent.getSortBy())) {
            if ("DESC".equalsIgnoreCase(intent.getSortDirection())) {
                return Comparator.comparing(Pet::getCreatedAt).reversed();
            } else {
                return Comparator.comparing(Pet::getCreatedAt);
            }
        } else {
            // default: price asc
            return Comparator.comparing(Pet::getPrice);
        }
    }

    // -------------------------
    // Helper: apply filters for services
    // -------------------------
    private List<com.pet.entity.Service> applyServiceFilters(List<com.pet.entity.Service> services, AiIntentDTO intent) {
        if (services == null) return Collections.emptyList();

        return services.stream()
                .filter(s -> {
                    if (intent.getMax_price() != null && s.getPrice() > intent.getMax_price()) return false;
                    if (intent.getKeyword() != null) {
                        String kw = intent.getKeyword().toLowerCase();
                        boolean match = (s.getName() != null && s.getName().toLowerCase().contains(kw))
                                || (s.getDescription() != null && s.getDescription().toLowerCase().contains(kw));
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    // ----------------------------------
    //    PROMPT: ANALYZE INTENT (nâng cấp)
    // ----------------------------------
    private AiIntentDTO analyzeIntent(String userInput) {

        String prompt = """
You are an intent extractor for an ecommerce pet shop (Petopia). Your job is to read a customer message and return a SINGLE JSON object that describes:
1) the primary intent (one of: SEARCH_PET, SEARCH_SERVICE, SEARCH_ARTICLE, CHECK_VOUCHER, CHECK_ORDER, GENERAL_CHAT)
2) filters the user asked for (keyword, furType, size, color)
3) numeric constraints (min_price, max_price)
4) sorting preferences (sortBy: PRICE|DATE, sortDirection: ASC|DESC)
5) whether user expects a single best result (wantsBest: true) e.g. "rẻ nhất", "đắt nhất", "tốt nhất"
6) tracking_id if present
7) an optional confidence score between 0.0 - 1.0 (higher = more confident). If low, backend can fallback to GENERAL_CHAT.

Return ONLY one JSON object. Do NOT print any other text.

JSON Format:
{
 "intent": "...",
 "keyword": "...",
 "max_price": null,
 "min_price": null,
 "furType": null,
 "size": null,
 "color": null,
 "sortBy": null,
 "sortDirection": null,
 "wantsBest": false,
 "tracking_id": null,
 "confidence": 0.90
}

Examples:
- "Tìm chó Corgi dưới 10 triệu" -> intent=SEARCH_PET, keyword="Corgi", max_price=10000000, confidence=0.95
- "Thú cưng nào rẻ nhất" -> intent=SEARCH_PET, wantsBest=true, sortBy=PRICE, sortDirection=ASC, confidence=0.9
- "Tôi cần 1 bé lông xoăn" -> intent=SEARCH_PET, furType="curly", wantsBest=false, confidence=0.9
- "Mã đơn #OD1234" -> intent=CHECK_ORDER, tracking_id="OD1234", confidence=0.95
- "Bạn khỏe không?" -> intent=GENERAL_CHAT, confidence=0.6

User message:
\"\"\"%s\"\"\" 
""".formatted(userInput);

        String jsonRaw = callGemini(prompt);

        AiIntentDTO parsed = parseJsonToAiIntent(jsonRaw);

        // Safety: if parsed null -> fallback to GENERAL_CHAT
        if (parsed == null) {
            return new AiIntentDTO("GENERAL_CHAT", null, null, null, null, null, null, null, null, null, null, 0.0);
        }
        return parsed;
    }

    // ----------------------------------
    //    PROMPT: GENERATE FINAL MESSAGE
    // ----------------------------------
    private String generateFinalResponse(String userQuestion, String context) {

        String systemPrompt = """
Bạn là trợ lý Petopia (vui vẻ, cute, emoji).
Dữ liệu tóm tắt:
%s

Yêu cầu:
1) Trả lời câu hỏi "%s" dựa trên dữ liệu nêu trên.
2) Giọng điệu: thân thiện, ngắn gọn, dùng emoji.
3) Nếu dữ liệu rỗng: xin lỗi và gợi ý phương án khác (ví dụ: phụ kiện, để lại thông tin).
4) Tuyệt đối không bịa thông tin.
""".formatted(context, userQuestion);

        return callGemini(systemPrompt);
    }

    // ----------------------------------
    //    UTIL: CALL GEMINI
    // ----------------------------------
    private String callGemini(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL_NAME + ":generateContent?key=" + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            Map res = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) res.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Gemini API Error: ", e);
            return "";
        }
    }

    // ----------------------------------
    //    UTIL: PARSE JSON to AiIntentDTO (robust)
    // ----------------------------------
    private AiIntentDTO parseJsonToAiIntent(String raw) {
        if (raw == null || raw.isEmpty()) return null;

        try {
            // Find first JSON object block
            Pattern pattern = Pattern.compile("\\{[\\s\\S]*?\\}");
            Matcher matcher = pattern.matcher(raw);
            if (!matcher.find()) {
                log.warn("analyzeIntent: no json found in response: {}", raw);
                return null;
            }
            String json = matcher.group();

            // Attempt to normalize some keys (e.g., true/false, numbers)
            return gson.fromJson(json, AiIntentDTO.class);

        } catch (Exception e) {
            log.error("parseJsonToAiIntent error, raw: {}", raw, e);
            return null;
        }
    }

    // Map Action → DataType
    private String mapActionToDataType(String action) {
        return switch (action) {
            case "SHOW_PETS" -> "pet";
            case "SHOW_SERVICES" -> "service";
            case "SHOW_ARTICLES" -> "article";
            case "SHOW_VOUCHERS" -> "voucher";
            case "SHOW_ORDER_STATUS" -> "order";
            default -> null;
        };
    }

    // ----------------------------------
    //    GENERAL CHAT HANDLER
    // ----------------------------------
    private ChatResponseDTO handleGeneralChat(String userMessage) {
        // Use LLM to create a short friendly reply
        String replyPrompt = """
Bạn là trợ lý Petopia, trả lời ngắn gọn, lịch sự, dùng emoji nếu phù hợp.
Người dùng: "%s"
""".formatted(userMessage);

        String aiReply = callGemini(replyPrompt);
        if (aiReply == null || aiReply.isEmpty()) {
            aiReply = "Mình không hiểu rõ lắm, bạn mô tả cụ thể hơn được không? 🐶";
        }

        return ChatResponseDTO.builder()
                .message(aiReply)
                .actionType("NONE")
                .dataType(null)
                .data(null)
                .build();
    }
}
