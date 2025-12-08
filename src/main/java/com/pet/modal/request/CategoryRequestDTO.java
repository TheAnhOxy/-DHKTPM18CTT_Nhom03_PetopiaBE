package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    private String categoryId;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    private String parentId;

    private String imageUrl;
}