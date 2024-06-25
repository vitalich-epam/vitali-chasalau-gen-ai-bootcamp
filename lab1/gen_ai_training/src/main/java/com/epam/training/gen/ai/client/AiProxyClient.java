package com.epam.training.gen.ai.client;

import com.epam.training.gen.ai.dto.DeploymentModelsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "aiProxyClient",
        url = "${client-azureopenai-endpoint}/openai",
        configuration = AiProxyConfiguration.class
)
public interface AiProxyClient {
    @GetMapping("/deployments")
    DeploymentModelsResponse getDeployments();
}
