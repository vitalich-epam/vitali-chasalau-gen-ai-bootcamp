package com.epam.training.gen.ai.semantic.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class WikiSearchUrlPlugin {

    private static final String WIKIPEDIA_URL_TEMPLATE = "https://wikipedia.org/w/index.php?search=%s";

    @DefineKernelFunction(name = "getWikipediaSearchUrl", description = "Return URL for Wikipedia search query.")
    public String getWikipediaSearchUrl(
            @KernelFunctionParameter(description = "Text to search for", name = "query") String query) {
        log.info("WikiSearchUrlPlugin is used with query: {}", query);
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String result = String.format(WIKIPEDIA_URL_TEMPLATE, encoded);
        log.info("Result: {}", result);
        return result;
    }
}
