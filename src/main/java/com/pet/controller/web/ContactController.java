package com.pet.controller.web;
import com.pet.modal.request.ContactRequestDTO;
import com.pet.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> submitContactForm(@Valid @RequestBody ContactRequestDTO request) {
        System.out.println(request);
        emailService.sendContactNotification(request);
        return ResponseEntity.ok("Cảm ơn bạn đã liên hệ. Chúng tôi sẽ phản hồi sớm nhất!");
    }
}