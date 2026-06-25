package com.deutschhub.infrastructure.identity.web.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutUserRequest(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
