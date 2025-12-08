package com.pet.controller.admin;

import com.pet.entity.User;
import com.pet.enums.BookingStatus;
import com.pet.modal.request.PreBookingRequestDTO;
import com.pet.modal.request.PreBookingStatusDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.impl.PreBookingServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/admin/pre-bookings")
public class AdminPreBookingController {

    @Autowired
    private PreBookingServiceImpl preBookingService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllPreBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy danh sách thành công")
                .data(preBookingService.getAllPreBookings(status, keyword, page, size))
                .build());
    }

    // Admin duyệt/hủy đơn (Kèm gửi mail)
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable String bookingId,
            @Valid @RequestBody PreBookingStatusDTO request) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Đã cập nhật trạng thái và gửi email thông báo")
                .data(preBookingService.updateStatus(bookingId, request))
                .build());
    }

}
