package com.pet.modal.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class PreBookingRequestDTO {
    @NotNull(message = "Vui lòng chọn thú cưng")
    private String petId;

    @NotNull(message = "Số tiền đặt cọc là bắt buộc")
    private Double depositAmount;

    @Future(message = "Ngày dự kiến nhận phải trong tương lai")
    private LocalDate expectedDate;
}