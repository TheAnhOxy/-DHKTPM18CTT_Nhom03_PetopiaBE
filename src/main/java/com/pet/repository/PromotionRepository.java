package com.pet.repository;

import com.pet.entity.Promotion;
import com.pet.enums.PromotionType;
import com.pet.enums.PromotionVoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);

    @Query("""
    SELECT p FROM Promotion p 
    WHERE (:keyword IS NULL OR LOWER(p.code) LIKE :keyword OR LOWER(p.description) LIKE :keyword)
      AND (:categoryId IS NULL OR p.category.categoryId = :categoryId)
      AND (:status IS NULL OR p.status = :status)
      AND (:type IS NULL OR p.promotionType = :type)
    """)
    Page<Promotion> searchPromotions(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            @Param("status") PromotionVoucherStatus status,
            @Param("type") PromotionType type,
            Pageable pageable
    );
}
