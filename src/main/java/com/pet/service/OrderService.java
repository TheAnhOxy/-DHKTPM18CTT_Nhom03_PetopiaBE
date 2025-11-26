package com.pet.service;

import com.pet.enums.OrderStatus;
import com.pet.modal.request.OrderCreateRequestDTO;
import com.pet.modal.request.SePayWebhookDTO;
import com.pet.modal.response.OrderResponseDTO;
import com.pet.modal.response.PageResponse;

public interface OrderService {
    // User tạo đơn
    OrderResponseDTO createOrder(String userId, OrderCreateRequestDTO request);

    // User xem đơn của mình
    PageResponse<OrderResponseDTO> getMyOrders(String userId, int page, int size);

    // Admin xem tất cả đơn
    PageResponse<OrderResponseDTO> getAllOrders(OrderStatus status, String keyword, int page, int size);
    OrderResponseDTO getOrderDetail(String orderId);
    // Xem chi tiết đơn
    OrderResponseDTO getOrderById(String orderId);

    // Admin cập nhật trạng thái (PENDING -> SHIPPED -> DELIVERED)
    OrderResponseDTO updateOrderStatus(String orderId, OrderStatus status);

    void processSePayPayment(SePayWebhookDTO webhookData);
}