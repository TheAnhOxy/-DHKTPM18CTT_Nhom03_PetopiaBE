package com.pet.controller.admin;

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
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse> getMyAddresses(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .data(userService.getMyAddresses(currentUser.getUserId())).build());
    }

    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse> addAddress(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(201).message("Thêm địa chỉ thành công")
                .data(userService.addAddress(currentUser.getUserId(), request)).build());
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse> updateAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId,
            @RequestBody AddressRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200).message("Cập nhật địa chỉ thành công")
                .data(userService.updateAddress(currentUser.getUserId(), addressId, request)).build());
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<ApiResponse> deleteAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId) {
        userService.deleteAddress(currentUser.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.builder().status(200).message("Xóa địa chỉ thành công").build());
    }

    @PutMapping("/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse> setDefaultAddress(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String addressId) {
        userService.setDefaultAddress(currentUser.getUserId(), addressId);
        return ResponseEntity.ok(ApiResponse.builder().status(200).message("Đã đặt làm địa chỉ mặc định").build());
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse> saveUser(@RequestBody UserSaveRequestDTO request) {
        var result = userService.saveUser(request);
        String action = (request.getUserId() == null || request.getUserId().isEmpty())
                ? "Tạo mới" : "Cập nhật";

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message(action + " người dùng thành công")
                        .data(result)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = userService.getAllUsers(page, size);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Lấy danh sách người dùng thành công")
                        .data(result)
                        .build()
        );
    }

}