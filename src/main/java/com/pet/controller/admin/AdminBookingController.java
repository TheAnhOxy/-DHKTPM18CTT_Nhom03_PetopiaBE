package com.pet.controller.admin;

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
@RequestMapping("/admin/bookings")
public class AdminBookingController {


    @Autowired
    private ServiceManagement serviceManagement;



    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllBookings(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(serviceManagement.getAllBookings(keyword, page, size))
                .build());
    }


}
