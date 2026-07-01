package com.deutschhub.application.identity.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSessionResponse(
        UUID id,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt,
        boolean active
) {
}
