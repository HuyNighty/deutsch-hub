package com.deutschhub.application.identity.dto.request;

public record GetUsersQuery(
        int page,
        int size
) {
    public GetUsersQuery {
        if (page < 0) {
            page = 0;
        }

        if (size <= 0 || size > 100) {
            size = 20;
        }
    }
}
