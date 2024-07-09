package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingService embeddingService;

    @GetMapping("/ai/embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        EmbeddingResponse embeddingResponse = this.embeddingClient.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);
    }

    @PostMapping("/ai/embedding")
    public void addDocument(@RequestBody String document) {
        this.embeddingService.addDocument(document);
    }

    @GetMapping("/ai/embedding/search")
    public List<Document> search(@RequestParam(value = "message", defaultValue = "prompt engineering") String message,
                                 @RequestParam(value = "similarity", defaultValue = "0.1") double similarityThreshold,
                                 @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return this.embeddingService.searchDocuments(message, similarityThreshold, limit);
    }

    @PostMapping("/ai/upload-embedding")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        var uploadedFile = getUploadedFile(file);
        try {
            embeddingService.addDocument(uploadedFile);
        } finally {
            uploadedFile.delete();
        }
    }

    @GetMapping("/ai/dimensions")
    public int getDimensions() {
        return embeddingClient.dimensions();
    }

    private File getUploadedFile(MultipartFile file) {
        try {
            File target = File.createTempFile("embeddingUpload", ".pdf");
            OutputStream out = new FileOutputStream(target);
            IOUtils.copy(file.getInputStream(), out);
            out.close();

            return target;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
