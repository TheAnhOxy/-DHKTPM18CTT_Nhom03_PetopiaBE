package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopFavoritedPetDTO {
    private String petId;
    private String petName;
    private Double price;
    private Long totalLikes; // Số lượng tim
}