package com.example.aigateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    /** Comma-separated client bearer tokens accepted by the gateway (empty = dev mode, allow all). */
    private String clientTokens = "";

    private final Upstreams upstreams = new Upstreams();

    public List<String> clientTokenList() {
        List<String> out = new ArrayList<>();
        if (clientTokens == null || clientTokens.isBlank()) {
            return out;
        }
        for (String p : clientTokens.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    @Getter
    @Setter
    public static class Upstreams {
        private final OpenAiCompat openai = new OpenAiCompat();
        private final OpenAiCompat deepseek = new OpenAiCompat();
        private final Anthropic anthropic = new Anthropic();
        private final Gemini gemini = new Gemini();
    }

    @Getter
    @Setter
    public static class OpenAiCompat {
        private String baseUrl = "https://api.openai.com";
        private String apiKey = "";
    }

    @Getter
    @Setter
    public static class Anthropic {
        private String baseUrl = "https://api.anthropic.com";
        private String apiKey = "";
        private String version = "2023-06-01";
    }

    @Getter
    @Setter
    public static class Gemini {
        /** google (API key) or vertex (future extension; still uses API key field for local dev). */
        private String mode = "google";
        private String baseUrl = "https://generativelanguage.googleapis.com";
        private String apiKey = "";
    }
}
