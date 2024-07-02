package com.epam.training.gen.ai.semantic;

import com.epam.training.gen.ai.dto.ChatHistoryResponse;
import com.epam.training.gen.ai.semantic.client.OpenAIAsyncClientService;
import com.epam.training.gen.ai.semantic.plugin.AgeCalculatorPlugin;
import com.epam.training.gen.ai.semantic.plugin.BingSearchUrlPlugin;
import com.epam.training.gen.ai.semantic.plugin.WikiSearchUrlPlugin;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KernelPromptService {
    private final OpenAIAsyncClientService aiClientService;
    private final AgeCalculatorPlugin ageCalculatorPlugin;
    private final WikiSearchUrlPlugin wikiSearchUrlPlugin;
    private final BingSearchUrlPlugin bingSearchUrlPlugin;

    @Value("${client-azureopenai-deployment-name}")
    @Setter
    private String defaultModelName;

    private ChatHistory history;

    @PostConstruct
    void init() {
        history = new ChatHistory();
        history.addSystemMessage("""
                        Hello! I am an assistant that can use different plugins to generate better answers to user requests.
                """);
    }

    public String executePromptWithKernel(String inputPrompt, Double temperature, Integer maxTokens) {

        log.info("User request: {}", inputPrompt);

        ChatCompletionService chatCompletionService = ChatCompletionService.builder()
                .withModelId(defaultModelName)
                .withOpenAIAsyncClient(aiClientService.get())
                .build();

        KernelPlugin ageCalculator = KernelPluginFactory.createFromObject(
                ageCalculatorPlugin, "AgeCalculatorPlugin");
        KernelPlugin searchWiki = KernelPluginFactory.createFromObject(
                wikiSearchUrlPlugin, "SearchWikiUrlPlugin");
        KernelPlugin searchBing = KernelPluginFactory.createFromObject(
                bingSearchUrlPlugin, "SearchBingUrlPlugin");


        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(ageCalculator)
                .withPlugin(searchWiki)
                .withPlugin(searchBing)
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

    public void clearChatHistory() {
        init();
    }

    public ChatHistoryResponse getChatHistory() {
        List<ChatHistoryResponse.ChatMessage> messages = history.getMessages().stream()
                .map(message ->
                        new ChatHistoryResponse.ChatMessage(message.getAuthorRole().toString(),
                                message.getContent()))
                .collect(Collectors.toList());
        return new ChatHistoryResponse(messages);
    }
}
