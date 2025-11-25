package com.pet.converter;

import com.pet.entity.Address;
import com.pet.entity.Order;
import com.pet.entity.OrderItem;
import com.pet.modal.response.OrderItemResponseDTO;
import com.pet.modal.response.OrderResponseDTO;
import com.pet.modal.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    public OrderResponseDTO toResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUser().getUserId());
        dto.setUserName(order.getUser().getFullName());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setNote(order.getNote());
        dto.setCreatedAt(order.getCreatedAt());

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