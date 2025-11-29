package com.pet.repository;

import com.pet.entity.Order;
import com.pet.entity.User;
import com.pet.enums.OrderStatus;
import com.pet.modal.response.MonthlyRevenueDTO;
import com.pet.modal.response.TopSellingPetDTO;
import com.pet.modal.response.TopUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o.orderId FROM Order o ORDER BY o.orderId DESC LIMIT 1")
    Optional<String> findLastOrderId();

    @Query("SELECT o.user FROM Order o JOIN o.orderItems oi WHERE oi.pet.petId = :petId AND o.status = 'DELIVERED' ORDER BY o.createdAt DESC")
    List<User> findOwnersByPetId(String petId);
    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED'")
    Double calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o WHERE o.status = 'DELIVERED'")
    Long countTotalSoldPets();

    @Query("SELECT new com.pet.modal.response.TopSellingPetDTO(" +
            "p.petId, " +
            "p.name, " +
            "MIN(pi.imageUrl), " +
            "SUM(oi.quantity), " +
            "CAST(SUM(oi.priceAtPurchase * oi.quantity) AS double)) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.pet p " +
            "LEFT JOIN p.images pi ON pi.isThumbnail = true " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY p.petId, p.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingPetDTO> findTopSellingPets(Pageable pageable);


    // ADMIN: Tìm kiếm đa năng (Mã đơn, Tên khách, Trạng thái)
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:keyword IS NULL OR o.orderId LIKE %:keyword% OR o.user.fullName LIKE %:keyword%)")
    Page<Order> searchOrders(OrderStatus status, String keyword, Pageable pageable);

    @Query("SELECT new com.pet.modal.response.TopUserDTO(" +
            "u.userId, u.fullName, u.email, u.avatar, SUM(o.totalAmount), COUNT(o)) " +
            "FROM Order o " +
            "JOIN o.user u " +
            "WHERE o.status = 'DELIVERED' " + // Chỉ tính đơn thành công
            "GROUP BY u.userId, u.fullName, u.email, u.avatar " +
            "ORDER BY SUM(o.totalAmount) DESC")
    List<TopUserDTO> findTopSpendingUsers(Pageable pageable);

    // 1. Tính tổng doanh thu trong khoảng thời gian (Chỉ tính DELIVERED)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :startDate AND :endDate")
    Double calculateRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    // 2. Đếm số đơn theo trạng thái
    long countByStatus(OrderStatus status);

    @Query(value = "SELECT " +
            "MONTH(o.created_at) as month, " +
            "YEAR(o.created_at) as year, " +
            "SUM(o.total_amount) as revenue, " +
            "(SUM(o.total_amount) * 0.3) as profit " +
            "FROM orders o " +
            "WHERE o.status = 'DELIVERED' AND YEAR(o.created_at) = :year " +
            "GROUP BY YEAR(o.created_at), MONTH(o.created_at) " +
            "ORDER BY MONTH(o.created_at) ASC", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("year") int year);


    // 2. Đếm tổng đơn hàng trong khoảng
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 3. Đếm đơn hủy trong khoảng
    long countByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}