package com.pet.repository;

import com.pet.entity.Order;
import com.pet.modal.response.TopSellingPetDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o.orderId FROM Order o ORDER BY o.orderId DESC LIMIT 1")
    Optional<String> findLastOrderId();

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    Double calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o WHERE o.status = 'DELIVERED'")
    Long countTotalSoldPets();

    // --- ĐÃ SỬA LẠI ĐOẠN CAST ---
    @Query("SELECT new com.pet.modal.response.TopSellingPetDTO(" +
            "p.petId, " +
            "p.name, " +
            "MIN(pi.imageUrl), " +
            "SUM(oi.quantity), " +
            "CAST(SUM(oi.priceAtPurchase * oi.quantity) AS double)) " + // <--- Ép kiểu về double tại đây
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.pet p " +
            "LEFT JOIN p.images pi ON pi.isThumbnail = true " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY p.petId, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingPetDTO> findTopSellingPets(Pageable pageable);
}