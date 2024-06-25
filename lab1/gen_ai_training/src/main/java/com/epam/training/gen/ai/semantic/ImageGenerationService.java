package com.epam.training.gen.ai.semantic;

import com.azure.ai.openai.models.*;
import com.epam.training.gen.ai.semantic.client.OpenAIAsyncClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageGenerationService {

    private final OpenAIAsyncClientService aiClientService;

    public List<String> getImages(String model, String prompt, Integer n, String size) {
        log.info("Requesting image generation with model: {}, prompt: {}, n: {}, size: {}",
                model, prompt, n, size);

        ImageGenerationOptions options = new ImageGenerationOptions(prompt);
        options.setN(n);
        options.setSize(ImageSize.fromString(size));
        options.setModel(model);
        options.setQuality(ImageGenerationQuality.fromString("standard"));

        ImageGenerations imageGenerations = aiClientService.get()
                .getImageGenerations(model, options)
                .block();

        List<ImageGenerationData> data = imageGenerations.getData();

        List<String> result = data.stream()
                .map(ImageGenerationData::getUrl)
                .collect(Collectors.toList());

        log.info("Generated images: {}", result);
        return result;
    }
}
