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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, Instant> otpExpiry = new HashMap<>();

    private static final long OTP_TTL_SECONDS = 5 * 60; // 5 phút


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

    public void sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        otpExpiry.put(email, Instant.now().plusSeconds(OTP_TTL_SECONDS));

        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        Instant expiry = otpExpiry.get(email);

        if (storedOtp == null || expiry == null) return false;

        if (Instant.now().isAfter(expiry)) {
            otpStorage.remove(email);
            otpExpiry.remove(email);
            return false;
        }
        return storedOtp.equals(otp);
    }

    public void resetPassword(String email, String newPassword) {
        if (!otpStorage.containsKey(email)) {
            throw new RuntimeException("Bạn chưa xác thực OTP hoặc OTP đã hết hạn!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại!"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpStorage.remove(email);
        otpExpiry.remove(email);
    }
}