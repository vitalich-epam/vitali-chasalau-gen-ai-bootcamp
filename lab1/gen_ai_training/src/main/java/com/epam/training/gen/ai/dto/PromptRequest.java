package com.epam.training.gen.ai.dto;

import lombok.Data;

@Data
public class PromptRequest {
    private String input;
    private Double temperature = 0d;
    private Integer maxTokens = 200;
}
