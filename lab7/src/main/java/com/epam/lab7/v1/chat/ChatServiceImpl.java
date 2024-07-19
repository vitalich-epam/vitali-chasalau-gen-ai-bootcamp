package com.epam.lab7.v1.chat;

import com.epam.lab7.v1.common.PromptRequest;
import com.epam.lab7.v1.documentreader.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private static final String INSTRUCTION_PROMPT = """
            Please answer based on provided context. Do not use other sources of information.
            If context doesn't provide enough information just reply that you don't have information about this subject.
            Context: %s
            """;
    private final ChatOptions chatOptions = ChatOptionsBuilder.builder().withTemperature(0.0f).build();
    private final AzureOpenAiChatClient chatClient;
    private final DocumentService documentService;

    @Override
    public String chat(PromptRequest userPrompt) {

        List<Message> messages = List.of(
                new SystemMessage(userPrompt.getSystemPrompt()),
                new UserMessage(userPrompt.getUserPrompt())
        );

        var prompt = new Prompt(messages, chatOptions);
        AssistantMessage response = chatClient.call(prompt)
                .getResult()
                .getOutput();
        return response.getContent();
    }

    @Override
    public String getResponseFromPdf(String message) {
        log.info("User request: {}", message);

        String content = documentService.searchDocumentsContent(message);
        log.info("Found related content: {}", content);

        List<Message> messages = List.of(
                new SystemMessage(String.format(INSTRUCTION_PROMPT, content)),
                new UserMessage(message)
        );

        var prompt = new Prompt(messages, chatOptions);
        AssistantMessage response = chatClient.call(prompt)
                .getResult()
                .getOutput();

        log.info("AI response: {}", response);

        return response.getContent();
    }
}
