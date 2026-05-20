package com.love.Backend.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) { 
        super(message); 
    }
}
