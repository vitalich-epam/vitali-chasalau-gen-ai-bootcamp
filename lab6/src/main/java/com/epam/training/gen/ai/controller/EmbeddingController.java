package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.dto.PromptResponse;
import com.epam.training.gen.ai.service.EmbeddingService;
import com.epam.training.gen.ai.service.KernelPromptService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final KernelPromptService kernelPromptService;

    @GetMapping("/ai/embedding/search")
    public List<Document> search(@RequestParam(value = "message", defaultValue = "prompt engineering") String message,
                                 @RequestParam(value = "similarity", defaultValue = "0.1") double similarityThreshold,
                                 @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return this.embeddingService.searchDocuments(message, similarityThreshold, limit);
    }

    @GetMapping("/ai/prompt")
    public ResponseEntity<PromptResponse> prompt(@RequestParam(value = "message", defaultValue = "prompt engineering") String message) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(message)));
    }


    @PostMapping("/ai/upload-embedding")
    public void uploadFile(@RequestParam("file") MultipartFile file) {
        var uploadedFile = getUploadedFile(file);
        try {
            embeddingService.addDocument(uploadedFile, file.getOriginalFilename());
        } finally {
            uploadedFile.delete();
        }
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
