package com.pet.repository;

import com.pet.entity.Pet;
import com.pet.enums.PetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, String>, JpaSpecificationExecutor<Pet> {
    Page<Pet> findByCategory_CategoryId(String categoryId, Pageable pageable);
    Page<Pet> findAllByStatus(PetStatus status, Pageable pageable);
    long count();

    // Đếm số lượng pet có status AVAILABLE (Khỏe mạnh/Sẵn sàng bán)
    @Query("SELECT COUNT(p) FROM Pet p WHERE p.status = 'AVAILABLE'")
    long countHealthyPets();

    // Query lấy ID thú cưng lớn nhất (Ví dụ: P005)
    @Query("SELECT p.petId FROM Pet p ORDER BY p.petId DESC LIMIT 1")
    Optional<String> findMaxPetId();

    // Đếm thú cưng mới thêm trong khoảng thời gian
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


    // AI
    @Query("SELECT p FROM Pet p JOIN p.category c WHERE " +
            "p.status = 'AVAILABLE' AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR p.description LIKE CONCAT('%', :keyword, '%') " + // Đã sửa dòng này
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Pet> searchForChat(@Param("keyword") String keyword, @Param("maxPrice") Double maxPrice, Pageable pageable);
    List<Pet> findTop5ByOrderByPriceAsc();
}
