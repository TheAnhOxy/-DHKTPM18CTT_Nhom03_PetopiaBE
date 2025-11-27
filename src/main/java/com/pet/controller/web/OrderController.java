package com.pet.controller.web;

import com.pet.enums.OrderStatus;
import com.pet.modal.request.OrderCreateRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.pet.entity.User;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderService orderService;

    //  User tạo đơn
    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody OrderCreateRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201)
                        .message("Đặt hàng thành công")
                        .data(orderService.createOrder(currentUser.getUserId(), request))
                        .build());
    }

    //  User xem đơn của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyOrders(@AuthenticationPrincipal User currentUser,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(orderService.getMyOrders(currentUser.getUserId(), page, size))
                .build());
    }

    // Xem chi tiết đơn (User/Admin)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDetail(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(orderService.getOrderDetail(id))
                .build());
    }

    // Admin: Quản lý đơn (Có lọc status & tìm kiếm)
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).data(orderService.getAllOrders(status, keyword, page, size)).build());
    }
}