package com.deutschhub.infrastructure.identity.web.request;

public record RegisterUserRequest(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber
) {}
