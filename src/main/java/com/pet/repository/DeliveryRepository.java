package com.pet.repository;

import com.pet.entity.Delivery;
import com.pet.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {

    // Sửa lại query: Gọi thẳng đến com.pet.enums.DeliveryStatus.TEN_ENUM
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.deliveryStatus IN (com.pet.enums.DeliveryStatus.SHIPPED, com.pet.enums.DeliveryStatus.IN_TRANSIT)")
    Long countShippingOrders();

    @Query("SELECT d FROM Delivery d JOIN d.order o JOIN o.user u " +
            "WHERE LOWER(d.trackingNumber) LIKE LOWER(:term) " +
            "OR LOWER(o.phoneNumber) LIKE LOWER(:term) " +
            "OR LOWER(u.fullName) LIKE LOWER(:term)")
    Page<Delivery> searchDeliveries(@Param("term") String term, Pageable pageable);
}