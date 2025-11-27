package com.pet.entity;

import com.pet.converter.VaccineStatusConverter;
import com.pet.enums.VaccineStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaccines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vaccin {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "vaccine_id", length = 10, nullable = false)
    private String vaccineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "vaccine_name", nullable = false, length = 255)
    private String vaccineName;

    @Column(name = "vaccine_type", length = 100)
    private String vaccineType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file", length = 255)
    private String file;

    @Convert(converter = VaccineStatusConverter.class)
    @Column(name = "status")
    private VaccineStatus status = VaccineStatus.CHUA_TIEM;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
