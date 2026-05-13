package com.example.aigateway.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class GeminiTranslator {

    private final ObjectMapper mapper;

    public ObjectNode toGenerateContentRequest(JsonNode openAi) {
        ArrayNode contents = mapper.createArrayNode();
        JsonNode msgs = openAi.path("messages");
        if (msgs.isArray()) {
            for (JsonNode m : msgs) {
                String role = m.path("role").asText("");
                if ("system".equals(role)) {
                    continue;
                }
                String geminiRole = "assistant".equals(role) ? "model" : "user";
                ObjectNode contentRow = mapper.createObjectNode();
                contentRow.put("role", geminiRole);
                ArrayNode parts = mapper.createArrayNode();
                ObjectNode part = mapper.createObjectNode();
                part.put("text", textContent(m.path("content")));
                parts.add(part);
                contentRow.set("parts", parts);
                contents.add(contentRow);
            }
        }

        ObjectNode root = mapper.createObjectNode();
        root.set("contents", contents);
        return root;
    }

    public URI buildUri(String baseUrl, String model, String apiKey) {
        String base = baseUrl.replaceAll("/+$", "");
        return UriComponentsBuilder.fromUriString(base + "/v1beta/models/" + model + ":generateContent")
                .queryParam("key", apiKey)
                .build()
                .toUri();
    }

    public String toOpenAiChatCompletion(String geminiResponseJson, String model) throws Exception {
        JsonNode root = mapper.readTree(geminiResponseJson);
        JsonNode candidates = root.path("candidates");
        String text = "";
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (parts.isArray()) {
                for (JsonNode p : parts) {
                    text += p.path("text").asText("");
                }
            }
        }

        ObjectNode out = mapper.createObjectNode();
        out.put("id", "gemini-" + System.currentTimeMillis());
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
        usage.put("prompt_tokens", root.path("usageMetadata").path("promptTokenCount").asInt(0));
        usage.put("completion_tokens", root.path("usageMetadata").path("candidatesTokenCount").asInt(0));
        usage.put("total_tokens", root.path("usageMetadata").path("totalTokenCount").asInt(0));
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
