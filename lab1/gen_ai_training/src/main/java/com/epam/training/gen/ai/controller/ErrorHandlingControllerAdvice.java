package com.epam.training.gen.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class ErrorHandlingControllerAdvice {
    private static final String ERROR_HANDLING = "Error handling";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handle(Exception e, HttpServletRequest request) {
        log.error(ERROR_HANDLING, e);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(INTERNAL_SERVER_ERROR.value());
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setError(e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(Exception e, HttpServletRequest request) {
        log.error(ERROR_HANDLING, e);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(BAD_REQUEST.value());
        errorResponse.setPath(request.getRequestURI());
        errorResponse.setError(e.getMessage());
        return errorResponse;
    }

    @Data
    public static class ErrorResponse {

        private Instant timestamp = Instant.now();
        private int status;
        private String error;
        private String path;
    }

}
