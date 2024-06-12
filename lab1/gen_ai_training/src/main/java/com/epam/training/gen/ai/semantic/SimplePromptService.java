package com.epam.training.gen.ai.semantic;

import com.azure.ai.openai.models.*;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.BinaryData;
import com.azure.json.implementation.jackson.core.type.TypeReference;
import com.epam.training.gen.ai.semantic.client.OpenAIAsyncClientService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimplePromptService {

    public static final String GREETING_MESSAGE = "Say hi";
    public static final int MAX_FUNC_ITERATIONS = 3;

    private final OpenAIAsyncClientService aiClientService;
    private final LightPlugin lightPlugin;

    @Value("${client-azureopenai-deployment-name}")
    @Setter
    private String deploymentOrModelName;

    public List<String> getChatCompletions() {

        ChatCompletions completions = aiClientService.get()
                .getChatCompletions(
                        deploymentOrModelName,
                        new ChatCompletionsOptions(
                                List.of(new ChatRequestUserMessage(GREETING_MESSAGE))))
                .block();
        List<String> messages = completions.getChoices().stream()
                .map(c -> c.getMessage().getContent())
                .collect(Collectors.toList());
        log.info(messages.toString());
        return messages;
    }

    @SneakyThrows
    public String executePrompt(String inputPrompt) {
        List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestUserMessage(inputPrompt));

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(messages);
        FunctionDefinition functionGetLightState = new FunctionDefinition("get_light_state");
        functionGetLightState.setDescription("Gets the state of the light: on (means that the light is switched on) or off (means that the light is switched off).");

        FunctionDefinition functionSetLightState = new FunctionDefinition("set_light_state");
        functionGetLightState.setDescription("Changes the state of the light.");

        FunctionParameters parameters = new FunctionParameters();

        Map<String, FunctionParameter> parametersMap = new HashMap<>();
        parametersMap.put("newState", new FunctionParameter("The new state of the light, boolean true==on, false==off.", "string"));

        parameters.setProperties(parametersMap);
        parameters.setType("object");
        parameters.setRequired(List.of("newState"));

        functionSetLightState.setParameters(BinaryData.fromObject(parameters));

        chatCompletionsOptions.setFunctions(List.of(functionGetLightState, functionSetLightState));

        ChatCompletions completions = aiClientService.get()
                .getChatCompletions(
                        deploymentOrModelName,
                        chatCompletionsOptions)
                .block();

        ChatResponseMessage message = completions.getChoices().get(0).getMessage();
        FunctionCall functionCall = message.getFunctionCall();

        int iteration = 0;
        while (functionCall != null) {
            log.info("Function iteration: {}", ++iteration);
            if (iteration > MAX_FUNC_ITERATIONS) {
                throw new RuntimeException("Max iterations reached. Exiting.");
            }
            log.info("Function call requested: {}({})", functionCall.getName(), functionCall.getArguments());

            String result = switch (functionCall.getName()) {
                case "get_light_state" -> lightPlugin.getState();
                case "set_light_state" -> {
                    String argumentsJSON = functionCall.getArguments();
                    ObjectMapperShim objectMapper = ObjectMapperShim.createDefaultMapper();
                    TypeReference<Map<String, String>> typeReference = new TypeReference<>() {
                    };
                    Map<String, String> params = objectMapper.readValue(argumentsJSON, typeReference.getType());
                    yield lightPlugin.changeState(params.get("newState"));
                }
                default -> "Unknown function";
            };
            log.info("Function result: {}", result);
            messages.add(new ChatRequestFunctionMessage(functionCall.getName(), result));
            ChatCompletionsOptions chatCompletionsOptionsUpd = new ChatCompletionsOptions(messages);
            chatCompletionsOptionsUpd.setFunctions(List.of(functionGetLightState));

            completions = aiClientService.get()
                    .getChatCompletions(
                            deploymentOrModelName,
                            chatCompletionsOptionsUpd
                    )
                    .block();

            ChatResponseMessage newMessage = completions.getChoices().get(0).getMessage();
            functionCall = newMessage.getFunctionCall();
        }
        var response = completions.getChoices().stream()
                .map(c -> c.getMessage().getContent())
                .collect(Collectors.joining("\n"));
        log.info("Response value: {}", response);
        return response;

    }
}


