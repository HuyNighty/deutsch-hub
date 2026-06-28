package com.deutschhub.application.identity.dto.request;

import java.util.Set;
import java.util.UUID;

public record UpdateUserRolesCommand(
        UUID userId,
        UUID currentAdminId,
        Set<String> roles
) {
}
