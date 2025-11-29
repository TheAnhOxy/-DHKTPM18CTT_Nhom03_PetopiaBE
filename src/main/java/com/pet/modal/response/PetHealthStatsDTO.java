package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetHealthStatsDTO {
    private long healthyPets;
    private long vaccinatedPets;
    private long upcomingVaccines;
}