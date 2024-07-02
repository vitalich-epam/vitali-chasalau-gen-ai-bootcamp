package com.epam.training.gen.ai.semantic.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class BingSearchUrlPlugin {

    private static final String BING_URL_TEMPLATE = "https://www.bing.com/search?q=%s";

    @DefineKernelFunction(name = "getBingSearchUrl", description = "Return URL for Bing search query.")
    public String getBingSearchUrl(
            @KernelFunctionParameter(description = "Text to search for", name = "query") String query) {
        log.info("BingSearchUrlPlugin is used with query: {}", query);
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String result = String.format(BING_URL_TEMPLATE, encoded);
        log.info("Result: {}", result);
        return result;
    }
}
