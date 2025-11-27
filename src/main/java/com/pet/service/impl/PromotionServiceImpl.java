package com.pet.service.impl;

import com.pet.converter.PromotionConverter;
import com.pet.entity.Promotion;
import com.pet.enums.PromotionType;
import com.pet.enums.PromotionVoucherStatus;
import com.pet.modal.request.PromotionRequestDTO;
import com.pet.modal.request.PromotionSearchRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PromotionResponseDTO;
import com.pet.repository.PromotionRepository;
import com.pet.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {
    @Autowired
    private PromotionRepository promotionRepository;
    @Autowired
    private PromotionConverter promotionConverter;

    @Override
    public PromotionResponseDTO getPromotionByCode(String promoCode) {
        return promotionRepository.findByCode(promoCode)
                .map(promotionConverter::mapToDTO)
                .orElse(null);
    }

    @Override
    public PageResponse<PromotionResponseDTO> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<PromotionResponseDTO> promotionDTOs = promotionPage.getContent().stream()
                .map(promotionConverter::mapToDTO)
                .toList();

        PageResponse<PromotionResponseDTO> response = new PageResponse<>();
        response.setContent(promotionDTOs);
        response.setTotalElements(promotionPage.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        return response;
    }

    @Override
    @Transactional
    public PromotionResponseDTO addOrUpdatePromotion(PromotionRequestDTO request) {
        Promotion promotion;
        if (request.getPromotionId() != null) {
            promotion = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + request.getPromotionId()));
        } else {
            promotionRepository.findByCode(request.getCode())
                    .ifPresent(p -> { throw new RuntimeException("Promotion code already exists: " + request.getCode()); });

            promotion = new Promotion();
            promotion.setCreatedAt(LocalDateTime.now());
        }
        promotion = promotionConverter.mapToEntity(request, promotion);
        Promotion savedPromotion = promotionRepository.save(promotion);
        return promotionConverter.mapToDTO(savedPromotion);
    }

    @Override
    public PromotionResponseDTO inactivePromotion(String promoId) {
        Promotion promotion = promotionRepository.findById(promoId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promoId));
        promotion.setStatus(PromotionVoucherStatus.INACTIVE);
        Promotion updatedPromotion = promotionRepository.save(promotion);
        return promotionConverter.mapToDTO(updatedPromotion);
    }

    @Override
    public PageResponse<PromotionResponseDTO> searchPromotions(PromotionSearchRequestDTO req) {
        int page = req.getPage() != null ? req.getPage() : 0;
        int size = req.getSize() != null && req.getSize() > 0 ? req.getSize() : 9;
        Pageable pageable = PageRequest.of(page, size);

        String kw = req.getKeyword() != null && !req.getKeyword().isBlank()
                ? "%" + req.getKeyword().trim().toLowerCase() + "%" : null;

        String cat = req.getCategoryId() != null && !"all".equalsIgnoreCase(req.getCategoryId())
                ? req.getCategoryId() : null;

        PromotionVoucherStatus statusEnum = null;
        if (req.getStatus() != null && !"all".equalsIgnoreCase(req.getStatus())) {
            statusEnum = "active".equalsIgnoreCase(req.getStatus()) ? PromotionVoucherStatus.ACTIVE
                    : PromotionVoucherStatus.INACTIVE;
        }

        PromotionType typeEnum = null;
        if (req.getType() != null && !"all".equalsIgnoreCase(req.getType())) {
            typeEnum = switch (req.getType().toUpperCase()) {
                case "DISCOUNT"  -> PromotionType.DISCOUNT;
                case "FREESHIP"  -> PromotionType.FREESHIP;
                case "CASHBACK"  -> PromotionType.CASHBACK;
                case "BUNDLE"    -> PromotionType.BUNDLE;
                default          -> null;
            };
        }

        Page<Promotion> result = promotionRepository.searchPromotions(kw, cat, statusEnum, typeEnum, pageable);

        List<PromotionResponseDTO> content = result.getContent().stream()
                .map(promotionConverter::mapToDTO)
                .toList();

        PageResponse<PromotionResponseDTO> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalElements(result.getTotalElements());
        response.setPage(page);
        response.setSize(size);
        return response;
    }
}
