package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}