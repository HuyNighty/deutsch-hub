package com.deutschhub.infrastructure.identity.web.request;

public record LoginUserRequest(
        String usernameOrEmail,
        String password
) {}
