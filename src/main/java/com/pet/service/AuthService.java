package com.pet.service;

import com.pet.entity.User;
import com.pet.enums.UserRole;
import com.pet.modal.request.LoginRequestDTO;
import com.pet.modal.request.RegisterRequestDTO;
import com.pet.modal.response.LoginResponseDTO;
import com.pet.repository.UserRepository;
import com.pet.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;


    public LoginResponseDTO register(RegisterRequestDTO request) {
        int index = userRepository.findAll().size() + 1;
        String randomId = "U" + String.format("%03d", index);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User user = User.builder()
                .userId(randomId)
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
        try {
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu user: " + e.getMessage());
        }


        var jwtToken = jwtUtils.generateToken(user);
        return LoginResponseDTO.builder()
                .accessToken(jwtToken)
                .user(user)
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        System.out.println("Dang nhap user: " + request.getIdentifier());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );
        System.out.println("Dang nhap thanh cong");
        var user = userRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow();

        var token =  jwtUtils.generateToken(user);

        return LoginResponseDTO.builder()
                .accessToken(token)
                .user(user)
                .build();
    }
}