package com.pet.controller.web;

import com.pet.modal.request.ApplyVoucherRequestDTO;
import com.pet.modal.response.*;
import com.pet.modal.search.PetSearchRequestDTO;
import com.pet.service.CategoryService;
import com.pet.service.PetService;
import com.pet.service.PromotionService;
import com.pet.service.ReviewService;
import com.pet.service.VoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/pets")
public class PetWebController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private PetService petService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private PromotionService promotionService;

    @GetMapping()
    public ResponseEntity<PageResponse<PetForListResponseDTO>> getAllPetsWithStatusActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PetForListResponseDTO> pets = petService.getAllPetsWithStatusActive(page, size);
        if (pets == null || pets.getContent().isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(pets);
    }
    @PostMapping("/search")
    public ResponseEntity<PageResponse<PetForListResponseDTO>> searchPets(@RequestBody PetSearchRequestDTO request) {
        request.validate();
        PageResponse<PetForListResponseDTO> result = petService.advanceSearch(request);
        if (result == null || result.getContent().isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPetById(@PathVariable String id) {
        PetResponseDTO pet = petService.getPetById(id);

        if (pet == null ) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy chi tiết thú cưng thành công")
                        .data(pet)
                        .build()
        );
    }
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{petId}/reviews")
    public ResponseEntity<PageResponse<ReviewResponseDTO>> getReviewsByPetId(
            @PathVariable String petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ReviewResponseDTO> reviews = reviewService.getReviewsByPetId(petId, page, size);
        if (reviews == null || reviews.getContent().isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/vouchers/apply")
    public ResponseEntity<VoucherResponseDTO> applyVoucher(@RequestBody ApplyVoucherRequestDTO request){
        request.validate();
        VoucherResponseDTO voucher = voucherService.applyVoucher(request);
        if (voucher == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(voucher);
    }

    @GetMapping("/vouchers/{voucherCode}")
    public ResponseEntity<VoucherResponseDTO> getVoucher(@PathVariable String voucherCode){
        VoucherResponseDTO voucher = voucherService.getVoucherByCode(voucherCode);
        if (voucher == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(voucher);
    }

    @GetMapping("/promotions")
    public ResponseEntity<PageResponse<PromotionResponseDTO>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PageResponse<PromotionResponseDTO> promotions = promotionService.getAllPromotions(page, size);
        if (promotions == null || promotions.getContent().isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/promotions/{promoCode}")
    public ResponseEntity<PromotionResponseDTO> getPromotion(@PathVariable String promoCode){
        PromotionResponseDTO promotion = promotionService.getPromotionByCode(promoCode);
        if (promotion == null) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(promotion);
    }

}