package com.pet.repository;

import com.pet.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("SELECT c.categoryId FROM Category c ORDER BY c.categoryId DESC LIMIT 1")
    Optional<String> findLastCategoryId();
    boolean existsByName(String name);
    @Query("SELECT c FROM Category c WHERE :keyword IS NULL OR c.name LIKE %:keyword%")
    Page<Category> searchCategories(String keyword, Pageable pageable);
}