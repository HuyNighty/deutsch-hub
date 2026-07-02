package com.deutschhub.application.learning.dto.request;

public record GetCoursesQuery(
        String keyword,
        int page,
        int size
) {
    public GetCoursesQuery {
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        if (page < 0) {
            page = 0;
        }

        if (size <= 0 || size > 100) {
            size = 20;
        }
    }
}
