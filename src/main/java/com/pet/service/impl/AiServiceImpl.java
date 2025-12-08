package com.pet.service.impl;

import com.google.gson.Gson;
import com.pet.entity.*;
import com.pet.modal.response.AiIntentDTO;
import com.pet.modal.response.ChatResponseDTO;
import com.pet.repository.*;
import com.pet.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    // Inject Repositories
    private final PetRepository petRepository;
    private final ServiceRepository serviceRepository;
    private final ArticleRepository articleRepository;
    private final VoucherRepository voucherRepository;
    private final DeliveryRepository deliveryRepository;

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private final RestClient restClient = RestClient.create();
    private final Gson gson = new Gson();

    @Override
    public ChatResponseDTO chat(String userInput) {
        // BƯỚC 1: Phân tích ý định (Intent Classification)
        AiIntentDTO intent = analyzeIntent(userInput);

        if (intent == null) {
            return ChatResponseDTO.builder()
                    .message("Hệ thống AI đang bận hoặc gặp lỗi kết nối, bạn thử lại sau nhé!")
                    .actionType("NONE")
                    .build();
        }

        // BƯỚC 2 & 3: Truy xuất dữ liệu (RAG) và Tổng hợp câu trả lời
        return processIntentAndGenerateResponse(intent, userInput);
    }

    private ChatResponseDTO processIntentAndGenerateResponse(AiIntentDTO intent, String userQuestion) {
        StringBuilder promptContext = new StringBuilder(); // Dữ liệu dạng text gửi cho AI đọc
        Object rawData = null;                             // Dữ liệu dạng Object gửi cho Frontend vẽ Card
        String actionType = "NONE";                        // Loại hành động để Frontend biết vẽ Card gì

        switch (intent.getIntent()) {
            case "SEARCH_PET":
                List<Pet> pets = petRepository.searchForChat(intent.getKeyword(), intent.getMax_price(), PageRequest.of(0, 5));
                if (!pets.isEmpty()) {
                    rawData = pets;
                    actionType = "SHOW_PETS"; // Frontend sẽ render ProductCard
                    promptContext.append("Hệ thống tìm thấy các thú cưng sau:\n");
                    for (Pet p : pets) {
                        promptContext.append(String.format("- Tên: %s | Giống: %s | Giá: %.0f VNĐ | Tình trạng: %s\n",
                                p.getName(), p.getCategory().getName(), p.getPrice(), p.getStatus()));
                    }
                } else {
                    promptContext.append("Hệ thống không tìm thấy thú cưng nào phù hợp với từ khóa: ").append(intent.getKeyword());
                }
                break;

            case "SEARCH_SERVICE":
                List<com.pet.entity.Service> services = serviceRepository.searchServicesForChat(intent.getKeyword() != null ? intent.getKeyword() : "");
                if (!services.isEmpty()) {
                    rawData = services;
                    actionType = "SHOW_SERVICES"; // Frontend sẽ render ServiceCard
                    promptContext.append("Hệ thống tìm thấy các dịch vụ sau:\n");
                    for (com.pet.entity.Service s : services) {
                        promptContext.append(String.format("- Dịch vụ: %s | Giá tham khảo: %.0f VNĐ | Mô tả: %s\n",
                                s.getName(), s.getPrice(), s.getDescription()));
                    }
                } else {
                    promptContext.append("Hệ thống không tìm thấy dịch vụ nào phù hợp.");
                }
                break;

            case "SEARCH_ARTICLE":
                // Tìm kiếm bài viết (Page<Article>)
                Page<Article> articlePage = articleRepository.searchArticles(intent.getKeyword() != null ? intent.getKeyword() : "", PageRequest.of(0, 3));
                if (articlePage.hasContent()) {
                    rawData = articlePage.getContent();
                    actionType = "SHOW_ARTICLES"; // Frontend sẽ render ArticleCard nhỏ
                    promptContext.append("Hệ thống tìm thấy các bài viết kiến thức sau:\n");
                    for (Article a : articlePage.getContent()) {
                        promptContext.append(String.format("- Bài viết: %s (Tác giả: %s)\n", a.getTitle(), a.getAuthor().getFullName()));
                    }
                } else {
                    promptContext.append("Không tìm thấy bài viết hướng dẫn nào về chủ đề này.");
                }
                break;

            case "CHECK_VOUCHER":
                List<Voucher> vouchers = voucherRepository.findAvailableVouchersForChat();
                if (!vouchers.isEmpty()) {
                    rawData = vouchers;
                    actionType = "SHOW_VOUCHERS"; // Frontend render VoucherCard
                    promptContext.append("Danh sách mã giảm giá đang có hiệu lực:\n");
                    for (Voucher v : vouchers) {
                        promptContext.append(String.format("- Mã: %s | Giảm: %.0f (%s) | Đơn tối thiểu: %.0f\n",
                                v.getCode(), v.getDiscountValue(), v.getDiscountType(), v.getMinOrderAmount()));
                    }
                } else {
                    promptContext.append("Hiện tại hệ thống không có mã giảm giá nào đang hoạt động.");
                }
                break;

            case "CHECK_ORDER":
                if (intent.getTracking_id() == null || intent.getTracking_id().isEmpty()) {
                    promptContext.append("Khách hàng đang hỏi về đơn hàng nhưng chưa cung cấp mã đơn hàng. Hãy yêu cầu khách cung cấp mã.");
                } else {
                    Optional<Delivery> delivery = deliveryRepository.findByTrackingOrOrderId(intent.getTracking_id());
                    if (delivery.isPresent()) {
                        Delivery d = delivery.get();
                        // Order thì thường trả text chi tiết là đủ, hoặc trả rawData để FE hiện cái box trạng thái
                        rawData = d;
                        actionType = "SHOW_ORDER_STATUS";
                        promptContext.append(String.format("Thông tin đơn hàng %s:\n- Trạng thái vận chuyển: %s\n- Đơn vị vận chuyển: %s\n- Dự kiến giao: %s\n- Phí ship: %.0f",
                                d.getOrder().getOrderId(), d.getDeliveryStatus(), d.getProvider().getName(), d.getEstimatedDeliveryDate(), d.getShippingFee()));
                    } else {
                        promptContext.append("Hệ thống không tìm thấy đơn hàng nào với mã: ").append(intent.getTracking_id());
                    }
                }
                break;

            default:
                promptContext.append("Đây là câu hỏi giao tiếp thông thường, hãy trả lời thân thiện.");
                break;
        }

        // BƯỚC 4: Gọi AI để sinh lời thoại thân thiện (Generation)
        String aiMessage = generateFinalResponse(userQuestion, promptContext.toString(), intent.getIntent());

        // BƯỚC 5: Đóng gói trả về (DTO)
        return ChatResponseDTO.builder()
                .message(aiMessage)       // Lời nói của AI (Text)
                .actionType(actionType)   // Loại Card cần hiển thị
                .data(rawData)            // Dữ liệu để vẽ Card
                .build();
    }

    // --- HÀM 1: Phân tích ý định (Intent Analysis) ---
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
            - tracking_id: (Mã đơn hàng nếu có, ví dụ DH12345).
            
            Ví dụ: "Tìm chó Corgi dưới 10 củ" -> {"intent": "SEARCH_PET", "keyword": "Corgi", "max_price": 10000000}
            Ví dụ: "Đơn hàng DH123 đi đến đâu rồi" -> {"intent": "CHECK_ORDER", "tracking_id": "DH123"}
            Ví dụ: "Làm sao để tắm cho mèo" -> {"intent": "SEARCH_ARTICLE", "keyword": "tắm cho mèo"}
            
            Câu hỏi: "%s"
            """.formatted(userInput);

        String jsonRaw = callGemini(prompt);
        return parseJson(jsonRaw);
    }

    // --- HÀM 2: Sinh câu trả lời cuối cùng (Final Generation) ---
    private String generateFinalResponse(String userQuestion, String databaseInfo, String intentType) {
        String systemPrompt = """
            Bạn là trợ lý ảo AI của hệ thống Petopia (Shop thú cưng & Spa).
            
            DỮ LIỆU TỪ HỆ THỐNG (Đã được tìm kiếm):
            %s
            
            CHỈ THỊ:
            1. Dựa vào dữ liệu trên để trả lời câu hỏi của khách: "%s".
            2. Nếu dữ liệu có sản phẩm/dịch vụ, hãy mời chào khách mua/đặt lịch một cách khéo léo, dễ thương.
            3. Nếu là CHECK_ORDER, hãy báo cáo trạng thái rõ ràng.
            4. Nếu tìm thấy bài viết (SEARCH_ARTICLE), hãy tóm tắt ngắn gọn và mời khách đọc chi tiết.
            5. Nếu không có dữ liệu, hãy xin lỗi chân thành và gợi ý chủ đề khác.
            6. Giọng điệu: Thân thiện, "nhí nhảnh", chuyên nghiệp, dùng nhiều icon 🐶🐱✨.
            7. Tuyệt đối không bịa đặt thông tin sản phẩm không có trong dữ liệu hệ thống.
            """.formatted(databaseInfo, userQuestion);

        return callGemini(systemPrompt);
    }

    // --- CÁC HÀM TIỆN ÍCH ---
    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + apiKey;
        // Cấu trúc Request Body của Gemini API
        Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        try {
            Map response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // Parse Response Gemini (Hơi lằng nhằng do cấu trúc JSON lồng nhau)
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi để debug
            return "Xin lỗi, hiện tại kết nối đến não bộ AI đang bị gián đoạn.";
        }
    }

    private AiIntentDTO parseJson(String rawText) {
        try {
            // Làm sạch chuỗi JSON trả về từ AI (thường AI hay bọc trong ```json ... ```)
            String json = rawText.replaceAll("```json", "").replaceAll("```", "").trim();
            return gson.fromJson(json, AiIntentDTO.class);
        } catch (Exception e) {
            System.err.println("Lỗi parse JSON từ AI: " + rawText);
            return null;
        }
    }
}