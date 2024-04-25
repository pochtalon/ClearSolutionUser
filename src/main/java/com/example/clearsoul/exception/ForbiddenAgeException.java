package com.example.clearsoul.exception;

public class ForbiddenAgeException extends RuntimeException {
    public ForbiddenAgeException(String message) {
        super(message);
    }
}
