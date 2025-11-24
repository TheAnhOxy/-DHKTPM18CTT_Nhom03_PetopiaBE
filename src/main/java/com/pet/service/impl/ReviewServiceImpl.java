package com.pet.service.impl;

import com.pet.converter.ReviewConverter;
import com.pet.entity.Pet;
import com.pet.entity.Review;
import com.pet.entity.User;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ReviewResponseDTO;
import com.pet.repository.PetRepository;
import com.pet.repository.ReviewRepository;
import com.pet.repository.UserRepository;
import com.pet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewConverter reviewConverter;

    @Override
    public PageResponse<ReviewResponseDTO> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        return reviewConverter.toPageResponse(reviewPage);
    }

    @Override
    public PageResponse<ReviewResponseDTO> getUnrepliedReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByReplyIsNull(pageable);
        return reviewConverter.toPageResponse(reviewPage);
    }

    @Override
    public PageResponse<ReviewResponseDTO> getReviewsByPetId(String petId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByPet_PetIdOrderByCreatedAtDesc(petId, pageable);
        return reviewConverter.toPageResponse(reviewPage);
    }

    @Override
    @Transactional
    public ReviewResponseDTO replyToReview(String reviewId, ReviewReplyRequestDTO replyRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Logic cập nhật trả lời
        review.setReply(replyRequest.getReplyContent());
        review.setReplyDate(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return reviewConverter.toResponseDTO(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        Review review = new Review();
        review.setReviewId(generateReviewId());
        review.setPet(pet);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrl(request.getImageUrl());
        Review savedReview = reviewRepository.save(review);
        return reviewConverter.toResponseDTO(savedReview);
    }

    @Override
    public void deleteReview(String reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        }
        reviewRepository.deleteById(reviewId);
    }

    private String generateReviewId() {
        String lastId = reviewRepository.findLastReviewId().orElse(null);

        if (lastId == null) {
            return "R001";
        }
        try {
            int number = Integer.parseInt(lastId.substring(1));
            number++;
            return String.format("R%03d", number);
        } catch (NumberFormatException e) {
            return "R" + System.currentTimeMillis();
        }
    }
}