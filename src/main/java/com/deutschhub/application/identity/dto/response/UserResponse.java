package com.deutschhub.application.identity.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String fullName) {
}