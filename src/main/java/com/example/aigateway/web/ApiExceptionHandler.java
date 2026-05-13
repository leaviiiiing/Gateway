package com.example.aigateway.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> upstream(WebClientResponseException ex) {
        log.warn("upstream_error status={} bodySnippet={}", ex.getStatusCode(), truncate(ex.getResponseBodyAsString()));
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> rateLimited(RequestNotPermitted ex) throws Exception {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorJson("rate_limited"));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> unsupported(UnsupportedOperationException ex) throws Exception {
        if ("stream_not_supported".equals(ex.getMessage())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorJson(ex.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorJson(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) throws Exception {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorJson(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generic(Exception ex) throws Exception {
        log.error("unhandled_error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorJson("internal_error"));
    }

    private String errorJson(String code) throws Exception {
        ObjectNode n = objectMapper.createObjectNode();
        ObjectNode err = objectMapper.createObjectNode();
        err.put("message", code);
        n.set("error", err);
        return objectMapper.writeValueAsString(n);
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 500 ? s.substring(0, 500) + "…" : s;
    }
}
