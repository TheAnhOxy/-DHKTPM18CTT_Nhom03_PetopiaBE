package com.pet.service;

import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ReviewResponseDTO;

public interface ReviewService {
    PageResponse<ReviewResponseDTO> getAllReviews(int page, int size);
    PageResponse<ReviewResponseDTO> getUnrepliedReviews(int page, int size);
    PageResponse<ReviewResponseDTO> getReviewsByPetId(String petId, int page, int size);
    ReviewResponseDTO replyToReview(String reviewId, ReviewReplyRequestDTO replyRequest);
    ReviewResponseDTO createReview(ReviewRequestDTO request);
    void deleteReview(String reviewId);
}