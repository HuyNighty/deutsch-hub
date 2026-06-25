package com.deutschhub.application.identity.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserDetailResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String phoneNumber,
        boolean active,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt
) {
}
