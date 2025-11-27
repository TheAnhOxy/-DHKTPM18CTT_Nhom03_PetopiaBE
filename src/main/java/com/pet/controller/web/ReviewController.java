package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;


    // Lấy danh sách review (Có lọc) - Dùng chung cho cả Admin & User xem
    // Admin có thể lọc isReplied=false để tìm cái chưa trả lời
    @GetMapping
    public ResponseEntity<ApiResponse> getReviews(
            @RequestParam(required = false) String petId,    // Lọc theo Pet
            @RequestParam(required = false) Integer rating,  // Lọc theo sao (1-5)
            @RequestParam(required = false) Boolean isReplied, // Lọc đã/chưa trả lời
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(reviewService.getReviews(petId, rating, isReplied, page, size))
                .build());
    }

    // User tạo đánh giá
    @PostMapping
    public ResponseEntity<ApiResponse> createReview(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReviewRequestDTO request) {

        // Gán userId từ token cho an toàn
        request.setUserId(currentUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201)
                        .message("Đánh giá thành công")
                        .data(reviewService.createReview(request))
                        .build());
    }

}