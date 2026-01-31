package com.det.ragchat.api.dto;

import com.det.ragchat.domain.Sender;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID sessionId,
        Sender sender,
        String content,
        JsonNode context,
        OffsetDateTime createdAt
) {}
