package com.deutschhub.application.identity.dto.request;

import java.util.UUID;

public record UpdateMyProfileCommand(
        UUID userId,
        String firstName,
        String lastName,
        String phoneNumber
) {
}
