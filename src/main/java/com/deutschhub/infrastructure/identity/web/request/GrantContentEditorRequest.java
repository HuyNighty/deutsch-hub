package com.deutschhub.infrastructure.identity.web.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantContentEditorRequest(

        @NotNull
        UUID userId
) {
}
