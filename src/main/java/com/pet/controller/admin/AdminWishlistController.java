package com.pet.controller.admin;

import com.pet.entity.User;
import com.pet.modal.response.ApiResponse;
import com.pet.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/wishlists")
public class AdminWishlistController {

    @Autowired
    private WishlistService wishlistService;
    
    @GetMapping("/admin/top-stats")
    public ResponseEntity<ApiResponse> getTopStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Thống kê Top yêu thích")
                .data(wishlistService.getTopFavoritedPets(page, size))
                .build());
    }


}
