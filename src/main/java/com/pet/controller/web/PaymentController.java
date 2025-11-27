package com.pet.controller.web;

import com.pet.modal.request.SePayWebhookDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.impl.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired private OrderServiceImpl orderService;

    // API này sẽ được SePay gọi tự động (Bạn cần config trên SePay)
    @PostMapping("/sepay-webhook")
    public ResponseEntity<ApiResponse> handleSePayWebhook(@RequestBody SePayWebhookDTO webhookData) {
        try {
            orderService.processSePayPayment(webhookData);
            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200).message("Xử lý thanh toán thành công").build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status(400).message("Lỗi xử lý: " + e.getMessage()).build());
        }
    }
}