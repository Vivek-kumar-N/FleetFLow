package com.carcaddy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
     @ExceptionHandler(InvalidEntityException.class)
    public ResponseEntity<String> handleInvalidEntity(InvalidEntityException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex){
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);


    }
      @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethod(MethodArgumentNotValidException ex){
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);


    }

}

