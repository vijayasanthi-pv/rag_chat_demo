package com.det.ragchat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Security security = new Security();
    private final RateLimit rateLimit = new RateLimit();
    private final Cors cors = new Cors();
    private final Encryption encryption = new Encryption();
    private final Retention retention = new Retention();

    public Security getSecurity() { return security; }
    public RateLimit getRateLimit() { return rateLimit; }
    public Cors getCors() { return cors; }
    public Encryption getEncryption() { return encryption; }
    public Retention getRetention() { return retention; }

    public static class Security {
        /** API_KEY or JWT */
        private AuthMode mode = AuthMode.API_KEY;

        /** API key value when mode=API_KEY */
        private String apiKey;

        /** required header for user scoping when mode=API_KEY */
        private String userIdHeader = "X-User-Id";


        /**
         * When true, roles/permissions are resolved from DB at request-time based on the userId.
         * This enables real-time role updates without redeploying or changing env vars.
         *
         * Default: true
         * Env: APP_SECURITY_RESOLVE_ROLES_FROM_DB
         */
        private boolean resolveRolesFromDb = true;

        /**
         * When true and resolveRolesFromDb=true, requests are rejected if the userId header
         * does not match an active DB user.
         *
         * Default: true
         * Env: APP_SECURITY_REQUIRE_USER_IN_DB
         */
        private boolean requireUserInDb = true;


        /**
         * Base roles granted for authorization (SpEL @PreAuthorize). Combined with DB roles when enabled.
         * Use comma-separated values in env: APP_SECURITY_ROLES=CHAT_READ,CHAT_WRITE
         * Values may be with or without ROLE_ prefix.
         */
        private List<String> roles = List.of("API");

        /**
         * Backwards compatibility: when true, ROLE_API is granted even if not present in roles.
         * Set false to require explicit roles only.
         */
        private boolean grantApiRole = true;

        public AuthMode getMode() { return mode; }
        public void setMode(AuthMode mode) { this.mode = mode; }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getUserIdHeader() { return userIdHeader; }
        public void setUserIdHeader(String userIdHeader) { this.userIdHeader = userIdHeader; }


        public boolean isResolveRolesFromDb() { return resolveRolesFromDb; }
        public void setResolveRolesFromDb(boolean resolveRolesFromDb) { this.resolveRolesFromDb = resolveRolesFromDb; }

        public boolean isRequireUserInDb() { return requireUserInDb; }
        public void setRequireUserInDb(boolean requireUserInDb) { this.requireUserInDb = requireUserInDb; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public boolean isGrantApiRole() { return grantApiRole; }
        public void setGrantApiRole(boolean grantApiRole) { this.grantApiRole = grantApiRole; }

    }

    public enum AuthMode {
        API_KEY,
        JWT
    }

    public static class RateLimit {
        private boolean enabled = true;

        /** IN_MEMORY or REDIS */
        private RateLimitBackend backend = RateLimitBackend.REDIS;

        /** max requests per window */
        private long capacity = 120;

        /** window size in seconds (fixed window) */
        private long refillDurationSeconds = 60;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public RateLimitBackend getBackend() { return backend; }
        public void setBackend(RateLimitBackend backend) { this.backend = backend; }

        public long getCapacity() { return capacity; }
        public void setCapacity(long capacity) { this.capacity = capacity; }

        public long getRefillDurationSeconds() { return refillDurationSeconds; }
        public void setRefillDurationSeconds(long refillDurationSeconds) { this.refillDurationSeconds = refillDurationSeconds; }
    }

    public enum RateLimitBackend {
        IN_MEMORY,
        REDIS
    }

    public static class Cors {
        private boolean enabled = true;
        private String allowedOrigins = "http://localhost:3000";
        private String allowedMethods = "GET,POST,PATCH,DELETE,OPTIONS";
        private String allowedHeaders = "Content-Type,X-API-Key,X-User-Id,Authorization";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public String getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }
        public String getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }
    }

    public static class Encryption {
        private boolean enabled = false;
        /** NONE|LOCAL|AWS_KMS */
        private EncryptionProvider provider = EncryptionProvider.LOCAL;

        private String localKeyBase64;

        private String awsRegion;
        private String awsKmsKeyId;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public EncryptionProvider getProvider() { return provider; }
        public void setProvider(EncryptionProvider provider) { this.provider = provider; }
        public String getLocalKeyBase64() { return localKeyBase64; }
        public void setLocalKeyBase64(String localKeyBase64) { this.localKeyBase64 = localKeyBase64; }
        public String getAwsRegion() { return awsRegion; }
        public void setAwsRegion(String awsRegion) { this.awsRegion = awsRegion; }
        public String getAwsKmsKeyId() { return awsKmsKeyId; }
        public void setAwsKmsKeyId(String awsKmsKeyId) { this.awsKmsKeyId = awsKmsKeyId; }
    }

    public enum EncryptionProvider {
        NONE,
        LOCAL,
        AWS_KMS
    }

    public static class Retention {
        private boolean enabled = true;
        private int hardDeleteAfterDays = 180;
        private int softDeleteGraceDays = 30;
        private String cron = "0 0 3 * * *";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getHardDeleteAfterDays() { return hardDeleteAfterDays; }
        public void setHardDeleteAfterDays(int hardDeleteAfterDays) { this.hardDeleteAfterDays = hardDeleteAfterDays; }
        public int getSoftDeleteGraceDays() { return softDeleteGraceDays; }
        public void setSoftDeleteGraceDays(int softDeleteGraceDays) { this.softDeleteGraceDays = softDeleteGraceDays; }
        public String getCron() { return cron; }
        public void setCron(String cron) { this.cron = cron; }
    }
}
