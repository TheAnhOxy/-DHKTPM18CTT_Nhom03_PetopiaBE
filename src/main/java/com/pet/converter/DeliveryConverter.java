package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Delivery;
import com.pet.entity.DeliveryHistory;
import com.pet.entity.Order;
import com.pet.enums.DeliveryStatus;
import com.pet.modal.response.DeliveryHistoryResponseDTO;
import com.pet.modal.response.DeliveryResponseDTO;
import com.pet.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class DeliveryConverter {

    @Autowired
    private ModelMapperConfig modelMapper;

    @Autowired
    private OrderRepository orderRepository;

    public DeliveryResponseDTO toResponseDTO(Delivery delivery) {
        DeliveryResponseDTO dto = modelMapper.getModelMapper().map(delivery, DeliveryResponseDTO.class);

        Order order = orderRepository.findById(delivery.getOrder().getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + delivery.getOrder().getOrderId()));

        dto.setOrderId(order.getOrderId());
        dto.setCustomerName(order.getUser().getFullName());
        dto.setCustomerPhone(order.getPhoneNumber());
        dto.setDeliveryAddress(getFullAddress(order));
        dto.setTotalAmount(order.getTotalAmount() + delivery.getShippingFee());
        dto.setItemCount(order.getOrderItems() != null ? order.getOrderItems().size() : 0);
        dto.setCurrentStatus(getVietnameseStatus(delivery.getDeliveryStatus()));

        List<DeliveryHistoryResponseDTO> timeline = delivery.getHistory().stream()
                .sorted(Comparator.comparing(DeliveryHistory::getUpdatedAt).reversed())
                .map(history -> new DeliveryHistoryResponseDTO(
                        getVietnameseStatus(history.getStatus()),
                        history.getDescription() != null ? history.getDescription() : getDefaultDescription(history.getStatus()),
                        history.getLocation(),
                        history.getUpdatedAt()
                ))
                .toList();

        dto.setTimeline(timeline);

        return dto;
    }

    private String getFullAddress(Order order) {
        if (order.getAddress() != null) {
            return String.format("%s, %s, %s, %s",
                    order.getAddress().getStreet(),
                    order.getAddress().getWard(),
                    order.getAddress().getDistrict(),
                    order.getAddress().getProvince());
        }
        return "Chưa có địa chỉ";
    }

    private String getVietnameseStatus(DeliveryStatus status) {
        return switch (status) {
            case PREPARING -> "Chuẩn bị";
            case SHIPPED -> "Đã đóng gói";
            case IN_TRANSIT -> "Đang giao";
            case DELIVERED -> "Đã giao hàng";
            case RETURNED -> "Đã trả hàng";
            case FAILED -> "Giao hàng thất bại";
        };
    }

    private String getDefaultDescription(DeliveryStatus status) {
        return switch (status) {
            case PREPARING -> "Đơn hàng đang được chuẩn bị tại kho";
            case SHIPPED -> "Đã giao cho đơn vị vận chuyển";
            case IN_TRANSIT -> "Hàng đang trên đường vận chuyển";
            case DELIVERED -> "Giao hàng thành công";
            case RETURNED -> "Khách trả hàng";
            case FAILED -> "Giao hàng không thành công";
        };
    }
}