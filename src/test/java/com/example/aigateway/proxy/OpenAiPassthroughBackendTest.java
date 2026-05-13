package com.example.aigateway.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class OpenAiPassthroughBackendTest {

    @Test
    void forwardsBodyToOpenAiCompatibleUpstream() throws Exception {
        MockWebServer server = new MockWebServer();
        try {
            server.enqueue(
                    new MockResponse()
                            .setBody("{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"OK\"}}]}")
                            .addHeader("Content-Type", "application/json"));
            server.start();
            String base = "http://" + server.getHostName() + ":" + server.getPort();
            var backend = new OpenAiPassthroughBackend("openai", base, "sk-test");
            WebClient wc = WebClient.create();
            String req = "{\"model\":\"gpt-4o-mini\",\"messages\":[{\"role\":\"user\",\"content\":\"ping\"}]}";
            String resp = backend.forward(req, wc);
            assertThat(resp).contains("OK");
            var recorded = server.takeRequest();
            assertThat(recorded.getPath()).isEqualTo("/v1/chat/completions");
            assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer sk-test");
        } finally {
            server.shutdown();
        }
    }
}
