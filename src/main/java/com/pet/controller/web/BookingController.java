package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.request.BookingRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ServiceManagement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private ServiceManagement serviceManagement;

    // User đặt lịch
    @PostMapping
    public ResponseEntity<ApiResponse> createBooking(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody BookingRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201)
                        .message("Đặt lịch thành công")
                        .data(serviceManagement.createBooking(currentUser.getUserId(), request))
                        .build());
    }

    //  User xem lịch sử đặt của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyBookings(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(serviceManagement.getMyBookings(currentUser.getUserId(), page, size))
                .build());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(serviceManagement.getAllBookings(page, size))
                .build());
    }

    @GetMapping("/services")
    public ResponseEntity<ApiResponse> getPublicServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(serviceManagement.getAllServices(page, size))
                .build());
    }
}