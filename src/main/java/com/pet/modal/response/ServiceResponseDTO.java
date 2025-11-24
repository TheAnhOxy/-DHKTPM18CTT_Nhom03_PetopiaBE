package com.pet.modal.response;

import lombok.Data;

@Data
public class ServiceResponseDTO {
    private String serviceId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
}