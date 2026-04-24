package com.pranta.ecommerce.Exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateValue(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}