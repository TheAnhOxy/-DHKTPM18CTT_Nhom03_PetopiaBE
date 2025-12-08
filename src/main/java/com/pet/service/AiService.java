package com.pet.service;

import com.pet.modal.response.ChatResponseDTO;

public interface AiService {
    ChatResponseDTO chat(String userInput);
}