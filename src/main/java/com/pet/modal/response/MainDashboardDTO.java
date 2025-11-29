package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainDashboardDTO {
    // Doanh thu (Chỉ tính đơn thành công - DELIVERED)
    private Double revenueToday;
    private Double revenueThisWeek;
    private Double revenueThisMonth;

    // Số lượng đơn
    private Long totalOrders;       // Tổng đơn hàng đã đặt
    private Long totalPreBookings;  // Tổng đơn đặt trước (Pre-booking)
    private Long cancelledOrders;   // Tổng đơn đã hủy
    private Double totalRevenue;
}