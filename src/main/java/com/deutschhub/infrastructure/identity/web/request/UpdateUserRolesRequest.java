package com.deutschhub.infrastructure.identity.web.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateUserRolesRequest(

        @NotEmpty(message = "Roles must not be empty")
        Set<String> roles
) {
}
