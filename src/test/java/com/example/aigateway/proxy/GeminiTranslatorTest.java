package com.example.aigateway.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GeminiTranslatorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final GeminiTranslator translator = new GeminiTranslator(mapper);

    @Test
    void buildUriContainsModelAndKey() {
        var uri = translator.buildUri("https://generativelanguage.googleapis.com", "gemini-1.5-flash", "secret");
        assertThat(uri.toString()).contains("gemini-1.5-flash");
        assertThat(uri.toString()).contains("key=secret");
    }

    @Test
    void mapsGeminiResponseToOpenAi() throws Exception {
        String gemini =
                """
                {"candidates":[{"content":{"parts":[{"text":"Hi"}]}}],
                 "usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1,"totalTokenCount":4}}
                """;
        String openAi = translator.toOpenAiChatCompletion(gemini, "gemini-1.5-flash");
        var node = mapper.readTree(openAi);
        assertThat(node.path("choices").get(0).path("message").path("content").asText()).isEqualTo("Hi");
    }
}
