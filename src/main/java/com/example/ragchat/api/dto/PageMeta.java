package com.example.ragchat.api.dto;

public record PageMeta(
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
