package com.pet.modal.response;

import com.pet.enums.OrderPaymentStatus;
import com.pet.enums.OrderStatus;
import com.pet.enums.PaymentMethod;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private String orderId;

    // User Info
    private String userId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;

    // Order Info
    private Double totalAmount;
    private Double shippingFee;
    private OrderStatus status;
    private OrderPaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String note;
    private LocalDateTime createdAt;

    // Payment Info (Nếu chọn CK)
    private String paymentUrl; // Link ảnh QR Code
    private String transactionId;

    // Tổng giảm giá (voucher + khuyến mãi)
    private Double discountAmount;
    // Giảm giá từ voucher
    private Double voucherDiscountAmount;
    // Giảm giá từ khuyến mãi
    private Double promotionDiscountAmount;
    // Items
    private List<OrderItemResponseDTO> orderItems;
}