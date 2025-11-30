package com.pet.service.impl;

import com.google.gson.Gson;
import com.pet.entity.*;
import com.pet.modal.dto.AiIntentDTO;
import com.pet.repository.*;
import com.pet.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // Inject tất cả Repository cần thiết để AI "biết tuốt"
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final ArticleRepository articleRepository;
    private final VoucherRepository voucherRepository;
    private final DeliveryRepository deliveryRepository;

    private static final String MODEL_NAME = "gemini-2.5-flash"; // Hoặc gemini-1.5-flash
    private final RestClient restClient = RestClient.create();
    private final Gson gson = new Gson();

    @Override
    public String chat(String userInput) {
        // BƯỚC 1: PHÂN TÍCH Ý ĐỊNH (INTENT CLASSIFICATION)
        AiIntentDTO intent = analyzeIntent(userInput);

        if (intent == null) return "Hệ thống AI đang bận, vui lòng thử lại sau.";

        // BƯỚC 2: TRUY XUẤT DỮ LIỆU (RAG)
        String databaseContext = retrieveData(intent);

        // BƯỚC 3: TỔNG HỢP CÂU TRẢ LỜI
        return generateFinalResponse(userInput, databaseContext, intent.getIntent());
    }

    // --- HÀM 1: Dùng AI để phân tích xem khách muốn gì ---
    private AiIntentDTO analyzeIntent(String userInput) {
        String prompt = """
            Bạn là bộ não phân tích của hệ thống Petopia. Hãy đọc câu hỏi và trích xuất thông tin ra JSON.
            
            Các loại Intent (intent):
            - SEARCH_PET: Tìm mua thú cưng, hỏi giá chó mèo.
            - SEARCH_SERVICE: Hỏi về spa, cắt tỉa lông, khách sạn thú cưng, tiêm phòng.
            - SEARCH_ARTICLE: Hỏi kiến thức chăm sóc, bệnh tật, kinh nghiệm nuôi.
            - CHECK_VOUCHER: Hỏi về khuyến mãi, mã giảm giá.
            - CHECK_ORDER: Hỏi về tình trạng đơn hàng, vận chuyển (Cần trích xuất mã đơn).
            - GENERAL_CHAT: Chào hỏi, khen ngợi, hoặc câu hỏi không liên quan shop.
            
            Các trường cần lấy:
            - intent: (Bắt buộc theo list trên)
            - keyword: (Tên con vật, tên bệnh, tên dịch vụ...)
            - max_price: (Số tiền tối đa nếu khách nhắc đến, ví dụ 'dưới 5 triệu' -> 5000000).
            - tracking_id: (Mã đơn hàng nếu có).
            
            Ví dụ: "Tìm chó Corgi dưới 10 củ" -> {"intent": "SEARCH_PET", "keyword": "Corgi", "max_price": 10000000}
            Ví dụ: "Đơn hàng DH123 đi đến đâu rồi" -> {"intent": "CHECK_ORDER", "tracking_id": "DH123"}
            Ví dụ: "Cách chữa bệnh ghẻ cho chó" -> {"intent": "SEARCH_ARTICLE", "keyword": "ghẻ"}
            
            Câu hỏi: "%s"
            """.formatted(userInput);

        String jsonRaw = callGemini(prompt);
        return parseJson(jsonRaw);
    }

    // --- HÀM 2: Lấy dữ liệu từ DB dựa trên Intent ---
    private String retrieveData(AiIntentDTO intent) {
        StringBuilder data = new StringBuilder();

        switch (intent.getIntent()) {
            case "SEARCH_PET":
                List<Pet> pets = petRepository.searchForChat(intent.getKeyword(), intent.getMax_price(), PageRequest.of(0, 5));
                if (pets.isEmpty()) return "Không tìm thấy thú cưng nào phù hợp trong kho.";
                data.append("Danh sách thú cưng tìm thấy:\n");
                for (Pet p : pets) {
                    data.append(String.format("- Tên: %s | Giống: %s | Giá: %.0f VNĐ | Tình trạng: %s\n",
                            p.getName(), p.getCategory().getName(), p.getPrice(), p.getStatus()));
                }
                break;

            case "SEARCH_SERVICE":
                List<com.pet.entity.Service> services = serviceRepository.searchServicesForChat(intent.getKeyword() != null ? intent.getKeyword() : "");
                if (services.isEmpty()) return "Không tìm thấy dịch vụ nào.";
                data.append("Các dịch vụ tại Petopia:\n");
                for (com.pet.entity.Service s : services) {
                    data.append(String.format("- Dịch vụ: %s | Giá tham khảo: %.0f VNĐ | Mô tả: %s\n",
                            s.getName(), s.getPrice(), s.getDescription()));
                }
                break;

            case "SEARCH_ARTICLE":
                // Dùng hàm searchArticles có sẵn trong repo của bạn
                var articlePage = articleRepository.searchArticles(intent.getKeyword() != null ? intent.getKeyword() : "", PageRequest.of(0, 3));
                if (articlePage.isEmpty()) return "Không tìm thấy bài viết hướng dẫn nào.";
                data.append("Kiến thức liên quan:\n");
                for (Article a : articlePage.getContent()) {
                    data.append(String.format("- Bài: %s (Tác giả: %s)\n  Tóm tắt: %s...\n",
                            a.getTitle(), a.getAuthor().getFullName(), a.getContent().substring(0, Math.min(a.getContent().length(), 100))));
                }
                break;

            case "CHECK_VOUCHER":
                List<Voucher> vouchers = voucherRepository.findAvailableVouchersForChat();
                if (vouchers.isEmpty()) return "Hiện tại không có mã giảm giá nào đang hoạt động.";
                data.append("Danh sách mã giảm giá HOT:\n");
                for (Voucher v : vouchers) {
                    data.append(String.format("- Mã: %s | Giảm: %.0f (%s) | Đơn tối thiểu: %.0f\n",
                            v.getCode(), v.getDiscountValue(), v.getDiscountType(), v.getMinOrderAmount()));
                }
                break;

            case "CHECK_ORDER":
                if (intent.getTracking_id() == null) return "Khách chưa cung cấp mã đơn hàng.";
                Optional<Delivery> delivery = deliveryRepository.findByTrackingOrOrderId(intent.getTracking_id());
                if (delivery.isPresent()) {
                    Delivery d = delivery.get();
                    data.append(String.format("Thông tin đơn hàng %s:\n- Trạng thái: %s\n- Vận chuyển bởi: %s\n- Dự kiến giao: %s\n- Phí ship: %.0f",
                            d.getOrder().getOrderId(), d.getDeliveryStatus(), d.getProvider().getName(), d.getEstimatedDeliveryDate(), d.getShippingFee()));
                } else {
                    return "Không tìm thấy đơn hàng nào với mã " + intent.getTracking_id();
                }
                break;

            default:
                return "Đây là câu hỏi giao tiếp thông thường, không cần tra dữ liệu.";
        }
        return data.toString();
    }

    // --- HÀM 3: Sinh câu trả lời cuối cùng ---
    private String generateFinalResponse(String userQuestion, String databaseInfo, String intentType) {
        String systemPrompt = """
            Bạn là trợ lý ảo AI của hệ thống Petopia (Shop thú cưng, spa & kiến thức).
            
            DỮ LIỆU TỪ HỆ THỐNG:
            %s
            
            CHỈ THỊ:
            1. Dựa vào dữ liệu trên để trả lời câu hỏi: "%s".
            2. Nếu dữ liệu có sản phẩm/dịch vụ, hãy mời chào khách mua/đặt lịch một cách khéo léo.
            3. Nếu là CHECK_ORDER, hãy báo cáo trạng thái rõ ràng.
            4. Nếu không có dữ liệu, hãy xin lỗi và gợi ý chủ đề khác.
            5. Giọng điệu: Thân thiện, chuyên nghiệp, dùng icon 🐶🐱.
            6. Nếu khách hỏi ngoài lề (không liên quan thú cưng), hãy từ chối lịch sự.
            """.formatted(databaseInfo, userQuestion);

        return callGemini(systemPrompt);
    }

    // --- CÁC HÀM TIỆN ÍCH (Giữ nguyên như trước) ---
    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + apiKey;
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        try {
            Map response = restClient.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(requestBody).retrieve().body(Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Lỗi kết nối AI.";
        }
    }

    private AiIntentDTO parseJson(String rawText) {
        try {
            String json = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
            return gson.fromJson(json, AiIntentDTO.class);
        } catch (Exception e) {
            return null;
        }
    }
}