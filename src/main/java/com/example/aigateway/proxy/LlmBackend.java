package com.example.aigateway.proxy;

import org.springframework.web.reactive.function.client.WebClient;

public interface LlmBackend {

    String name();

    /** @param rawJsonBody OpenAI-style chat.completions JSON body (string) */
    String forward(String rawJsonBody, WebClient webClient) throws Exception;
}
