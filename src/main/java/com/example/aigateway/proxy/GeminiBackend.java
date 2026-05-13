package com.example.aigateway.proxy;

import com.example.aigateway.config.GatewayProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GeminiBackend implements LlmBackend {

    private final GatewayProperties properties;
    private final ObjectMapper mapper;
    private final GeminiTranslator translator;

    @Override
    public String name() {
        return "gemini";
    }

    @Override
    public String forward(String rawJsonBody, WebClient webClient) throws Exception {
        JsonNode openAi = mapper.readTree(rawJsonBody);
        String model = openAi.path("model").asText("gemini-1.5-flash");
        var body = translator.toGenerateContentRequest(openAi);
        String apiKey = properties.getUpstreams().getGemini().getApiKey();
        var uri = translator.buildUri(properties.getUpstreams().getGemini().getBaseUrl(), model, apiKey);

        String raw = webClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapper.writeValueAsString(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return translator.toOpenAiChatCompletion(raw, model);
    }
}
