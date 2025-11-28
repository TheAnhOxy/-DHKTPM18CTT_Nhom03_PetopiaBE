package com.pet.repository;

import com.pet.entity.Vaccin;
import com.pet.enums.VaccineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccin, String> {

    @Query("SELECT v.vaccineId FROM Vaccin v ORDER BY v.vaccineId DESC LIMIT 1")
    Optional<String> findLastVaccineId();

    // Đã sửa: Dùng tham số Enum trực tiếp
    long countByStatus(VaccineStatus status);

    // Đã sửa: Gọi full path của Enum trong query để tránh lỗi type mismatch
    @Query("SELECT COUNT(v) FROM Vaccin v WHERE v.status = com.pet.enums.VaccineStatus.CHUA_TIEM AND v.startDate BETWEEN :start AND :end")
    long countUpcoming(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT v.pet.petId) FROM Vaccin v WHERE v.status = com.pet.enums.VaccineStatus.CHUA_TIEM")
    long countPetsNeedVaccination();

    @Query("SELECT COUNT(DISTINCT v.pet.petId) FROM Vaccin v WHERE v.status = com.pet.enums.VaccineStatus.Da_TIEM")
    long countVaccinatedPets();

    @Query("SELECT COUNT(v) FROM Vaccin v WHERE v.status = com.pet.enums.VaccineStatus.CHUA_TIEM AND v.startDate BETWEEN CURRENT_TIMESTAMP AND :nextWeek")
    long countUpcomingVaccines(LocalDateTime nextWeek);

    @Query("SELECT v FROM Vaccin v WHERE v.pet.petId = :petId ORDER BY v.startDate DESC")
    List<Vaccin> findByPetId(String petId);
}