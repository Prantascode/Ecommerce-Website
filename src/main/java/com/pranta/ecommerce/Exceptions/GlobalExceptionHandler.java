package com.pranta.ecommerce.Exceptions;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRequest(
            InvalidRequestException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                errors,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("Parameter '%s' invalid value: '%s'", ex.getName(), ex.getValue());
        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                "Malformed JSON request body",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                "Access Denied: You do not have permission to access this resource",
                request.getRequestURI(),
                HttpStatus.FORBIDDEN.value()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                "Invalid credentials provided",
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponseDto> handleDisabledUser(
            DisabledException ex,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                LocalDateTime.now(),
                "User account is deactivated. Please contact support.",
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED.value()
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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