package com.pet.modal.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDTO {
    private String message;
    private String actionType;
    private String dataType;   // "pet", "service", "article", null
    private Object data;       // chính là rawData
}