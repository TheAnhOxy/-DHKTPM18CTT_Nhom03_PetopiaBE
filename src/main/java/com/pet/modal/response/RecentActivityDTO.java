package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDTO {
    private String activityType; // "VACCINE" (Lịch khám/tiêm), "PRE_BOOKING" (Đặt trước)
    private String customerName;
    private String customerAvatar;
    private String petName;
    private String description; // Tên vaccine hoặc ghi chú
    private LocalDateTime time; // Thời gian tạo hoặc thời gian hẹn
    private String status;      // Trạng thái
}