package com.pet.modal.request;

import com.pet.enums.OrderPaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateRequestDTO {
    @NotBlank(message = "Vui lòng chọn địa chỉ nhận hàng")
    private String addressId;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<OrderItemRequestDTO> items;

    private String note;
    private OrderPaymentStatus paymentMethod; // UNPAID (COD) hoặc PAID (VNPAY...)
}