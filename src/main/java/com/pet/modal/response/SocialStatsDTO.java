package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SocialStatsDTO {
    private long totalLikes;    // Tổng lượt yêu thích toàn hệ thống
    private long totalComments; // Tổng bình luận toàn hệ thống

    // Danh sách Pet được yêu thích nhiều nhất (Top 5)
    private List<TopFavoritedPetDTO> topLikedPets;

    // Danh sách Bình luận gần đây (Top 5)
    private List<RecentReviewDTO> recentReviews;
}