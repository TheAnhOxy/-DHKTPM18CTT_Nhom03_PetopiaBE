package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDTO {
    private String message;
    private String actionType;
    private Object data;
}