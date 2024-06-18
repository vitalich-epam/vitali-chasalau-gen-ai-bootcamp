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

    private final KernelPromptService kernelPromptService;

    @PostMapping("/kernel")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestBody PromptRequest prompt) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt.getInput(),
                        prompt.getTemperature(),
                        prompt.getMaxTokens())));
    }

    @GetMapping("/kernel")
    public ResponseEntity<PromptResponse> generateResponseWithKernel(@RequestParam(name = "q") String prompt,
                                                                     @RequestParam(defaultValue = "0") Double temperature,
                                                                     @RequestParam(defaultValue = "200") Integer maxTokens) {
        return ResponseEntity.ok()
                .body(new PromptResponse(kernelPromptService.executePromptWithKernel(prompt, temperature, maxTokens)));
    }
}
