package com.deutschhub.application.identity.dto.response;

import java.util.Set;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        boolean active,
        Set<String> roles
) {
}
