package com.deutschhub.application.identity.dto.request;

public record LoginUserRequest (
        String usernameOrEmail,
        String password
){}
