package com.deutschhub.application.identity.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
