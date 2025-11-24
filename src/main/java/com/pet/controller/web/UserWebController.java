package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.request.*;
import com.pet.modal.response.ApiResponse;
import com.pet.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")

public class UserWebController {

    @Autowired
    private  IUserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Lấy thông tin thành công")
                .data(userService.getUserProfile(currentUser.getUserId())).build());
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserUpdateRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Cập nhật hồ sơ thành công")
                .data(userService.updateUserProfile(currentUser.getUserId(), request)).build());
    }

}