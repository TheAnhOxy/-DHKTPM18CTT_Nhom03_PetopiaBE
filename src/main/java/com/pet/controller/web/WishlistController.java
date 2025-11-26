package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.response.ApiResponse;
import com.pet.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    //  Toggle tim (Thêm/Xóa)
    @PostMapping("/toggle/{petId}")
    public ResponseEntity<ApiResponse> toggleWishlist(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String petId) {

        String action = wishlistService.toggleWishlist(currentUser.getUserId(), petId);
        String message = action.equals("Added") ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích";

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message(message)
                .data(action) // FE dựa vào đây (Added/Removed) để render màu nút tim
                .build());
    }

    //  User: Xem danh sách của mình
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyWishlist(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(wishlistService.getMyWishlist(currentUser.getUserId(), page, size))
                .build());
    }


}