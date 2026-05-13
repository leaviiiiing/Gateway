package com.example.aigateway.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BackendRegistryTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void headerOverridesModel() throws Exception {
        LlmBackend openai = Mockito.mock(LlmBackend.class);
        Mockito.when(openai.name()).thenReturn("openai");
        LlmBackend deepseek = Mockito.mock(LlmBackend.class);
        AnthropicBackend anth = Mockito.mock(AnthropicBackend.class);
        GeminiBackend gem = Mockito.mock(GeminiBackend.class);
        var reg = new BackendRegistry(mapper, openai, deepseek, anth, gem);
        String body = "{\"model\":\"deepseek-chat\"}";
        assertThat(reg.resolve("openai", mapper.readTree(body))).isSameAs(openai);
        assertThat(reg.resolve("deepseek", mapper.readTree(body))).isSameAs(deepseek);
        assertThat(reg.resolve("anthropic", mapper.readTree(body))).isSameAs(anth);
        assertThat(reg.resolve("gemini", mapper.readTree(body))).isSameAs(gem);
    }

    @Test
    void infersDeepseekFromModelPrefix() throws Exception {
        LlmBackend openai = Mockito.mock(LlmBackend.class);
        LlmBackend deepseek = Mockito.mock(LlmBackend.class);
        AnthropicBackend anth = Mockito.mock(AnthropicBackend.class);
        GeminiBackend gem = Mockito.mock(GeminiBackend.class);
        var reg = new BackendRegistry(mapper, openai, deepseek, anth, gem);
        String body = "{\"model\":\"deepseek-chat\"}";
        assertThat(reg.resolve(null, mapper.readTree(body))).isSameAs(deepseek);
    }

    @Test
    void unknownRouteThrows() throws Exception {
        LlmBackend openai = Mockito.mock(LlmBackend.class);
        LlmBackend deepseek = Mockito.mock(LlmBackend.class);
        AnthropicBackend anth = Mockito.mock(AnthropicBackend.class);
        GeminiBackend gem = Mockito.mock(GeminiBackend.class);
        var reg = new BackendRegistry(mapper, openai, deepseek, anth, gem);
        assertThatThrownBy(() -> reg.resolve("unknown", mapper.readTree("{}")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
