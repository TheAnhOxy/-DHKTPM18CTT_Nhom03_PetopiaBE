package com.pet.repository;

import com.pet.entity.Pet;
import com.pet.enums.PetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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
}
