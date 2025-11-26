package com.pet.service;

import com.pet.modal.request.PromotionRequestDTO;
import com.pet.modal.request.PromotionSearchRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PromotionResponseDTO;

public interface PromotionService {
    PromotionResponseDTO getPromotionByCode(String promoCode);
    PageResponse<PromotionResponseDTO> getAllPromotions(int page, int size);
    PromotionResponseDTO addOrUpdatePromotion(PromotionRequestDTO request);
    PromotionResponseDTO inactivePromotion(String promoId);

    PageResponse<PromotionResponseDTO> searchPromotions(PromotionSearchRequestDTO req);
}
