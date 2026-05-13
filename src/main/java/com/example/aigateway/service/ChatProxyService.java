package com.example.aigateway.service;

import com.example.aigateway.proxy.BackendRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProxyService {

    private final BackendRegistry registry;
    private final WebClient gatewayWebClient;

    @RateLimiter(name = "gatewayChat")
    public String proxyChatCompletions(String rawBody, String routeHeader) throws Exception {
        JsonNode body = registry.parseBody(rawBody);
        if (body.path("stream").asBoolean(false)) {
            throw new UnsupportedOperationException("stream_not_supported");
        }
        var backend = registry.resolve(routeHeader, body);
        log.info("routing llm backend={} model={}", backend.name(), body.path("model").asText(""));
        return backend.forward(rawBody, gatewayWebClient);
    }
}
