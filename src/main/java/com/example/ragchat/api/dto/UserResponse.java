package com.example.ragchat.api.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String userId,
        String displayName,
        boolean active,
        Set<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
