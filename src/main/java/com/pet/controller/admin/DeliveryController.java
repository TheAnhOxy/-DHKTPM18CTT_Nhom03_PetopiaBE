package com.pet.controller.admin;

import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.DeliveryResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/deliveries")
public class DeliveryController {
    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    public ResponseEntity<PageResponse<DeliveryResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(deliveryService.getAllDeliveries(page, size));
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryResponseDTO> getDeliveryById(@PathVariable String deliveryId) {
        DeliveryResponseDTO delivery = deliveryService.getDeliveryById(deliveryId);
        if (delivery == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(delivery);
    }

    @PutMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryResponseDTO> updateStatus(
            @PathVariable String deliveryId,
            @RequestParam String status) {

        DeliveryResponseDTO updated = deliveryService.updateDeliveryStatus(deliveryId, status);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<DeliveryResponseDTO>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {

        PageResponse<DeliveryResponseDTO> result = deliveryService.searchDeliveries(query, status, page, size);
        return ResponseEntity.ok(result);
    }
}
