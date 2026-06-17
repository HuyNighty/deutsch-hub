package com.deutschhub.application.identity.dto.response;

public record LoginResponse(
        UserResponse user,
        String accessToken,
        long accessTokenExpiresIn
) {
}
