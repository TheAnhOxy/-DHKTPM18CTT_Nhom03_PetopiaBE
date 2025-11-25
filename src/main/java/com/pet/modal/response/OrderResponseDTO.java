package com.pet.modal.response;

import com.pet.enums.OrderPaymentStatus;
import com.pet.enums.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private String orderId;
    private String userId;
    private String userName;

    // Thông tin giao hàng
    private String shippingAddress;
    private String phoneNumber;

    // Thông tin tiền & trạng thái
    private Double totalAmount;
    private Double shippingFee;
    private OrderStatus status;
    private OrderPaymentStatus paymentStatus;
    private String note;
    private LocalDateTime createdAt;

    // Danh sách sản phẩm
    private List<OrderItemResponseDTO> orderItems;
}