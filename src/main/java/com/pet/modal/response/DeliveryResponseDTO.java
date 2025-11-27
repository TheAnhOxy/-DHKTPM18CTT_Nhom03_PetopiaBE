package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponseDTO {
    private String deliveryId;
    private String trackingNumber;
    private String orderId;
    private String shippingMethod;
    private Double shippingFee;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Double totalAmount;
    private Integer itemCount;
    private String currentStatus;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
    private List<DeliveryHistoryResponseDTO> timeline;
}
