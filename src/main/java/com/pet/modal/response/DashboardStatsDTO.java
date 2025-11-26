package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private Long totalSoldPets;        // Tổng thú cưng đã bán
    private Long shippingOrders;       // Đơn hàng đang vận chuyển
    private Long scheduledVaccines;    // Lịch tiêm đã lên
    private Double totalRevenue;       // Tổng doanh thu
}