package com.pet.service;

import com.pet.modal.response.SystemPerformanceDTO;
import com.pet.modal.response.EndpointPerformanceDTO;

import java.util.List;
import java.util.Map;

public interface SystemPerformanceService {

    SystemPerformanceDTO getSystemPerformance();

    List<EndpointPerformanceDTO> getEndpointPerformance();

    List<Map<String, Object>> getRawHttpMetrics();
}


