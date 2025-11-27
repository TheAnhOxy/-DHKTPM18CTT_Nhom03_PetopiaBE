package com.pet.service.impl;

import com.pet.converter.DeliveryConverter;
import com.pet.entity.Delivery;
import com.pet.modal.response.DeliveryResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.DeliveryRepository;
import com.pet.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryConverter deliveryConverter;

    @Override
    public PageResponse<DeliveryResponseDTO> getAllDeliveries(int page, int size) {
        Pageable pageable = PageRequest.of(0, 9, Sort.by("createdAt").descending());
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
}
