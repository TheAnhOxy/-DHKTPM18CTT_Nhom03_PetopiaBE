package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    private String parentId;
    private String parentName;
}
