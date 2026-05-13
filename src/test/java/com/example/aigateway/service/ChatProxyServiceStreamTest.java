package com.example.aigateway.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.aigateway.proxy.BackendRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

class ChatProxyServiceStreamTest {

    @Test
    void rejectsStreamTrue() throws Exception {
        BackendRegistry reg = Mockito.mock(BackendRegistry.class);
        ChatProxyService svc = new ChatProxyService(reg, WebClient.create());
        String body = "{\"model\":\"gpt-4o-mini\",\"stream\":true,\"messages\":[]}";
        assertThatThrownBy(() -> svc.proxyChatCompletions(body, null))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("stream");
        Mockito.verifyNoInteractions(reg);
    }
}
