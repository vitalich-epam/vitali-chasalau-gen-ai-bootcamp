package com.epam.training.gen.ai.semantic.client;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@NoArgsConstructor
public class OpenAIAsyncClientServiceImpl implements OpenAIAsyncClientService {

    @Setter
    @Value("${client-azureopenai-key}")
    private String apiKey;

    @Setter
    @Value("${client-azureopenai-endpoint}")
    private String endpoint;

    @Override
    public OpenAIAsyncClient get() {
        OpenAIClientBuilder provider = new OpenAIClientBuilder().credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint);
        return provider.buildAsyncClient();
    }
}
