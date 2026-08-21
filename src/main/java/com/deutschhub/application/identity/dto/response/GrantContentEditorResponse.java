package com.deutschhub.application.identity.dto.response;

import com.deutschhub.domain.identity.enums.RoleType;

import java.util.Set;
import java.util.UUID;

public record GrantContentEditorResponse(
        UUID userId,
        String username,
        Set<RoleType> roles,
        boolean active
) {
}
