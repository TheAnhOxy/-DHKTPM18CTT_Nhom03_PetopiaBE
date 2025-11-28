package com.pet.repository;

import com.pet.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    @Query("SELECT s.serviceId FROM Service s ORDER BY s.serviceId DESC LIMIT 1")
    Optional<String> findLastServiceId();
    boolean existsByName(String name);

    // Tìm kiếm theo tên hoặc mô tả
    @Query("SELECT s FROM Service s WHERE :keyword IS NULL OR s.name LIKE %:keyword% OR s.description LIKE %:keyword%")
    Page<Service> searchServices(String keyword, Pageable pageable);
}