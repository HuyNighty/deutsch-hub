package com.deutschhub.application.content.article.dto.query;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {
}
