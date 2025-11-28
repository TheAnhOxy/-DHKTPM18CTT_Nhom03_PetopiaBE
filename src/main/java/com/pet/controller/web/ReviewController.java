package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ReviewService;
import com.pet.service.impl.ReviewServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ReviewServiceImpl reviewServiceImpl;


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

    // User tạo đánh giá (với ảnh upload lên Cloudinary)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> createReviewWithImage(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("review") String reviewJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            // Parse JSON và gán userId từ token
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            ReviewRequestDTO request = objectMapper.readValue(reviewJson, ReviewRequestDTO.class);
            request.setUserId(currentUser.getUserId());

            // Tạo review với upload ảnh - truyền request đã có userId
            var result = reviewServiceImpl.createReviewWithImage(request, image);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.builder()
                            .status(201)
                            .message("Đánh giá thành công")
                            .data(result)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status(400)
                            .message("Lỗi xử lý: " + e.getMessage())
                            .build());
        }
    }

    // User tạo đánh giá (không có ảnh, chỉ JSON)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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