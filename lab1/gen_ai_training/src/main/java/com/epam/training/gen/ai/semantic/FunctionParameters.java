package com.epam.training.gen.ai.semantic;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FunctionParameters {

    Map<String, FunctionParameter> properties = new HashMap<>();
    String type;
    List<String> required;
}
