package com.pet.service.impl;

import com.pet.modal.response.EndpointPerformanceDTO;
import com.pet.modal.response.SystemPerformanceDTO;
import com.pet.service.SystemPerformanceService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemPerformanceServiceImpl implements SystemPerformanceService {

    private final MeterRegistry meterRegistry;

    @Override
    public SystemPerformanceDTO getSystemPerformance() {

        Timer totalTimer = meterRegistry.find("http.server.requests").timer();

        long totalRequests = totalTimer != null ? totalTimer.count() : 0L;
        double avgResponseTimeMs = totalTimer != null
                ? totalTimer.mean(TimeUnit.MILLISECONDS)
                : 0.0;

        // Lấy thống kê theo status code
        long successRequests = getCountByStatusRange(200, 299);
        long clientErrors = getCountByStatusRange(400, 499);
        long serverErrors = getCountByStatusRange(500, 599);

        return SystemPerformanceDTO.builder()
                .totalRequests(totalRequests)
                .successRequests(successRequests)
                .clientErrors(clientErrors)
                .serverErrors(serverErrors)
                .avgResponseTimeMs(avgResponseTimeMs)
                .memoryUsedMb(getMemoryUsedMb())
                .memoryMaxMb(getMemoryMaxMb())
                .build();
    }

    @Override
    public List<EndpointPerformanceDTO> getEndpointPerformance() {
        Collection<Timer> collectedTimers = meterRegistry.find("http.server.requests").timers();
        if (collectedTimers.isEmpty()) {
            return List.of();
        }

        List<Timer> timers = new ArrayList<>(collectedTimers);

        Map<String, EndpointAccumulator> grouped = new HashMap<>();

        for (Timer timer : timers) {
            String uri = defaultIfBlank(timer.getId().getTag("uri"), "UNKNOWN");
            String method = defaultIfBlank(timer.getId().getTag("method"), "UNKNOWN");
            String status = timer.getId().getTag("status");
            String exception = timer.getId().getTag("exception");

            String key = method + " " + uri;
            EndpointAccumulator acc = grouped.computeIfAbsent(key, k -> new EndpointAccumulator(uri, method));

            acc.totalRequests += timer.count();
            acc.avgResponseTimeMsAccum += timer.mean(TimeUnit.MILLISECONDS) * timer.count(); // weighted sum
            acc.totalCountForAvg += timer.count();
            acc.maxResponseTimeMs = Math.max(acc.maxResponseTimeMs, timer.max(TimeUnit.MILLISECONDS));

            int statusCode = parseStatus(status);
            if (statusCode >= 200 && statusCode < 300) {
                acc.successRequests += timer.count();
            } else if (statusCode >= 400 && statusCode < 500) {
                acc.clientErrorRequests += timer.count();
            } else if (statusCode >= 500 && statusCode < 600) {
                acc.serverErrorRequests += timer.count();
            }

            if (exception != null && !"None".equalsIgnoreCase(exception)) {
                acc.exceptionCounts.merge(exception, timer.count(), Long::sum);
            }
        }

        return grouped.values().stream()
                .map(EndpointAccumulator::toDto)
                .sorted(Comparator.comparingDouble(EndpointPerformanceDTO::getAvgResponseTimeMs).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getRawHttpMetrics() {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Timer timer : meterRegistry.find("http.server.requests").timers()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("uri", defaultIfBlank(timer.getId().getTag("uri"), "UNKNOWN"));
            entry.put("method", defaultIfBlank(timer.getId().getTag("method"), "UNKNOWN"));
            entry.put("status", defaultIfBlank(timer.getId().getTag("status"), "UNKNOWN"));
            entry.put("exception", defaultIfBlank(timer.getId().getTag("exception"), "None"));
            entry.put("count", timer.count());
            entry.put("meanMs", timer.mean(TimeUnit.MILLISECONDS));
            entry.put("maxMs", timer.max(TimeUnit.MILLISECONDS));
            results.add(entry);
        }

        return results;
    }


    private long getCountByStatusRange(int fromInclusive, int toInclusive) {
        return meterRegistry.find("http.server.requests").timers().stream()
                .filter(timer -> {
                    String status = timer.getId().getTag("status");
                    if (status == null) {
                        return false;
                    }
                    try {
                        int code = Integer.parseInt(status);
                        return code >= fromInclusive && code <= toInclusive;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .mapToLong(Timer::count)
                .sum();
    }

    private double getMemoryUsedMb() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedBytes = heapUsage.getUsed();
        return bytesToMb(usedBytes);
    }

    private double getMemoryMaxMb() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long maxBytes = heapUsage.getMax();
        return bytesToMb(maxBytes);
    }

    private double bytesToMb(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    private int parseStatus(String statusTag) {
        if (statusTag == null) {
            return -1;
        }
        try {
            return Integer.parseInt(statusTag);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String defaultIfBlank(String value, String defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        return value;
    }

    private static class EndpointAccumulator {
        private final String uri;
        private final String method;
        private long totalRequests = 0;
        private double avgResponseTimeMsAccum = 0.0;
        private long totalCountForAvg = 0;
        private double maxResponseTimeMs = 0.0;
        private long successRequests = 0;
        private long clientErrorRequests = 0;
        private long serverErrorRequests = 0;
        private final Map<String, Long> exceptionCounts = new HashMap<>();

        EndpointAccumulator(String uri, String method) {
            this.uri = uri;
            this.method = method;
        }

        EndpointPerformanceDTO toDto() {
            double avgMs = totalCountForAvg > 0 ? avgResponseTimeMsAccum / totalCountForAvg : 0.0;
            List<String> topExceptions = exceptionCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            return EndpointPerformanceDTO.builder()
                    .uri(uri)
                    .method(method)
                    .totalRequests(totalRequests)
                    .avgResponseTimeMs(avgMs)
                    .maxResponseTimeMs(maxResponseTimeMs)
                    .successRequests(successRequests)
                    .clientErrorRequests(clientErrorRequests)
                    .serverErrorRequests(serverErrorRequests)
                    .topExceptions(topExceptions)
                    .build();
        }
    }
}



