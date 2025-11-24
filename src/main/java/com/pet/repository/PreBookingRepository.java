package com.pet.repository;

import com.pet.entity.PreBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreBookingRepository extends JpaRepository<PreBooking, String> {
    @Query("SELECT p.bookingId FROM PreBooking p ORDER BY p.bookingId DESC LIMIT 1")
    Optional<String> findLastBookingId();
    Page<PreBooking> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}