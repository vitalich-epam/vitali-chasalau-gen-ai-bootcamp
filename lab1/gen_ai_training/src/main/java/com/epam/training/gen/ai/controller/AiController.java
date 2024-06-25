package com.epam.training.gen.ai.controller;


import com.epam.training.gen.ai.dto.*;
import com.epam.training.gen.ai.semantic.ImageGenerationService;
import com.epam.training.gen.ai.semantic.KernelPromptService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@AllArgsConstructor
public class AiController {

    private final KernelPromptService kernelPromptService;
    private final ImageGenerationService imageGenerationService;

    @PostMapping("/prompt")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestBody PromptRequest prompt) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt.getInput(),
                        prompt.getModel(),
                        prompt.getTemperature(),
                        prompt.getMaxTokens())));
    }

    @GetMapping("/prompt")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestParam(name = "q") String prompt,
                                                                     @RequestParam(name = "model") String model,
                                                                     @RequestParam(defaultValue = "0") Double temperature,
                                                                     @RequestParam(defaultValue = "200") Integer maxTokens) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt, model, temperature, maxTokens)));
    }

    @GetMapping("/chat-history")
    public ResponseEntity<ChatHistoryResponse> getChatHistory() {
        return ResponseEntity.ok().body(kernelPromptService.getChatHistory());
    }
    @DeleteMapping("/chat-history")
    public ResponseEntity<Void> clearChatHistory() {
        kernelPromptService.clearChatHistory();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/models")
    public ResponseEntity<DeploymentModelsResponse> getModels() {
        return ResponseEntity.ok()
                .body(kernelPromptService.getModels());
    }

    @PostMapping("/image")
    public ResponseEntity<ImageGenerationResponse> generateImage(@RequestBody ImageRequest prompt) {
        return ResponseEntity.ok()
                .body(new ImageGenerationResponse(imageGenerationService.getImages(prompt.getModel(),
                        prompt.getPrompt(),
                        prompt.getN(),
                        prompt.getSize())));
    }

    @GetMapping("/image")
    public ResponseEntity<ImageGenerationResponse> generateImage(@RequestParam(name = "q") String prompt,
                                                                     @RequestParam(defaultValue = "dall-e-3") String model,
                                                                     @RequestParam(defaultValue = "1") Integer n,
                                                                     @RequestParam(defaultValue = "512x512") String size) {
        return ResponseEntity.ok()
                .body(new ImageGenerationResponse(imageGenerationService.getImages(model, prompt, n, size)));
    }
}
