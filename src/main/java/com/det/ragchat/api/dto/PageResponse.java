package com.det.ragchat.api.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        PageMeta page
) {}
