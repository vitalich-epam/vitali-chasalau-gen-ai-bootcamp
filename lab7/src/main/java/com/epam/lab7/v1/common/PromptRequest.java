package com.epam.lab7.v1.common;

import lombok.Data;

@Data
public class PromptRequest {
    private static final String DEFAULT_SYSTEM_PROMPT = "I'm an AI, ask me anything!";

    private String systemPrompt = DEFAULT_SYSTEM_PROMPT;
    private String userPrompt;
}
