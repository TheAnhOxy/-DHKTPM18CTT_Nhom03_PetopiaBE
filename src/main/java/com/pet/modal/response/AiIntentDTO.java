package com.pet.modal.response;

import lombok.Data;

@Data
public class AiIntentDTO {
    // Loại ý định: SEARCH_PET, SEARCH_SERVICE, SEARCH_ARTICLE, CHECK_ORDER, CHECK_VOUCHER, GENERAL_CHAT
    private String intent;

    private String keyword;     // Từ khóa (ví dụ: "Corgi", "Poodle", "Spa")
    private Double max_price;   // Giá tối đa khách nhắc đến
    private String tracking_id; // Mã đơn hàng (nếu khách hỏi vận chuyển)
}