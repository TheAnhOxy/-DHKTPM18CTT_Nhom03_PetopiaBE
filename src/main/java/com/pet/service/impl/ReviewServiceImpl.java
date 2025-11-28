package com.pet.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.converter.ReviewConverter;
import com.pet.entity.Pet;
import com.pet.entity.Review;
import com.pet.entity.User;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.ReviewReplyRequestDTO;
import com.pet.modal.request.ReviewRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ReviewResponseDTO;
import com.pet.modal.response.ReviewStatsDTO;
import com.pet.repository.PetRepository;
import com.pet.repository.ReviewRepository;
import com.pet.repository.UserRepository;
import com.pet.service.CloudinaryService;
import com.pet.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private ObjectMapper objectMapper;

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
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        // Luôn ghi đè nội dung mới và thời gian mới
        review.setReply(replyRequest.getReplyContent());
        review.setReplyDate(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        return reviewConverter.toResponseDTO(savedReview);
    }

    // --- HÀM MỚI: XÓA REPLY ---
    @Override
    @Transactional
    public ReviewResponseDTO deleteReply(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        // Chỉ set các trường của Admin về null
        review.setReply(null);
        review.setReplyDate(null);

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

    // Tạo review với upload ảnh lên Cloudinary
    @Transactional
    public ReviewResponseDTO createReviewWithImage(ReviewRequestDTO request, MultipartFile image) throws IOException {
        // 1. Upload ảnh nếu có
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            request.setImageUrl(imageUrl);
        }

        // 2. Validate dữ liệu
        if (request.getPetId() == null || request.getPetId().isEmpty()) {
            throw new IllegalArgumentException("Pet ID không được để trống");
        }
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID không được để trống");
        }

        // 3. Tạo review
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

    @Override
    public PageResponse<ReviewResponseDTO> getReviews(String petId, Integer rating, Boolean isReplied, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findAllWithFilter(petId, rating, isReplied, pageable);
        return reviewConverter.toPageResponse(reviewPage);
    }

    @Override
    public ReviewStatsDTO getReviewStats() {
        long total = reviewRepository.count();

        // Gọi hàm ngắn gọn đã sửa
        long unreplied = reviewRepository.countUnreplied();
        long replied = reviewRepository.countReplied(); // <-- Sửa dòng này

        Double avg = reviewRepository.getAverageRating();

        Map<Integer, Long> starMap = new HashMap<>();
        List<Object[]> stars = reviewRepository.countByStars();
        for (Object[] row : stars) {
            // Rating có thể null nên cần check an toàn
            if (row[0] != null) {
                starMap.put((Integer) row[0], (Long) row[1]);
            }
        }
        for (int i = 1; i <= 5; i++) {
            starMap.putIfAbsent(i, 0L);
        }

        return ReviewStatsDTO.builder()
                .totalReviews(total)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .repliedCount(replied)
                .unrepliedCount(unreplied)
                .starCounts(starMap)
                .build();
    }
}