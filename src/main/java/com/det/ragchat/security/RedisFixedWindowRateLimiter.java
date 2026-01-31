package com.det.ragchat.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import com.det.ragchat.config.AppProperties;

import java.time.Instant;
import java.util.List;

@Component
public class RedisFixedWindowRateLimiter implements RateLimiter {

    private final AppProperties props;
    private final StringRedisTemplate redis;

    private final DefaultRedisScript<Long> script = new DefaultRedisScript<>(
            // KEYS[1] = key, ARGV[1] = ttlSeconds, ARGV[2] = capacity
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1])) end " +
            "local cap = tonumber(ARGV[2]) " +
            "if current > cap then return -1 else return cap - current end",
            Long.class
    );

    public RedisFixedWindowRateLimiter(AppProperties props, StringRedisTemplate redis) {
        this.props = props;
        this.redis = redis;
    }

    @Override
    public long consumeOrThrow(String key) {
        long capacity = props.getRateLimit().getCapacity();
        long windowSeconds = props.getRateLimit().getRefillDurationSeconds();

        long epoch = Instant.now().getEpochSecond();
        long windowId = epoch / windowSeconds;

        String redisKey = "rl:" + key + ":" + windowId;

        Long remaining = redis.execute(script, List.of(redisKey), String.valueOf(windowSeconds), String.valueOf(capacity));
        if (remaining == null) remaining = 0L;

        if (remaining < 0) {
            throw new RateLimitExceededException(0);
        }
        return remaining;
    }
}
