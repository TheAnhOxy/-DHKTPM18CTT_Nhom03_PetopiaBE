package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponseDTO {
    private String voucherId;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxUsage;
    private Integer usedCount;
    private String imageUrl;
    private String status;
}
