package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusStatsDTO {
    private long pending;
    private long confirmed;
    private long shipped;
    private long delivered;
    private long cancelled;
}