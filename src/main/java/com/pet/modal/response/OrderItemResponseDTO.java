package com.pet.modal.response;
import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private String petId;
    private String petName;
    private String petImage;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
}