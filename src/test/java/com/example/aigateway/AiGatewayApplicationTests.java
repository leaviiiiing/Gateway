package com.example.aigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "GATEWAY_RATE_LIMIT_PER_MINUTE=999999",
            "gateway.client-tokens=",
            "resilience4j.ratelimiter.instances.gatewayChat.limit-for-period=999999",
            "resilience4j.ratelimiter.instances.gatewayChat.limit-refresh-period=1s",
            "resilience4j.ratelimiter.instances.gatewayChat.timeout-duration=0s"
        })
class AiGatewayApplicationTests {

    @Test
    void contextLoads() {}
}
