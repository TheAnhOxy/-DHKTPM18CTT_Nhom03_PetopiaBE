package com.pet.modal.response;

import com.pet.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponseDTO {
    private String promotionId;
    private String code;
    private String description;
    private String promotionType;
    private Double discountValue;
    private Double minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsage;
    private Integer usedCount;
    private String categoryId;
    private String imageUrl;
    private String status;
}
