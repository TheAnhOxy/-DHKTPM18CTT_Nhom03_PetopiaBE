package com.pet.modal.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherSearchRequestDTO {
    private String keyword;
    private String status;
    private String discountType;
    private Integer page;
    private Integer size;
}
