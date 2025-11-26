package com.pet.modal.response;

import com.pet.enums.BookingStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PreBookingResponseDTO {
    private String bookingId;

    private String userId;
    private String userName;
    private String userPhone;

    private String petId;
    private String petName;
    private String petImage;
    private Double petPrice;

    private Double depositAmount;
    private LocalDate expectedDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
}