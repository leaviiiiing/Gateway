package com.example.aigateway.web;

import com.example.aigateway.service.ChatProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ChatCompletionsController {

    private final ChatProxyService chatProxyService;

    @PostMapping(path = "/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chatCompletions(
            @RequestBody String body,
            @RequestHeader(value = "X-Route-Provider", required = false) String routeProvider)
            throws Exception {
        return ResponseEntity.ok(chatProxyService.proxyChatCompletions(body, routeProvider));
    }
}
