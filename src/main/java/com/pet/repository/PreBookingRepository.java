package com.pet.repository;

import com.pet.entity.PreBooking;
import com.pet.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PreBookingRepository extends JpaRepository<PreBooking, String> {
    @Query("SELECT p.bookingId FROM PreBooking p ORDER BY p.bookingId DESC LIMIT 1")
    Optional<String> findLastBookingId();
    Page<PreBooking> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT p FROM PreBooking p WHERE p.status = 'CONFIRMED' AND p.updatedAt < :cutoffTime")
    List<PreBooking> findExpiredConfirmedBookings(LocalDateTime cutoffTime);

    @Query("SELECT p FROM PreBooking p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:keyword IS NULL OR p.pet.name LIKE %:keyword% OR p.user.fullName LIKE %:keyword%)")
    Page<PreBooking> searchBookings(BookingStatus status, String keyword, Pageable pageable);
}