package com.pet.modal.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class BookingResponseDTO {
    private String bookingServiceId;

    // Thông tin User
    private String userId;
    private String userName;

    // Thông tin Service
    private String serviceId;
    private String serviceName;
    private String serviceImage;

    // Thông tin đặt lịch
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date appointmentDate;

    private String note;
    private Integer quantity;
    private Double priceAtPurchase; // Giá tại thời điểm đặt
    private Double totalAmount;     // Tổng tiền (price * quantity)
}