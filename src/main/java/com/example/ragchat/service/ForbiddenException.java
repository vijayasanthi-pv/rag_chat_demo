package com.example.ragchat.service;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
