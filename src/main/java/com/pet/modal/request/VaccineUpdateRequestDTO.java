package com.pet.modal.request;

import com.pet.enums.VaccineStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VaccineUpdateRequestDTO {
    private String vaccineName;
    private String vaccineType;
    private String description;
    private String note;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VaccineStatus status;
    private String file;
}