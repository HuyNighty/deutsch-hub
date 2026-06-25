package com.deutschhub.application.identity.dto.request;

public record LogoutCommand(
        String refreshToken
) {
}
