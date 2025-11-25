package com.pet.controller.web;

import com.pet.entity.User;
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

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // --- USER ---

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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(orderService.getMyOrders(currentUser.getUserId(), page, size))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderDetail(@PathVariable String id) {
        // (Cần thêm logic check quyền: Chỉ user chủ đơn hoặc admin mới xem được)
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(orderService.getOrderById(id))
                .build());
    }


}