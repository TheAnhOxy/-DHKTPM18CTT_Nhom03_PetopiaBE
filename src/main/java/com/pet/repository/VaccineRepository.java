package com.pet.repository;

import com.pet.entity.Vaccin;
import com.pet.enums.VaccineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccin, String> {
    @Query("SELECT v.vaccineId FROM Vaccin v ORDER BY v.vaccineId DESC LIMIT 1")
    Optional<String> findLastVaccineId();

    long countByStatus(VaccineStatus status);

    @Query("SELECT COUNT(v) FROM Vaccin v WHERE v.status = 'CHUA_TIEM' AND v.startDate BETWEEN :start AND :end")
    long countUpcoming(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT v.pet.petId) FROM Vaccin v WHERE v.status = 'CHUA_TIEM'")
    long countPetsNeedVaccination();

    // Đếm thú cưng đã tiêm (Distinct Pet)
    @Query("SELECT COUNT(DISTINCT v.pet.petId) FROM Vaccin v WHERE v.status = 'Da_TIEM'")
    long countVaccinatedPets();

    // Đếm thú cưng sắp tiêm (Trong 7 ngày tới)
    // Lưu ý: Logic này đếm LỊCH sắp tới
    @Query("SELECT COUNT(v) FROM Vaccin v WHERE v.status = 'CHUA_TIEM' AND v.startDate BETWEEN CURRENT_TIMESTAMP AND :nextWeek")
    long countUpcomingVaccines(java.time.LocalDateTime nextWeek);

}