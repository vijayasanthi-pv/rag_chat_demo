package com.det.ragchat.api.dto;

import jakarta.validation.constraints.Size;

public record CreateSessionRequest(
        @Size(min = 1, max = 200, message = "title must be 1..200 chars")
        String title
) {}
