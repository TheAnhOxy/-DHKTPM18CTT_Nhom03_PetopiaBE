package com.pet.controller;

import com.pet.entity.User;
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
@RequestMapping("/pre-bookings")
public class PreBookingController {

    @Autowired
    private PreBookingServiceImpl preBookingService;


    //  User tạo yêu cầu đặt trước
    @PostMapping
    public ResponseEntity<ApiResponse> createPreBooking(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PreBookingRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201)
                        .message("Gửi yêu cầu đặt trước thành công. Vui lòng chờ xác nhận.")
                        .data(preBookingService.createPreBooking(currentUser.getUserId(), request))
                        .build());
    }

    // User xem danh sách của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyPreBookings(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(preBookingService.getMyPreBookings(currentUser.getUserId(), page, size))
                .build());
    }



}