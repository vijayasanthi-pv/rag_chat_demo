package com.det.ragchat.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) return null;
        return name;
    }
}
