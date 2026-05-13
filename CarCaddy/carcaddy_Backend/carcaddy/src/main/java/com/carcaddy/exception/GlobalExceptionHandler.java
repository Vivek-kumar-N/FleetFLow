package com.carcaddy.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidEntityException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidEntity(InvalidEntityException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /** Handles JPA constraint violations thrown at persist/flush time */
    @ExceptionHandler({TransactionSystemException.class, JpaSystemException.class})
    public ResponseEntity<Map<String, Object>> handleTransactionException(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException cve) {
                return handleConstraintViolation(cve);
            }
            cause = cause.getCause();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Data validation failed. Please check your input.");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return field + ": " + cv.getMessage();
                })
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", String.join("; ", errors));
        body.put("fields", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("message", String.join("; ", fieldErrors));
        body.put("fields", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String rawMessage = ex.getMessage() != null ? ex.getMessage() : "";

        // Parse JPA constraint violation messages from the raw string
        if (rawMessage.contains("ConstraintViolationImpl")) {
            List<String> errors = new java.util.ArrayList<>();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "propertyPath=(\\w+).*?interpolatedMessage='([^']+)'"
            );
            java.util.regex.Matcher m = p.matcher(rawMessage);
            while (m.find()) {
                errors.add(m.group(1) + ": " + m.group(2));
            }
            if (!errors.isEmpty()) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("status", HttpStatus.BAD_REQUEST.value());
                body.put("error", "Validation Failed");
                body.put("message", String.join("; ", errors));
                body.put("fields", errors);
                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", rawMessage.isEmpty() ? "An unexpected error occurred." : rawMessage);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
