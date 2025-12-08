package com.pet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.logging.ErrorLogEntry;
import com.pet.logging.ErrorLogStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorLogStore errorLogStore;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorLogEntry entry = ErrorLogEntry.builder()
                .timestamp(Instant.now())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .query(request.getQueryString())
                .exceptionClass(authException.getClass().getSimpleName())
                .message(authException.getMessage())
                .build();
        errorLogStore.add(entry);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("message", "Unauthorized");
        body.put("data", authException.getMessage());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}


