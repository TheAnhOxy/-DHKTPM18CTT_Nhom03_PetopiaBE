package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Review;
import com.pet.entity.PetImage;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.ReviewResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewConverter {

    @Autowired
    private ModelMapperConfig modelMapper;

    public ReviewResponseDTO toResponseDTO(Review review) {
        // Map các trường cơ bản tự động (reviewId, rating, comment, reply...)
        ReviewResponseDTO dto = modelMapper.getModelMapper().map(review, ReviewResponseDTO.class);

        dto.setReviewImageUrl(review.getImageUrl());
        // --- 1. MAP THÔNG TIN PET & ẢNH PET ---
        if (review.getPet() != null) {
            dto.setPetId(review.getPet().getPetId());
            dto.setPetName(review.getPet().getName());

            // LOGIC LẤY ẢNH PET:
            // Kiểm tra danh sách ảnh của Pet có tồn tại không
            if (review.getPet().getImages() != null && !review.getPet().getImages().isEmpty()) {

                // Cách 1: Tìm ảnh được đánh dấu là Thumbnail (isThumbnail = true)
                String petImageUrl = review.getPet().getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsThumbnail())) // Lọc ảnh thumbnail
                        .map(PetImage::getImageUrl) // Lấy URL
                        .findFirst()
                        .orElse(null);

                // Cách 2: Nếu không có thumbnail set cứng, lấy bừa ảnh đầu tiên trong list
                if (petImageUrl == null) {
                    petImageUrl = review.getPet().getImages().iterator().next().getImageUrl();
                }

                // Gán vào DTO
                dto.setPetImage(petImageUrl);
            }
        }

        // --- 2. MAP THÔNG TIN USER (NGƯỜI ĐÁNH GIÁ) ---
        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getUserId());
            dto.setUserFullName(review.getUser().getFullName());
            dto.setUserAvatar(review.getUser().getAvatar());
        }

        // --- 3. MAP ẢNH REVIEW (image_url) ---
        dto.setReviewImageUrl(review.getImageUrl());

        return dto;
    }

    public PageResponse<ReviewResponseDTO> toPageResponse(Page<Review> reviewPage) {
        List<ReviewResponseDTO> dtos = reviewPage.getContent().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        PageResponse<ReviewResponseDTO> response = new PageResponse<>();
        response.setContent(dtos);
        response.setPage(reviewPage.getNumber());
        response.setSize(reviewPage.getSize());
        response.setTotalElements(reviewPage.getTotalElements());
        return response;
    }
}