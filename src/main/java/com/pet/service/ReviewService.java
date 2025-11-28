package com.pet.service;

import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ReviewResponseDTO;
import com.pet.modal.response.ReviewStatsDTO;

public interface ReviewService {
    PageResponse<ReviewResponseDTO> getAllReviews(int page, int size);
    PageResponse<ReviewResponseDTO> getUnrepliedReviews(int page, int size);
    PageResponse<ReviewResponseDTO> getReviewsByPetId(String petId, int page, int size);
    // Hàm này dùng cho cả Tạo mới và Sửa reply (Ghi đè)
    ReviewResponseDTO replyToReview(String reviewId, ReviewReplyRequestDTO replyRequest);

    // Hàm MỚI: Xóa riêng phần trả lời của Admin
    ReviewResponseDTO deleteReply(String reviewId);
    ReviewResponseDTO createReview(ReviewRequestDTO request);
    void deleteReview(String reviewId);

    // API Lọc nâng cao (Thay thế getAllReviews cũ)
    PageResponse<ReviewResponseDTO> getReviews(String petId, Integer rating, Boolean isReplied, int page, int size);

    // API Thống kê
    ReviewStatsDTO getReviewStats();
}