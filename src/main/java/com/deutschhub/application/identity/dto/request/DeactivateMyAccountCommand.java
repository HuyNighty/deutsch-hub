package com.deutschhub.application.identity.dto.request;

import java.util.UUID;

public record DeactivateMyAccountCommand (
        UUID userId,
        String password
) {
}
