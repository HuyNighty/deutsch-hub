package com.deutschhub.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminAccountProperties(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {
}