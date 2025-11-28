package com.pet.repository;

import com.pet.entity.OrderPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderPromotionRepository extends JpaRepository<OrderPromotion, String> {
    @Query("SELECT op.orderPromotionId FROM OrderPromotion op ORDER BY op.orderPromotionId DESC LIMIT 1")
    Optional<String> findLastId();
}