package com.pet.modal.request;

import com.pet.enums.BookingStatus; // PENDING, CONFIRMED, CANCELLED
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PreBookingStatusDTO {
    @NotNull(message = "Trạng thái không được để trống")
    private BookingStatus status;

    private String note;
}