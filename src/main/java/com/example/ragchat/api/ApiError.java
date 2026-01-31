package com.example.ragchat.api;

import java.time.OffsetDateTime;

public record ApiError(
        String code,
        String message,
        String path,
        OffsetDateTime timestamp
) {}
