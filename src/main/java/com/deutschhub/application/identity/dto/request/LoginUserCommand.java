package com.deutschhub.application.identity.dto.request;

public record LoginUserCommand(
        String usernameOrEmail,
        String password
) {}
