package com.pet.service;

import com.pet.modal.response.DeliveryResponseDTO;
import com.pet.modal.response.PageResponse;

public interface DeliveryService {
    PageResponse<DeliveryResponseDTO> getAllDeliveries(int page, int size);

    DeliveryResponseDTO getDeliveryById(String deliveryId);

    DeliveryResponseDTO updateDeliveryStatus(String deliveryId, String status);

    PageResponse<DeliveryResponseDTO> searchDeliveries(String query, String status, int page, int size);
}
