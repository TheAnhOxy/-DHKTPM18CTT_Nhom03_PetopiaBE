package com.pet.repository;

import com.pet.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    @Query("SELECT s.serviceId FROM Service s ORDER BY s.serviceId DESC LIMIT 1")
    Optional<String> findLastServiceId();
    boolean existsByName(String name);
}