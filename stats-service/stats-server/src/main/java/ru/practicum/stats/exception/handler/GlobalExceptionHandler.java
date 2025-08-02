package ru.practicum.stats.exception.handler;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    public ResponseEntity<Map<String, String>> handleDateTimeParseException(DateTimeParseException e) {
        log.error("DateTimeParseException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid date format. Expected format: yyyy-MM-dd HH:mm:ss"));
    }

    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid parameter type: " + e.getName()));
    }

    public ResponseEntity<Map<String, String>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Required parameter is missing: " + e.getParameterName()));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<Map<String, String>> handleValidationExceptions(Exception e) {
        String errorMessage;

        if (e instanceof MethodArgumentNotValidException) {
            errorMessage = ((MethodArgumentNotValidException) e).getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fieldError -> String.format("[%s] %s",
                            fieldError.getField(),
                            fieldError.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
        } else {
            errorMessage = ((ConstraintViolationException) e).getConstraintViolations()
                    .stream()
                    .map(violation -> String.format("[%s] %s",
                            violation.getPropertyPath(),
                            violation.getMessage()))
                    .collect(Collectors.joining("; "));
        }

        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "Validation failed",
                        "details", errorMessage
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}