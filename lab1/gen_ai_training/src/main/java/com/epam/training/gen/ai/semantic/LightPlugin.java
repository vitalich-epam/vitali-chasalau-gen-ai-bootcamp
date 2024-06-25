package com.epam.training.gen.ai.semantic;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LightPlugin {
    private boolean isOn = false;

    @DefineKernelFunction(name = "getState", description = "Gets the current state of the light")
    public String getState() {
        log.info("Getting state of the light");
        return getStateInternal();
    }

    @DefineKernelFunction(name = "changeState", description = "Changes the state of the light and returns new state as response.")
    public String changeState(
            @KernelFunctionParameter(name = "newState", description = "The new state of the light, boolean true==on, false==off.") String newState) {

        if(StringUtils.isBlank(newState)) {
            log.warn("[No state provided. Returning current state]");
            return getStateInternal();
        }

        log.info("Changing state of the light to {}", newState);

        if(newState.equalsIgnoreCase("true") || newState.equalsIgnoreCase("on")) {
            isOn = true;
        } else if(newState.equalsIgnoreCase("false") || newState.equalsIgnoreCase("off")) {
            isOn = false;
        } else {
            log.warn("[Invalid state provided. Returning current state]");
        }
        return getStateInternal();
    }

    private String getStateInternal(){
        return isOn ? "on" : "off";
    }
}
