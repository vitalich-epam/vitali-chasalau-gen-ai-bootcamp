package com.epam.training.gen.ai.semantic;

import com.epam.training.gen.ai.semantic.client.OpenAIAsyncClientService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KernelPromptService {
    private final OpenAIAsyncClientService aiClientService;
    private final LightPlugin lightPlugin;

    @Value("${client-azureopenai-deployment-name}")
    @Setter
    private String deploymentOrModelName;

    private final ChatHistory history = new ChatHistory();

    @PostConstruct
    void init() {
        history.addSystemMessage("""
                        Hello! I am an assistant that apart of other questions can help also with light control system. 
                        You can ask me to turn on or off the light.
                        I use the following commands of the light plugin
                           getState() - to get the current state of the light
                           changeState(true) - to turn on or off the light
                """);
    }

    public String executePromptWithKernel(String inputPrompt, Double temperature, Integer maxTokens) {

        log.info("User request: {}", inputPrompt);
        ChatCompletionService chatCompletionService = ChatCompletionService.builder()
                .withModelId(deploymentOrModelName)
                .withOpenAIAsyncClient(aiClientService.get())
                .build();

        KernelPlugin plugin = KernelPluginFactory.createFromObject(
                lightPlugin, "LightPlugin");

        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(plugin)
                .build();

        history.addUserMessage(inputPrompt);

        var invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(
                        PromptExecutionSettings.builder()
                                .withTemperature(temperature)
                                .withMaxTokens(maxTokens)
                                .build()
                )
                .withToolCallBehavior(
                        ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
        var result = chatCompletionService
                .getChatMessageContentsAsync(
                        history,
                        kernel,
                        invocationContext)
                .block();
        String messageResponse = result.get(result.size() - 1).getContent();

        history.addAssistantMessage(messageResponse);

        log.info("AI response: {}", messageResponse);

        return messageResponse;
    }
}
