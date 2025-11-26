package com.pet.modal.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopSellingPetDTO {
    private String petId;
    private String petName;
    private String petImage;
    private Long totalSold; // Số lượng bán (Long)
    private Double revenue; // Doanh thu (Double)

    public TopSellingPetDTO(String petId, String petName, String petImage, Long totalSold, Double revenue) {
        this.petId = petId;
        this.petName = petName;
        this.petImage = petImage;
        this.totalSold = totalSold;
        this.revenue = revenue;
    }
}