package com.example.ragchat.security;

import com.example.ragchat.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppProperties props;
    private final RedisFixedWindowRateLimiter redisLimiter;
    private final InMemoryFixedWindowRateLimiter inMemoryLimiter;

    public RateLimitFilter(
            AppProperties props,
            RedisFixedWindowRateLimiter redisLimiter,
            InMemoryFixedWindowRateLimiter inMemoryLimiter
    ) {
        this.props = props;
        this.redisLimiter = redisLimiter;
        this.inMemoryLimiter = inMemoryLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!props.getRateLimit().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = buildKey(request);

        try {
            RateLimiter limiter = props.getRateLimit().getBackend() == AppProperties.RateLimitBackend.REDIS
                    ? redisLimiter
                    : inMemoryLimiter;

            long remaining = limiter.consumeOrThrow(key);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
            filterChain.doFilter(request, response);
        } catch (RateLimitExceededException ex) {
            response.setStatus(429);
            response.setHeader("X-RateLimit-Remaining", String.valueOf(ex.getRemaining()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
            		{"code":"RATE_LIMITED","message":"Too many requests"}
            		""");
        }
    }

    private String buildKey(HttpServletRequest request) {
        String mode = props.getSecurity().getMode().name();

        String principal = SecurityUtils.currentUserId();
        if (principal == null || principal.isBlank()) {
            if (props.getSecurity().getMode() == AppProperties.AuthMode.API_KEY) {
                principal = request.getHeader(props.getSecurity().getUserIdHeader());
            } else {
                String auth = request.getHeader("Authorization");
                principal = (auth != null && !auth.isBlank()) ? "auth:" + sha256(auth) : "ip:" + request.getRemoteAddr();
            }
        }

        if (props.getSecurity().getMode() == AppProperties.AuthMode.API_KEY) {
            String apiKey = request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER);
            return mode + ":" + (apiKey == null ? "no-key" : sha256(apiKey)) + ":" + principal;
        }
        return mode + ":" + principal;
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            // shorter key string
            return HexFormat.of().formatHex(digest, 0, 16);
        } catch (Exception e) {
            return "hasherr";
        }
    }
}
