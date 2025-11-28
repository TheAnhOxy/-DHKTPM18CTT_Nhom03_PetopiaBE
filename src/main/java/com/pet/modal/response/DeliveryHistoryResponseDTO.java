package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryHistoryResponseDTO {
    private String status;
    private String description;
    private String location;
    private LocalDateTime updatedAt;
}
