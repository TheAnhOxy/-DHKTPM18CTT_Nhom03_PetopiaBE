package com.pet.service;


import com.google.gson.Gson;
import com.pet.modal.response.AiAttributesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttributeExtractor {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private static final String MODEL_NAME = "gemini-2.5-flash";
    private final RestClient restClient = RestClient.create();
    private final Gson gson = new Gson();

    /**
     * Call LLM to extract attributes JSON from user message.
     * Returns AiAttributesDTO or null on failure.
     */
    public AiAttributesDTO extract(String userMessage) {
        String prompt = buildPrompt(userMessage);
        String raw = callGemini(prompt);
        System.out.println("AttributeExtractor raw response: " + raw );
        if (raw == null || raw.isEmpty()) return null;

        try {
            Pattern p = Pattern.compile("\\{[\\s\\S]*?\\}");
            Matcher m = p.matcher(raw);
            if (!m.find()) {
                log.warn("AttributeExtractor: no JSON found in LLM response: {}", raw);
                return null;
            }
            String json = m.group();
            return gson.fromJson(json, AiAttributesDTO.class);
        } catch (Exception e) {
            log.error("AttributeExtractor parse error, raw: {}", raw, e);
            return null;
        }
    }

    private String buildPrompt(String userMessage) {
        return """
You are an advanced attribute extractor for a pet-ecommerce AI system.

Your job: read the user message and output EXACTLY ONE JSON object.
No explanation, no markdown, no additional text.

========================================
### 1. FIELDS TO RETURN (STRICT JSON)
{
  "domain": "pet | service | article | voucher | order | general",
  "intent": "search | ask_detail | filter | general_chat",
  "keyword": string | null,
  "keywords": string[] | [],
  "minPrice": number | null,
  "maxPrice": number | null,
  "sortBy": "PRICE | DATE | RELEVANCE | null",
  "sortDirection": "ASC | DESC | null",
  "limit": number | null,
  "page": number | null,
  "pageSize": number | null,

  "breed": string | null,
  "category": string | null,
  "size": string | null,
  "furType": string | null,
  "color": string | null,
  "gender": "MALE | FEMALE | null",
  "minAge": number | null,
  "maxAge": number | null,
  "availableOnly": boolean | null,

  "serviceType": string | null,
  "duration": string | null,

  "topic": string | null,
  "author": string | null,
  "trending": boolean | null,

  "voucherCode": string | null,
  "voucherType": "PERCENTAGE | FIXED_AMOUNT | null",
  "onlyValid": boolean | null,

  "trackingId": string | null,
  "orderId": string | null,

  "confidence": number
}

========================================
### 2. DOMAIN DETECTION (Synonym Mapping)

#### PET (any of these → domain="pet")
["thú cưng","vật nuôi","động vật","pet","chó","cún","chó con","mèo","bé cưng",
 "poodle","phốc sóc","alaska","husky","corgi","mèo anh","mèo aln","mèo chân ngắn",
 "puppy","kitten"]

#### SERVICE
["dịch vụ","tắm","spa","làm đẹp","cạo lông","grooming","khách sạn thú cưng",
 "trông hộ","giữ hộ","tiêm vaccine"]

#### ARTICLE
["bài viết","kiến thức","tin tức","hướng dẫn","blog"]

#### VOUCHER
["voucher","mã giảm giá","ưu đãi","khuyến mãi","discount","code"]

#### ORDER
["đơn hàng","mã đơn","vận chuyển","ship","tracking","đơn","order"]

If message only contains greetings, chit-chat → domain = "general".

========================================
### 3. PRICE RULES
- Convert all money phrases to VND integer.
- “dưới X triệu” → maxPrice = X * 1,000,000
- “trên X triệu” → minPrice = X * 1,000,000
- “khoảng A - B” → minPrice = A; maxPrice = B

========================================
### 4. SORTING RULES
- “rẻ nhất” → sortBy=PRICE, sortDirection=ASC, limit=1
- “đắt nhất” → sortBy=PRICE, sortDirection=DESC, limit=1
- “mới nhất” → sortBy=DATE, sortDirection=DESC

========================================
### 5. CONFIDENCE
- If domain clearly detected → confidence = 0.8–1.0
- If ambiguous → domain="general", confidence=0.3

========================================

ONLY RETURN VALID JSON.

User message:
\"\"\"%s\"\"\"
""".formatted(userMessage);
    }


    private String callGemini(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + apiKey;
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
            log.error("AttributeExtractor: Gemini call error", e);
            return "";
        }
    }
}
