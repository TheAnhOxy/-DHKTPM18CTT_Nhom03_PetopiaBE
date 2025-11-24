package com.pet.controller.admin;

import com.pet.modal.request.ServiceRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.service.ServiceManagement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/services")
public class AdminServiceController {

    @Autowired
    private ServiceManagement serviceManagement;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).data(serviceManagement.getAllServices(page, size)).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createService(@Valid @RequestBody ServiceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder()
                        .status(201).message("Thêm dịch vụ thành công")
                        .data(serviceManagement.createService(request)).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateService(
            @PathVariable String id,
            @RequestBody ServiceRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Cập nhật dịch vụ thành công")
                .data(serviceManagement.updateService(id, request)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteService(@PathVariable String id) {
        serviceManagement.deleteService(id);
        return ResponseEntity.ok(ApiResponse.builder().status(200).message("Xóa dịch vụ thành công").build());
    }
}