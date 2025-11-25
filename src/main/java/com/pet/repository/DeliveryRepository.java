package com.pet.repository;

import com.pet.entity.Delivery;
import com.pet.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {
    // Đếm số đơn đang vận chuyển (SHIPPED hoặc IN_TRANSIT)
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.deliveryStatus IN ('SHIPPED', 'IN_TRANSIT')")
    Long countShippingOrders();
}