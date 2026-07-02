package com.deutschhub.common.util;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
