package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ImageGenerationResponse {
    private List<String> resultUrls;
}
