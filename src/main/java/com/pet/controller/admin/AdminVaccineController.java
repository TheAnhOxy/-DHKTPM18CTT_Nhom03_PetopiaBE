package com.pet.controller.admin;

import com.pet.modal.request.VaccineBatchCreateRequestDTO;
import com.pet.modal.request.VaccineUpdateRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.VaccineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vaccines")
public class AdminVaccineController {

    @Autowired
    private VaccineService vaccineService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllVaccines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).data(vaccineService.getAllVaccines(page, size)).build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getStats() {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Lấy thống kê thành công")
                .data(vaccineService.getVaccineStats()).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createBatch(@Valid @RequestBody VaccineBatchCreateRequestDTO request) {
        var result = vaccineService.createVaccineBatch(request);

        // Response
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201)
                        .message("Đã tạo lịch tiêm và gửi thông báo cho khách hàng")
                        .data(result)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateVaccine(
            @PathVariable String id,
            @RequestBody VaccineUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Cập nhật thành công")
                .data(vaccineService.updateVaccine(id, request)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVaccine(@PathVariable String id) {
        vaccineService.deleteVaccine(id);
        return ResponseEntity.ok(ApiResponse.builder().status(200).message("Đã xóa lịch tiêm").build());
    }
}