package com.pet.controller.web;

import com.pet.entity.User;
import com.pet.modal.request.*;
import com.pet.modal.response.ApiResponse;
import com.pet.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserWebController {

    @Autowired
    private IUserService userService;

    // --- PROFILE SECTION ---

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Lấy thông tin thành công")
                .data(userService.getUserProfile(currentUser.getUserId())).build());
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @ModelAttribute UserUpdateRequestDTO request,
            @RequestPart(value = "file", required = false) MultipartFile avatarFile
    ) throws IOException {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Cập nhật hồ sơ thành công")
                .data(userService.updateUserProfile(currentUser.getUserId(), request, avatarFile))
                .build());
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody ChangePasswordRequestDTO request) {
        userService.changePassword(currentUser.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Đổi mật khẩu thành công").build());
    }

    // --- ADDRESS SECTION (Đã bổ sung đầy đủ) ---

    // 1. Lấy danh sách (Sửa URL từ /me/addresses thành /addresses cho khớp FE)
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse> getMyAddresses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(userService.getMyAddresses(currentUser.getUserId())).build());
    }

    // 2. Thêm địa chỉ
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse> addAddress(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(201).message("Thêm địa chỉ thành công")
                .data(userService.addAddress(currentUser.getUserId(), request)).build());
    }

    // 3. Cập nhật địa chỉ (Bổ sung cái này)
    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse> updateAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId,
            @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Cập nhật địa chỉ thành công")
                .data(userService.updateAddress(currentUser.getUserId(), addressId, request)).build());
    }

    // 4. Xóa địa chỉ (Bổ sung cái này)
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse> deleteAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId) {
        userService.deleteAddress(currentUser.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Xóa địa chỉ thành công").build());
    }

    // 5. Đặt địa chỉ mặc định (Bổ sung cái này)
    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse> setDefaultAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId) {
        userService.setDefaultAddress(currentUser.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Đã đặt làm địa chỉ mặc định").build());
    }
}