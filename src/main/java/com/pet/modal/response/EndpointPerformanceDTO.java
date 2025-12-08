package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointPerformanceDTO {

    private String uri;
    private String method;
    private long totalRequests;
    private double avgResponseTimeMs;
    private double maxResponseTimeMs;
    private long successRequests;
    private long clientErrorRequests;
    private long serverErrorRequests;
    private List<String> topExceptions;
}

