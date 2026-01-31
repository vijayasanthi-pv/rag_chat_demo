package com.example.ragchat.api.dto;

import com.example.ragchat.domain.Sender;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
        @NotNull(message = "sender is required")
        Sender sender,

        @NotBlank(message = "content is required")
        @Size(max = 10000, message = "content must be <= 10000 chars")
        String content,

        JsonNode context
) {}
