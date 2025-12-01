package com.pet.modal.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class BookingRequestDTO {
    @NotBlank(message = "Vui lòng chọn dịch vụ")
    private String serviceId;

    @NotNull(message = "Ngày hẹn không được để trống")
    @Future(message = "Ngày hẹn phải ở trong tương lai")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date appointmentDate;

    private String note;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity ;
    private Double priceAtPurchase;
}