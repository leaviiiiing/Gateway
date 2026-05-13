package com.example.aigateway.proxy;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class OpenAiPassthroughBackend implements LlmBackend {

    private final String name;
    private final String baseUrl;
    private final String apiKey;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String forward(String rawJsonBody, WebClient webClient) {
        String base = baseUrl.replaceAll("/+$", "");
        return webClient
                .post()
                .uri(base + "/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(rawJsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
