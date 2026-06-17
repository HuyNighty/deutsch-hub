package com.deutschhub.infrastructure.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties (
        String secretKey,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {}

