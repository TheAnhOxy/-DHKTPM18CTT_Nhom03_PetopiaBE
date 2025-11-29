package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/general")
    public ResponseEntity<ApiResponse> getGeneralStats() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê tổng quan thành công")
                .data(dashboardService.getGeneralStats())
                .build());
    }

    @GetMapping("/top-users")
    public ResponseEntity<ApiResponse> getTopUsers() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy Top 5 khách hàng VIP")
                .data(dashboardService.getTopSpendingUsers())
                .build());
    }

    @GetMapping("/top-selling")
    public ResponseEntity<ApiResponse> getTopSelling() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy Top 10 thú cưng bán chạy")
                .data(dashboardService.getTopSellingPets())
                .build());
    }

    @GetMapping("/health-chart")
    public ResponseEntity<ApiResponse> getHealthChart() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy dữ liệu biểu đồ sức khỏe")
                .data(dashboardService.getPetHealthStats())
                .build());
    }

    // 1. API Thống kê chính (Box Dashboard)
    @GetMapping("/main-stats")
    public ResponseEntity<ApiResponse> getMainStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Validate cơ bản: Ngày bắt đầu không được sau ngày kết thúc
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .status(400)
                    .message("Ngày bắt đầu không được lớn hơn ngày kết thúc")
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê chính thành công")
                .data(dashboardService.getMainStats(startDate, endDate))
                .build());
    }
    // 2. API Biểu đồ (Chart Line/Bar)
    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse> getRevenueChart(@RequestParam(required = false) Integer year) {
        // Nếu không truyền năm, lấy năm hiện tại
        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy dữ liệu biểu đồ doanh thu năm " + targetYear)
                .data(dashboardService.getMonthlyRevenueChart(targetYear))
                .build());
    }

    // 3. API Thống kê trạng thái đơn (Chart Pie hoặc List)
    @GetMapping("/order-status")
    public ResponseEntity<ApiResponse> getOrderStatusStats() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê trạng thái đơn hàng")
                .data(dashboardService.getOrderStatusStats())
                .build());
    }
}