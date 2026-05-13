package com.example.aigateway.config;

import com.example.aigateway.proxy.LlmBackend;
import com.example.aigateway.proxy.OpenAiPassthroughBackend;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayBackendsConfiguration {

    @Bean
    @Qualifier("openaiPassthrough")
    public LlmBackend openaiPassthrough(GatewayProperties properties) {
        var o = properties.getUpstreams().getOpenai();
        return new OpenAiPassthroughBackend("openai", o.getBaseUrl(), o.getApiKey());
    }

    @Bean
    @Qualifier("deepseekPassthrough")
    public LlmBackend deepseekPassthrough(GatewayProperties properties) {
        var o = properties.getUpstreams().getDeepseek();
        return new OpenAiPassthroughBackend("deepseek", o.getBaseUrl(), o.getApiKey());
    }
}
