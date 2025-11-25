package com.pet.repository;

import com.pet.entity.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, String> {
    @Query("SELECT b.bookingServiceId FROM BookingService b ORDER BY b.bookingServiceId DESC LIMIT 1")
    Optional<String> findLastBookingId();
    Page<BookingService> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT b FROM BookingService b WHERE " +
            "(:keyword IS NULL OR b.service.name LIKE %:keyword% OR b.user.fullName LIKE %:keyword%)")
    Page<BookingService> searchBookings(String keyword, Pageable pageable);
}