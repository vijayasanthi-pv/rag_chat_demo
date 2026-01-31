package com.example.ragchat.security;

public interface RateLimiter {
    /**
     * @return remaining requests in the current window, or -1 if unlimited.
     * @throws RateLimitExceededException when limit is exceeded
     */
    long consumeOrThrow(String key);
}
