package com.example.aigateway.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackendRegistry {

    private final ObjectMapper mapper;
    private final LlmBackend openai;
    private final LlmBackend deepseek;
    private final AnthropicBackend anthropic;
    private final GeminiBackend gemini;

    public BackendRegistry(
            ObjectMapper mapper,
            @Qualifier("openaiPassthrough") LlmBackend openai,
            @Qualifier("deepseekPassthrough") LlmBackend deepseek,
            AnthropicBackend anthropic,
            GeminiBackend gemini) {
        this.mapper = mapper;
        this.openai = openai;
        this.deepseek = deepseek;
        this.anthropic = anthropic;
        this.gemini = gemini;
    }

    public LlmBackend resolve(String routeHeader, JsonNode body) {
        String route = routeHeader != null ? routeHeader.trim().toLowerCase() : "";
        if (!route.isEmpty()) {
            return switch (route) {
                case "openai" -> openai;
                case "deepseek" -> deepseek;
                case "anthropic" -> anthropic;
                case "gemini" -> gemini;
                default -> throw new IllegalArgumentException("unknown_route_provider: " + route);
            };
        }
        String model = body.path("model").asText("").toLowerCase();
        if (model.startsWith("deepseek")) {
            return deepseek;
        }
        if (model.startsWith("claude") || model.contains("anthropic")) {
            return anthropic;
        }
        if (model.startsWith("gemini") || model.startsWith("google/")) {
            return gemini;
        }
        return openai;
    }

    public JsonNode parseBody(String raw) throws Exception {
        return mapper.readTree(raw);
    }
}
