package com.pet.controller;

import com.pet.modal.response.ApiResponse;
import com.pet.modal.response.ChatResponseDTO;
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
        try {
            String message = payload.get("message");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .status(400)
                                .message("Câu hỏi không được để trống")
                                .build()
                );
            }

            ChatResponseDTO response = aiService.chat(message);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message("AI trả lời thành công")
                            .data(response)
                            .build()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    ApiResponse.builder()
                            .status(500)
                            .message("Lỗi xử lý Chat: " + e.getMessage())
                            .build()
            );
        }
    }
}