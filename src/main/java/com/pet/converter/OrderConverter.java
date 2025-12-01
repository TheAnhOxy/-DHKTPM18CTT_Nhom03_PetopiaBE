package com.pet.converter;

import com.pet.entity.Address;
import com.pet.entity.Order;
import com.pet.entity.OrderItem;
import com.pet.entity.Payment;
import com.pet.modal.response.OrderItemResponseDTO;
import com.pet.modal.response.OrderResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    @Autowired
    private PaymentRepository paymentRepository;

    public OrderResponseDTO toResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser().getUserId());
        dto.setCustomerName(order.getUser().getFullName());
        dto.setCustomerPhone(order.getPhoneNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());

        // Tính tổng giảm giá từ voucher & khuyến mãi
        double voucherDiscount = 0.0;
        if (order.getOrderVouchers() != null) {
            voucherDiscount = order.getOrderVouchers().stream()
                    .map(ov -> ov.getDiscountApplied() != null ? ov.getDiscountApplied() : 0.0)
                    .reduce(0.0, Double::sum);
        }

        double promotionDiscount = 0.0;
        if (order.getOrderPromotions() != null) {
            promotionDiscount = order.getOrderPromotions().stream()
                    .map(op -> op.getDiscountApplied() != null ? op.getDiscountApplied() : 0.0)
                    .reduce(0.0, Double::sum);
        }

        dto.setVoucherDiscountAmount(voucherDiscount);
        dto.setPromotionDiscountAmount(promotionDiscount);
        dto.setDiscountAmount(voucherDiscount + promotionDiscount);
        // Map Address thành chuỗi
        if (order.getAddress() != null) {
            Address addr = order.getAddress();
            String fullAddr = String.format("%s, %s, %s, %s",
                    addr.getStreet(), addr.getWard(), addr.getDistrict(), addr.getProvince());
            dto.setShippingAddress(fullAddr);
        }

        // Map Items
        if (order.getOrderItems() != null) {
            List<OrderItemResponseDTO> items = order.getOrderItems().stream()
                    .map(this::toItemDTO).collect(Collectors.toList());
            dto.setOrderItems(items);
        }

        // --- Map Payment Info ---
        Payment lastPayment = paymentRepository.findFirstByOrder_OrderIdOrderByCreatedAtDesc(order.getOrderId()).orElse(null);
        if (lastPayment != null) {
            dto.setPaymentMethod(lastPayment.getPaymentMethod());
            dto.setPaymentUrl(lastPayment.getPaymentUrl()); // Link QR
            dto.setTransactionId(lastPayment.getTransactionId());
        }
        return dto;
    }

    private OrderItemResponseDTO toItemDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.setPetId(item.getPet().getPetId());
        dto.setPetName(item.getPet().getName());
        dto.setPrice(item.getPriceAtPurchase());
        dto.setQuantity(item.getQuantity());
        dto.setTotalPrice(item.getPriceAtPurchase() * item.getQuantity());

        if (!item.getPet().getImages().isEmpty()) {
            dto.setPetImage(item.getPet().getImages().iterator().next().getImageUrl());
        }
        return dto;
    }

    public PageResponse<OrderResponseDTO> toPageResponse(Page<Order> page) {
        List<OrderResponseDTO> list = page.getContent().stream()
                .map(this::toResponseDTO).collect(Collectors.toList());
        return new PageResponse<>(list,page.getTotalElements(), page.getNumber(), page.getSize());
    }
}