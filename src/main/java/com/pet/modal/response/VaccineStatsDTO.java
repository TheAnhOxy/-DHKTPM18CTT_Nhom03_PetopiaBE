package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VaccineStatsDTO {
    private long totalSchedules;
    private long completedSchedules;
    private long upcomingSchedules;
    private long petsNeedVaccination;
}