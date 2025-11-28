package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PromotionRequestDTO {
    private String promotionId;
    private String code;
    private String description;
    private String promotionType;
    private Double discountValue;
    private Double minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsage;
    private String categoryId;
    private String imageUrl;
    private String status;

    public void validate() {
        if(startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("StartDate cannot be after endDate");
        }
        if(discountValue != null && discountValue < 0) {
            throw new IllegalArgumentException("DiscountValue cannot be negative");
        }
        if(minOrderAmount != null && minOrderAmount < 0) {
            throw new IllegalArgumentException("MinOrderAmount cannot be negative");
        }
        if(maxUsage != null && maxUsage < 0) {
            throw new IllegalArgumentException("MaxUsage cannot be negative");
        }
        if (categoryId != null && categoryId.isEmpty()) {
            throw new IllegalArgumentException("CategoryId cannot be empty if provided");
        }
        if (this.promotionId != null && this.status != null) {
            if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
            }
        }
    }
}
