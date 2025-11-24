package com.pet.controller.auth;

import com.pet.modal.request.LoginRequestDTO;
import com.pet.modal.request.RegisterRequestDTO;
import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.LoginResponseDTO;
import com.pet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


}