package com.pet.modal.request;
import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private String petId;
    private Integer quantity;
}