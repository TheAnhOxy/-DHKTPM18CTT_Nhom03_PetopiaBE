package com.pet.modal.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchRequestDTO {
    private String keyword;
    private String categoryId;
    private String status;
    private String type;
    private Integer page;
    private Integer size;
}
