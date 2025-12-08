package com.pet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.logging.ErrorLogEntry;
import com.pet.logging.ErrorLogStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ErrorLogStore errorLogStore;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorLogEntry entry = ErrorLogEntry.builder()
                .timestamp(Instant.now())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .query(request.getQueryString())
                .exceptionClass(accessDeniedException.getClass().getSimpleName())
                .message(accessDeniedException.getMessage())
                .build();
        errorLogStore.add(entry);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_FORBIDDEN);
        body.put("message", "Forbidden");
        body.put("data", accessDeniedException.getMessage());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}


