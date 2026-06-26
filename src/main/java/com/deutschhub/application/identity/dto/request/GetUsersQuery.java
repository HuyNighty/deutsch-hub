package com.deutschhub.application.identity.dto.request;

public record GetUsersQuery(
        String keyword,
        int page,
        int size
) {
    public GetUsersQuery {

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
