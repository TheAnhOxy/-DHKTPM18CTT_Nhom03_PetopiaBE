package com.pet.repository;

import com.pet.entity.Review;
import com.pet.modal.response.RecentReviewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Review> findByPet_PetIdOrderByCreatedAtDesc(String petId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.reply IS NULL OR r.reply = ''")
    Page<Review> findByReplyIsNull(Pageable pageable);

    @Query("SELECT r.reviewId FROM Review r ORDER BY r.reviewId DESC LIMIT 1")
    Optional<String> findLastReviewId();

    //  Tìm tất cả có hỗ trợ lọc rating và trạng thái trả lời
    @Query("SELECT r FROM Review r WHERE " +
            "(:petId IS NULL OR r.pet.petId = :petId) AND " +
            "(:rating IS NULL OR r.rating = :rating) AND " +
            "(:isReplied IS NULL OR (:isReplied = true AND r.reply IS NOT NULL) OR (:isReplied = false AND (r.reply IS NULL OR r.reply = ''))) " +
            "ORDER BY CASE WHEN (r.reply IS NULL OR r.reply = '') THEN 0 ELSE 1 END ASC, r.createdAt DESC")
    Page<Review> findAllWithFilter(String petId, Integer rating, Boolean isReplied, Pageable pageable);

    // --- CÁC HÀM THỐNG KÊ (ĐÃ SỬA) ---

    // 1. Đếm số chưa trả lời (Null hoặc Rỗng)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.reply IS NULL OR r.reply = ''")
    long countUnreplied();

    //  Đếm số ĐÃ trả lời (Khác Null và Khác Rỗng)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.reply IS NOT NULL AND r.reply <> ''")
    long countReplied();

    //  Đếm sao
    @Query("SELECT r.rating, COUNT(r) FROM Review r GROUP BY r.rating")
    List<Object[]> countByStars();

    //  Trung bình sao
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double getAverageRating();

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Lấy 5 bình luận mới nhất
    @Query("SELECT new com.pet.modal.response.RecentReviewDTO(" +
            "u.fullName, u.avatar, p.name, r.comment, r.rating, r.createdAt) " +
            "FROM Review r JOIN r.user u JOIN r.pet p " +
            "ORDER BY r.createdAt DESC LIMIT 5")
    List<RecentReviewDTO> findRecentReviews();
}