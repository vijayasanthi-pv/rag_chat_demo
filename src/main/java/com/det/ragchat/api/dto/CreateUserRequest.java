package com.det.ragchat.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank String userId,
        String displayName,
        Set<String> roles,
        Boolean active
) {}
