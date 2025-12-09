package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.SystemPerformanceDTO;
import com.pet.logging.ErrorLogStore;
import com.pet.service.SystemPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin/performance")
@RequiredArgsConstructor
public class SystemPerformanceController {

    private final SystemPerformanceService systemPerformanceService;
    private final ErrorLogStore errorLogStore;

    @GetMapping("/system")
    public ResponseEntity<ApiResponse> getSystemPerformance() {
        SystemPerformanceDTO data = systemPerformanceService.getSystemPerformance();
        ApiResponse response = ApiResponse.builder()
                .status(200)
                .message("System performance metrics")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/endpoints")
    public ResponseEntity<ApiResponse> getEndpointPerformance() {
        var data = systemPerformanceService.getEndpointPerformance();
        ApiResponse response = ApiResponse.builder()
                .status(200)
                .message("Endpoint performance metrics")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/http-metrics")
    public ResponseEntity<ApiResponse> getRawHttpMetrics() {
        var data = systemPerformanceService.getRawHttpMetrics();
        ApiResponse response = ApiResponse.builder()
                .status(200)
                .message("Raw http.server.requests metrics")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/errors/recent")
    public ResponseEntity<ApiResponse> getRecentErrors(@RequestParam(name = "limit", defaultValue = "20") int limit) {
        if (limit <= 0) {
            limit = 1;
        }
        var data = errorLogStore.getRecent(limit);
        ApiResponse response = ApiResponse.builder()
                .status(200)
                .message("Recent errors in memory")
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }
}



