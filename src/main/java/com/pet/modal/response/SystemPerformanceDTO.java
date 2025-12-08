package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPerformanceDTO {

    private long totalRequests;
    private long successRequests;
    private long clientErrors;
    private long serverErrors;
    private double avgResponseTimeMs;
    private double memoryUsedMb;
    private double memoryMaxMb;
}


