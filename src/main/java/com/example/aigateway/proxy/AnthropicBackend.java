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
public class AnthropicBackend implements LlmBackend {

    private final GatewayProperties properties;
    private final ObjectMapper mapper;
    private final AnthropicTranslator translator;

    @Override
    public String name() {
        return "anthropic";
    }

    @Override
    public String forward(String rawJsonBody, WebClient webClient) throws Exception {
        JsonNode openAi = mapper.readTree(rawJsonBody);
        String model = openAi.path("model").asText("claude-3-5-sonnet-20240620");
        var body = translator.toAnthropicMessagesRequest(openAi);
        String base = properties.getUpstreams().getAnthropic().getBaseUrl().replaceAll("/+$", "");
        String key = properties.getUpstreams().getAnthropic().getApiKey();
        String version = properties.getUpstreams().getAnthropic().getVersion();

        String raw = webClient
                .post()
                .uri(base + "/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-api-key", key)
                .header("anthropic-version", version)
                .bodyValue(mapper.writeValueAsString(body))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return translator.toOpenAiChatCompletion(raw, model);
    }
}
