package com.example.aigateway.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnthropicTranslator {

    private final ObjectMapper mapper;

    public ObjectNode toAnthropicMessagesRequest(JsonNode openAi) {
        String model = openAi.path("model").asText("claude-3-5-sonnet-20240620");
        int maxTokens = openAi.path("max_tokens").asInt(4096);
        if (maxTokens <= 0) {
            maxTokens = 4096;
        }

        List<String> systemParts = new ArrayList<>();
        ArrayNode messages = mapper.createArrayNode();

        JsonNode msgs = openAi.path("messages");
        if (msgs.isArray()) {
            for (JsonNode m : msgs) {
                String role = m.path("role").asText("");
                String content = textContent(m.path("content"));
                if ("system".equals(role)) {
                    systemParts.add(content);
                } else if ("user".equals(role) || "assistant".equals(role)) {
                    ObjectNode row = mapper.createObjectNode();
                    row.put("role", role);
                    row.put("content", content);
                    messages.add(row);
                }
            }
        }

        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        root.put("max_tokens", maxTokens);
        if (!systemParts.isEmpty()) {
            root.put("system", String.join("\n\n", systemParts));
        }
        root.set("messages", messages);
        return root;
    }

    public String toOpenAiChatCompletion(String anthropicResponseJson, String model) throws Exception {
        JsonNode root = mapper.readTree(anthropicResponseJson);
        JsonNode content = root.path("content");
        String text = "";
        if (content.isArray()) {
            for (JsonNode block : content) {
                if ("text".equals(block.path("type").asText())) {
                    text += block.path("text").asText("");
                }
            }
        }

        ObjectNode out = mapper.createObjectNode();
        out.put("id", root.path("id").asText("anthropic-msg"));
        out.put("object", "chat.completion");
        out.put("model", model);
        ObjectNode choice = mapper.createObjectNode();
        choice.put("index", 0);
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "assistant");
        message.put("content", text);
        choice.set("message", message);
        choice.put("finish_reason", "stop");
        ArrayNode choices = mapper.createArrayNode();
        choices.add(choice);
        out.set("choices", choices);
        ObjectNode usage = mapper.createObjectNode();
        int pt = root.path("usage").path("input_tokens").asInt(0);
        int ct = root.path("usage").path("output_tokens").asInt(0);
        usage.put("prompt_tokens", pt);
        usage.put("completion_tokens", ct);
        usage.put("total_tokens", pt + ct);
        out.set("usage", usage);
        return mapper.writeValueAsString(out);
    }

    private static String textContent(JsonNode contentNode) {
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode p : contentNode) {
                if ("text".equals(p.path("type").asText())) {
                    sb.append(p.path("text").asText(""));
                }
            }
            return sb.toString();
        }
        return contentNode.asText("");
    }
}
