package com.pet.modal.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PetDTO {
    private String petId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String categoryId;
    private String categoryName;
    private String furType;
    private String color;
    private String status;
}
