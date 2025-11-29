package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewDTO {
    private long newPets;           // Thú cưng mới (thêm trong tháng này)
    private long newCustomers;      // Khách hàng mới (đăng ký tháng này)
    private Double totalRevenue;    // Tổng doanh thu (đã có từ trước)
    private long ordersProcessing;  // Đơn đang xử lý (Pending, Confirmed, Shipped)
    private long newReviews;        // Bình luận mới (trong tháng)
}