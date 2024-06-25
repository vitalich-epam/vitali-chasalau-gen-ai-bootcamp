package com.epam.training.gen.ai.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class AiProxyConfiguration {
    public static final String AUTH_HEADER = "Api-Key";

    @Bean
    public RequestInterceptor authInterceptor(@Value("${client-azureopenai-key}") String apiKey) {

        return template -> template.header(AUTH_HEADER, apiKey);
    }
}
