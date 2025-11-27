package com.pet.service.impl;

import com.pet.converter.DeliveryConverter;
import com.pet.entity.Delivery;
import com.pet.entity.DeliveryHistory;
import com.pet.enums.DeliveryStatus;
import com.pet.modal.response.DeliveryResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.DeliveryHistoryRepository;
import com.pet.repository.DeliveryRepository;
import com.pet.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryConverter deliveryConverter;

    @Autowired
    private DeliveryHistoryRepository deliveryHistoryRepository;

    @Override
    public PageResponse<DeliveryResponseDTO> getAllDeliveries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Delivery> deliveryPage = deliveryRepository.findAll(pageable);

        List<DeliveryResponseDTO> content = deliveryPage.getContent().stream()
                .map(deliveryConverter::toResponseDTO)
                .toList();

        PageResponse<DeliveryResponseDTO> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalElements(deliveryPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    @Override
    public DeliveryResponseDTO getDeliveryById(String deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with id: " + deliveryId));
        return deliveryConverter.toResponseDTO(delivery);
    }

    @Override
    public DeliveryResponseDTO updateDeliveryStatus(String deliveryId, String status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + deliveryId));

        DeliveryStatus deliveryStatus;
        try {
            deliveryStatus = DeliveryStatus.valueOf(status); // "PREPARING" → enum
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ: " + status);
        }

        // Tạo lịch sử mới
        DeliveryHistory history = new DeliveryHistory();
        history.setHistoryId(generateHistoryId());
        history.setDelivery(delivery);
        history.setStatus(deliveryStatus);
        history.setUpdatedAt(LocalDateTime.now());

        deliveryHistoryRepository.save(history);

        // Cập nhật trạng thái hiện tại
        delivery.setDeliveryStatus(deliveryStatus);
        deliveryRepository.save(delivery);

        return deliveryConverter.toResponseDTO(delivery);
    }

    private String generateHistoryId() {
        return "DH%03d".formatted(deliveryHistoryRepository.count() + 1);
    }

}
