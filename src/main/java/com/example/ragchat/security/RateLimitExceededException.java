package com.example.ragchat.security;

public class RateLimitExceededException extends RuntimeException {
    private final long remaining;
    public RateLimitExceededException(long remaining) {
        super("Too many requests");
        this.remaining = remaining;
    }
    public long getRemaining() { return remaining; }
}
