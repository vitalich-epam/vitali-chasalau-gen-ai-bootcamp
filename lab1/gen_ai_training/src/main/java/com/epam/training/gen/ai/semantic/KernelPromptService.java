package com.epam.training.gen.ai.semantic;

import com.epam.training.gen.ai.semantic.client.OpenAIAsyncClientService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
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

    public String executePromptWithKernel(String inputPrompt) {

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

        ChatHistory history = new ChatHistory();
        history.addSystemMessage("""
                        Hello! I am a light control system. You can ask me to turn on or off the light.
                        I use the following commands of the light plugin
                           getState() - to get the current state of the light
                           changeState(true) - to turn on or off the light
                """);
        history.addUserMessage(inputPrompt);

        var invocationContext = InvocationContext.builder()
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
        log.info("AI response: {}", messageResponse);

        return messageResponse;
    }
}
