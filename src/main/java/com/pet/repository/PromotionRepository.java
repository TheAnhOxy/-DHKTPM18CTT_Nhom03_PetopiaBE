package com.pet.repository;

import com.pet.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' " +
            "AND p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findActivePromotions(LocalDate today);

    @Query("SELECT p.promotionId FROM Promotion p ORDER BY p.promotionId DESC LIMIT 1")
    java.util.Optional<String> findLastId();
}
