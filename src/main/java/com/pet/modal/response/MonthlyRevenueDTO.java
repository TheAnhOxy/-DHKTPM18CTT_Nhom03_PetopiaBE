package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDTO {
    private Integer month;
    private Integer year;
    private Double revenue; // Doanh thu
    private Double profit;  // Lợi nhuận (Giả định hoặc tính toán)
}