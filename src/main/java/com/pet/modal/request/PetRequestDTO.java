package com.pet.modal.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PetRequestDTO {

    private String petId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String categoryId;

    @PositiveOrZero(message = "Age must be >= 0")
    private Integer age;

    private String gender;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be >= 0")
    private Double price;

    @PositiveOrZero(message = "Discount price must be >= 0")
    private Double discountPrice;

    private String healthStatus;
    private String vaccinationHistory;

    @Min(value = 1, message = "Stock quantity must be at least 1")
    private Integer stockQuantity;

    private String status;

    private String videoUrl;

    @PositiveOrZero(message = "Weight must be >= 0")
    private Double weight;

    @PositiveOrZero(message = "Height must be >= 0")
    private Double height;

    private String color;
    private String furType;
    private List<PetImageDTO> oldImages;
//    private List<PetImageDTO> images;
    private List<MultipartFile> files;
}
