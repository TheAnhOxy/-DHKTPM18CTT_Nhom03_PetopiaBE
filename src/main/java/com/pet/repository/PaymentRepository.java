package com.pet.repository;

import com.pet.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query("SELECT p.paymentId FROM Payment p ORDER BY p.paymentId DESC LIMIT 1")
    Optional<String> findLastPaymentId();

    // Tìm payment theo OrderID
    Optional<Payment> findFirstByOrder_OrderIdOrderByCreatedAtDesc(String orderId);
}