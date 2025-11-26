package com.pet.controller.admin;

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
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDetail(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.builder().status(200).data(orderService.getOrderDetail(id)).build());
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).data(orderService.getAllOrders(status, keyword, page, size)).build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật trạng thái đơn hàng thành công")
                .data(orderService.updateOrderStatus(id, status))
                .build());
    }

}
