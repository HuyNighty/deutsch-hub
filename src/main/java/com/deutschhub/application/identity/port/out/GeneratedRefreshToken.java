package com.deutschhub.application.identity.port.out;

import java.time.LocalDateTime;

public record GeneratedRefreshToken(
        String value,
        String hash,
        LocalDateTime expiresAt
) {
}