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

    // Lấy ID cuối cùng để sinh ORxxx
    @Query("SELECT o.orderId FROM Order o ORDER BY o.orderId DESC LIMIT 1")
    Optional<String> findLastOrderId();

    // Lấy đơn hàng của User
    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Tính tổng doanh thu (Chỉ tính đơn DELIVERED)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    Double calculateTotalRevenue();

    // Đếm tổng thú cưng đã bán
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o WHERE o.status = 'DELIVERED'")
    Long countTotalSoldPets();

    // FIX LỖI: Top Selling Pets
    @Query("SELECT new com.pet.modal.response.TopSellingPetDTO(" +
            "p.petId, p.name, MIN(pi.imageUrl), SUM(oi.quantity), SUM(CAST(oi.priceAtPurchase * oi.quantity AS double)) ) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.pet p " +
            "LEFT JOIN p.images pi ON pi.isThumbnail = true " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY p.petId, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingPetDTO> findTopSellingPets(Pageable pageable);

}