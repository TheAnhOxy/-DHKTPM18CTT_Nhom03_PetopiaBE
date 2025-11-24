package com.pet.controller.admin;

import com.pet.modal.request.PromotionRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PromotionResponseDTO;
import com.pet.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/promotions")
public class PromotionController {
    @Autowired
    private PromotionService promotionService;

    @GetMapping("/{promoCode}")
    public ResponseEntity<PromotionResponseDTO> getPromotion(@PathVariable String promoCode){
        PromotionResponseDTO promotion = promotionService.getPromotionByCode(promoCode);
        if (promotion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(promotion);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResponse<PromotionResponseDTO>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(promotionService.getAllPromotions(page, size));
    }

    @PostMapping
    public ResponseEntity<PromotionResponseDTO> addOrUpdatePromotion(@RequestBody PromotionRequestDTO request) {
        request.validate();
        PromotionResponseDTO promotion = promotionService.addOrUpdatePromotion(request);
        if (request.getPromotionId() == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(promotion);
        }
        return ResponseEntity.ok(promotion);
    }

    @PutMapping("/{id}/inactive")
    public ResponseEntity<PromotionResponseDTO> inactivePromotion(@PathVariable String id) {
        PromotionResponseDTO promotion = promotionService.inactivePromotion(id);
        if (promotion == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(promotion);
    }

}
