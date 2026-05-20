package com.love.Backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class) 
    public ResponseEntity<ErrorResponse> handleAlreadyExist(UserAlreadyExistsException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(400, "User Exists", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); 
    }

    @ExceptionHandler(BadRequestException.class) 
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); 
    }

    @ExceptionHandler(ResourceNotFoundException.class) 
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND); 
    }

    @ExceptionHandler(UnauthorizedException.class) 
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(401, "Unauthorized", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); 
    }

    @ExceptionHandler(InvalidCredentialsException.class) 
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(401, "Unauthorized", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED); 
    }

    @ExceptionHandler(ForbiddenException.class) 
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(403, "Forbidden", ex.getMessage(), request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN); 
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) 
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) { 
        String message = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", ")); 
        ErrorResponse error = new ErrorResponse(400, "Validation Failed", message, request.getRequestURI()); 
        return new ResponseEntity<>(error, new HttpHeaders(), HttpStatus.BAD_REQUEST); 
    }

    @ExceptionHandler(IllegalArgumentException.class) 
    public ResponseEntity<ErrorResponse> handleIllegalArgument(Exception ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(400, "Bad Request", "Invalid input value", request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST); 
    }

    @ExceptionHandler(Exception.class) 
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) { 
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error", "Something went wrong", request.getRequestURI()); 
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR); 
    }
}
