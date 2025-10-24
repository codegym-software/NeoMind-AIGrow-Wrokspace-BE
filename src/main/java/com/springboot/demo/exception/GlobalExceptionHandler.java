package com.springboot.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateWorkspaceException.class)
    public ResponseEntity<Object> handleDuplicateWorkspaceException(
            DuplicateWorkspaceException ex, WebRequest request) {
        
        Map<String, Object> body = Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}