package com.epam.training.gen.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VectorSearchResult {

    private List<Record> records;

    @Data
    @AllArgsConstructor
    public static class Record {
        private String fileName;
        private Integer indexFrom;
        private Integer indexTo;
        private String content;
    }
}
