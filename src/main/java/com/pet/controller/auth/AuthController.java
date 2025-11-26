package com.pet.controller.auth;

import com.pet.modal.request.LoginRequestDTO;
import com.pet.modal.request.RegisterRequestDTO;
import com.pet.modal.request.ResetPasswordRequest;
import com.pet.modal.request.VerifyOtpRequest;
import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.LoginResponseDTO;
import com.pet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(
            @RequestBody RegisterRequestDTO request
    ) {

        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(
            @RequestBody LoginRequestDTO request
    ) {
        System.out.println("Controller nhan dang nhap user: " + request.getIdentifier());
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        service.sendOtp(email);
        return ResponseEntity.ok("OTP đã được gửi vào email!");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        boolean ok = service.verifyOtp(req.getEmail(), req.getOtp());
        return ok ? ResponseEntity.ok("OTP hợp lệ")
                : ResponseEntity.badRequest().body("OTP không đúng!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        service.resetPassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok("Đặt lại mật khẩu thành công!");
    }
}