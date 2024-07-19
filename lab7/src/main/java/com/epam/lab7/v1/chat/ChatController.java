package com.epam.lab7.v1.chat;

import com.epam.lab7.v1.common.PromptRequest;
import com.epam.lab7.v1.documentreader.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final DocumentService documentService;

    @PostMapping("/chat")
    public String chat(@RequestBody PromptRequest message) {
        return chatService.chat(message);
    }

    @GetMapping("/chat-pdf")
    public String chatWithPdf(@RequestParam(value = "message") String message) {
        return chatService.getResponseFromPdf(message);
    }

    @PostMapping("/ai/pdf")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        var uploadedFile = getUploadedFile(file);
        try {
            documentService.addDocument(uploadedFile);
        } finally {
            uploadedFile.delete();
        }
    }


    private File getUploadedFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        Path uploadPath = Paths.get("").toAbsolutePath().resolve(fileName);

        try {
            Files.deleteIfExists(uploadPath);
            Files.copy(file.getInputStream(), uploadPath);
            return uploadPath.toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save uploaded file", e);
        }
    }
}
