package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.client.OpenAIAsyncClientService;
import com.epam.training.gen.ai.dto.VectorSearchResult;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KernelPromptService {

    private final OpenAIAsyncClientService aiClientService;
    private final EmbeddingService embeddingService;

    @Value("${client-azureopenai-deployment-name}")
    @Setter
    private String defaultModelName;

    @Value("${ai.maxTokensPerMessage}")
    @Setter
    private Integer maxTokens;

    @Value("${ai.temperature}")
    @Setter
    private Double temperature;

    public String executePromptWithKernel(String inputPrompt) {

        log.info("User request: {}", inputPrompt);

        VectorSearchResult internalSearch = embeddingService.searchClosestDocuments(inputPrompt);
        if (CollectionUtils.isEmpty(internalSearch.getRecords())) {
            log.info("Internal Search Result: No results found");
            return "Internal knowledgebase doesn't have related information";
        }
        log.info("Internal Search Result: {}", internalSearch);

        ChatHistory history = new ChatHistory();
        history.addSystemMessage("""
                        Please answer based on provided context. Do not use other sources of information.
                        If context doesn't provide enough information just reply that you don't have information about this subject.
                        Context consists of one or several excerpts from internal documents.
                        Please make sure to include reference which parts of documents you used to build your answer as a comment in the end
                        (example: to form this answer I used document orders.pdf segments 20-45).
                """);
        internalSearch.getRecords().forEach(record ->
                history.addSystemMessage(String.format("Document: %s, Segments: %s, Text: %s", record.getFileName()
                        , record.getIndexFrom() + "-" + record.getIndexTo(), record.getContent())));


        ChatCompletionService chatCompletionService = ChatCompletionService.builder()
                .withModelId(defaultModelName)
                .withOpenAIAsyncClient(aiClientService.get())
                .build();

        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
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
