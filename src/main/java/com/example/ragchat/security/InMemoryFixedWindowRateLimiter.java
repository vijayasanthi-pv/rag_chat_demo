package com.example.ragchat.security;

import com.example.ragchat.config.AppProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryFixedWindowRateLimiter implements RateLimiter {

    private final AppProperties props;
    private final Cache<String, Window> windows;

    public InMemoryFixedWindowRateLimiter(AppProperties props) {
        this.props = props;
        this.windows = Caffeine.newBuilder()
                .maximumSize(50_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    @Override
    public long consumeOrThrow(String key) {
        long capacity = props.getRateLimit().getCapacity();
        long windowSeconds = props.getRateLimit().getRefillDurationSeconds();

        long epoch = Instant.now().getEpochSecond();
        long windowId = epoch / windowSeconds;
        String fullKey = "mem:" + key + ":" + windowId;

        Window w = windows.get(fullKey, k -> new Window());
        long used = w.counter.incrementAndGet();
        long remaining = capacity - used;

        if (remaining < 0) {
            throw new RateLimitExceededException(0);
        }
        return remaining;
    }

    static class Window {
        final AtomicLong counter = new AtomicLong(0);
    }
}
