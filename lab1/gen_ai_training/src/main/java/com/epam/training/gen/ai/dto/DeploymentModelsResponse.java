package com.epam.training.gen.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeploymentModelsResponse {

    List<DeploymentModel> data;

    @Data
    public static class DeploymentModel {
        private String id;

    }
}

