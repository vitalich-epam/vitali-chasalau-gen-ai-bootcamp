package com.epam.training.gen.ai.controller;


import com.epam.training.gen.ai.dto.PromptRequest;
import com.epam.training.gen.ai.dto.PromptResponse;
import com.epam.training.gen.ai.semantic.KernelPromptService;
import com.epam.training.gen.ai.semantic.SimplePromptService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@AllArgsConstructor
public class AiController {

    private final SimplePromptService promptService;
    private final KernelPromptService kernelPromptService;

    @PostMapping("/prompt")
    public ResponseEntity<PromptResponse> generateResponse(@RequestBody PromptRequest prompt) {
        log.info("prompt value: {}", prompt);
        return ResponseEntity.ok()
                .body(new PromptResponse(promptService.executePrompt(prompt.getInput())));
    }

    @GetMapping("/prompt")
    public ResponseEntity<PromptResponse> generateResponse(@RequestParam(name="q") String prompt) {
        log.info("prompt value: {}", prompt);
        return ResponseEntity.ok()
                .body(new PromptResponse(promptService.executePrompt(prompt)));
    }

    @PostMapping("/kernel")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestBody PromptRequest prompt) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt.getInput())));
    }

    @GetMapping("/kernel")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestParam(name="q") String prompt) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt)));
    }
}
