package com.epam.lab7.v1.chat;

import com.epam.lab7.v1.common.PromptRequest;

public interface ChatService {
    String chat(PromptRequest prompt);
    String getResponseFromPdf(String message);
}
