package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.service.DashboardOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard-overview") // Endpoint riêng biệt
public class DashboardOverviewController {

    @Autowired
    private DashboardOverviewService dashboardOverviewService;

    // 1. Tổng quan (Box số liệu đầu trang)
    @GetMapping("/general")
    public ResponseEntity<ApiResponse> getOverview() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy dữ liệu tổng quan thành công")
                .data(dashboardOverviewService.getOverviewStats())
                .build());
    }

    // 2. Hoạt động gần đây (Đơn hàng + Lịch tiêm)
    @GetMapping("/activities")
    public ResponseEntity<ApiResponse> getActivities() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy hoạt động gần đây thành công")
                .data(dashboardOverviewService.getRecentActivities())
                .build());
    }

    // 3. Tương tác (Like + Comment)
    @GetMapping("/social")
    public ResponseEntity<ApiResponse> getSocialStats() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Lấy thống kê tương tác thành công")
                .data(dashboardOverviewService.getSocialStats())
                .build());
    }
}