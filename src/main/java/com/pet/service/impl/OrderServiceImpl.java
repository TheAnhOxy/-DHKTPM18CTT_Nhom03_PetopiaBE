package com.pet.service.impl;

import com.pet.converter.OrderConverter;
import com.pet.entity.*;
import com.pet.enums.OrderPaymentStatus;
import com.pet.enums.OrderStatus;
import com.pet.enums.PetStatus;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.OrderCreateRequestDTO;
import com.pet.modal.request.OrderItemRequestDTO;
import com.pet.modal.response.OrderResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.*;
import com.pet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private OrderConverter orderConverter;

    //  Tạo Đơn Hàng ---
    @Override
    @Transactional
    @CacheEvict(value = "dashboard_general_stats", allEntries = true) // Xóa cache thống kê
    public OrderResponseDTO createOrder(String userId, OrderCreateRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUser(user);
        order.setAddress(address);
        order.setPhoneNumber(user.getPhoneNumber()); // Lấy sđt user làm mặc định
        order.setNote(request.getNote());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(request.getPaymentMethod());
        order.setShippingFee(30000.0); // Giả sử phí ship cố định hoặc tính sau

        // 2. Process Items & Calculate Total
        double totalAmount = 0;
        Set<OrderItem> orderItems = new HashSet<>();

        // Sinh ID cho OrderItem: lấy timestamp + số thứ tự để tránh trùng
        long currentTime = System.currentTimeMillis();
        int count = 0;

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            Pet pet = petRepository.findById(itemReq.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + itemReq.getPetId()));

            // Check stock (Số lượng tồn kho)
            if (pet.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Thú cưng " + pet.getName() + " không đủ số lượng");
            }

            // Trừ tồn kho
            pet.setStockQuantity(pet.getStockQuantity() - itemReq.getQuantity());
            if (pet.getStockQuantity() == 0) {
                 pet.setStatus(PetStatus.SOLD);
            }
            petRepository.save(pet);

            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderItemId(generateOrderItemId());
            orderItem.setOrder(order);
            orderItem.setPet(pet);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPriceAtPurchase(pet.getPrice());

            totalAmount += pet.getPrice() * itemReq.getQuantity();
            orderItems.add(orderItem);
        }
        order.setTotalAmount(totalAmount + order.getShippingFee());
        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        return orderConverter.toResponseDTO(savedOrder);
    }

    // Admin Update Trạng Thái ---
    @Override
    @Transactional
    @CacheEvict(value = {"dashboard_general_stats", "dashboard_top_selling"}, allEntries = true)
    public OrderResponseDTO updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        // Nếu delivered -> update payment status thành PAID (nếu là COD)
        if (status == OrderStatus.DELIVERED) {
             order.setPaymentStatus(OrderPaymentStatus.PAID);
        }

        return orderConverter.toResponseDTO(orderRepository.save(order));
    }

    @Override
    public PageResponse<OrderResponseDTO> getMyOrders(String userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return orderConverter.toPageResponse(orders);
    }

    @Override
    public PageResponse<OrderResponseDTO> getAllOrders(int page, int size) {
        Page<Order> orders = orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return orderConverter.toPageResponse(orders);
    }

    @Override
    public OrderResponseDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderConverter.toResponseDTO(order);
    }

    private String generateOrderId() {
        String lastId = orderRepository.findLastOrderId().orElse("O000");
        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("O%03d", num + 1);
        } catch (Exception e) {
            return "O001";
        }
    }
    private String generateOrderItemId() {
        String lastId = orderItemRepository.findLastOrderItemId().orElse("OI000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("OI%03d", num + 1);
        } catch (Exception e) {
            return "OI001";
        }
    }


}