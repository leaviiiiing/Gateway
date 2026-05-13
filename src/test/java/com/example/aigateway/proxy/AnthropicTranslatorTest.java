package com.example.aigateway.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

class AnthropicTranslatorTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AnthropicTranslator translator = new AnthropicTranslator(mapper);

    @Test
    void mapsOpenAiToAnthropicMessages() throws Exception {
        String openAi =
                """
                {"model":"claude-3-5-sonnet-20240620","messages":[
                  {"role":"system","content":"You are helpful"},
                  {"role":"user","content":"Hello"}
                ],"max_tokens":1024}
                """;
        ObjectNode anth = translator.toAnthropicMessagesRequest(mapper.readTree(openAi));
        assertThat(anth.path("model").asText()).isEqualTo("claude-3-5-sonnet-20240620");
        assertThat(anth.path("system").asText()).contains("helpful");
        assertThat(anth.path("messages")).hasSize(1);
        assertThat(anth.path("messages").get(0).path("role").asText()).isEqualTo("user");
        assertThat(anth.path("messages").get(0).path("content").asText()).isEqualTo("Hello");
    }

    @Test
    void mapsAnthropicResponseToOpenAi() throws Exception {
        String anth =
                """
                {"id":"msg_1","type":"message","role":"assistant","content":[
                  {"type":"text","text":"World"}
                ],"usage":{"input_tokens":1,"output_tokens":2}}
                """;
        String openAi = translator.toOpenAiChatCompletion(anth, "claude-3-5-sonnet-20240620");
        var node = mapper.readTree(openAi);
        assertThat(node.path("object").asText()).isEqualTo("chat.completion");
        assertThat(node.path("choices").get(0).path("message").path("content").asText()).isEqualTo("World");
    }
}
