package com.pet.modal.response;

import com.fasterxml.jackson.annotation.JsonFormat; // <--- Import này
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Nên thêm cái này để tránh lỗi deserialization khác
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentReviewDTO {
    private String userName;
    private String userAvatar;
    private String petName;
    private String comment;
    private Integer rating;

    // Định dạng ngày giờ chuẩn để Jackson hiểu và biến thành chuỗi String
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;
}