package com.det.ragchat.api.dto;

public record PageMeta(
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
