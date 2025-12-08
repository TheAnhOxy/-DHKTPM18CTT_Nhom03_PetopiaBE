package com.pet.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogEntry {
    private Instant timestamp;
    private String method;
    private String path;
    private String query;
    private String exceptionClass;
    private String message;
}

