package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatHistoryResponse {
    List<ChatMessage> messages;

    @Data
    @AllArgsConstructor
    public static class ChatMessage {
        String role;
        String message;
    }
}
