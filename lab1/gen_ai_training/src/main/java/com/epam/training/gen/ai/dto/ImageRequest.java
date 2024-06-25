package com.epam.training.gen.ai.dto;

import lombok.Data;

@Data
public class ImageRequest {
    private String model = "dall-e-3";
    private String prompt;
    private Integer n = 1;
    private String size = "512x512";
}
