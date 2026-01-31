package com.example.ragchat.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record SetUserRolesRequest(
        @NotNull Set<String> roles
) {}
