package com.deutschhub.application.identity.port.out;

public record GeneratedToken(
        String value,
        long expiresIn
) {
}
