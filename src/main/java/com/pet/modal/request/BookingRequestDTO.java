package com.pet.modal.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity;

    @NotNull(message = "Giá tại thời điểm đặt không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double priceAtPurchase;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Họ tên không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;
}
