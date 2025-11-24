package com.pet.modal.response;

import com.pet.enums.VaccineStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VaccineResponseDTO {
    private String vaccineId;
    private String petName;
    private String petImage;
    private String ownerName;
    private String vaccineName;
    private String vaccineType;
    private VaccineStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private String note;
    private String file;
}