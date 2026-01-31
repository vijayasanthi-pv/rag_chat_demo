package com.example.ragchat.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        String title,
        boolean favorite,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
