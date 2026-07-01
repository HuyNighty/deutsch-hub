package com.deutschhub.infrastructure.identity.web.request;

import jakarta.validation.constraints.NotBlank;

public record DeactivateMyAccountRequest (
        @NotBlank(message = "Password is required")
        String password
) {
}
