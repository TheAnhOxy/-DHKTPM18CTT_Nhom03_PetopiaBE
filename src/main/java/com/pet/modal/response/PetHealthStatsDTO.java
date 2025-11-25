package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PetHealthStatsDTO {
    private long healthyPets;      // Khỏe mạnh (Dựa trên status hoặc healthStatus)
    private long vaccinatedPets;   // Đã tiêm (Trạng thái Da_TIEM)
    private long upcomingVaccines; // Sắp tiêm (Trong 7 ngày tới)
}