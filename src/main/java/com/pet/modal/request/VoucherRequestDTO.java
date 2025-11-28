package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VoucherRequestDTO {
    private String voucherId;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsage;
    private String imageUrl;
    private String status;

    public void validate(){
        if(startDate != null && endDate != null && startDate.isAfter(endDate)){
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
        if(discountValue != null && discountValue <= 0){
            throw new IllegalArgumentException("discountValue must be greater than 0");
        }
        if(minOrderAmount != null && minOrderAmount < 0){
            throw new IllegalArgumentException("minOrderAmount cannot be negative");
        }
        if(maxUsage != null && maxUsage < 0){
            throw new IllegalArgumentException("maxUsage cannot be negative");
        }
        if (this.voucherId != null && this.status != null) {
            if (!"ACTIVE".equalsIgnoreCase(status) && !"INACTIVE".equalsIgnoreCase(status)) {
                throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE");
            }
        }
    }
}
