package com.pet.controller;

import com.pet.modal.response.ApiResponse;
import com.pet.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private AiService aiService;

    @PostMapping
    public ResponseEntity<ApiResponse> chat(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.builder()
                            .status(400)
                            .message("Câu hỏi không được để trống")
                            .build()
            );
        }

        String response = aiService.chat(message);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Phản hồi từ Gemini AI")
                        .data(response)
                        .build()
        );
    }
}