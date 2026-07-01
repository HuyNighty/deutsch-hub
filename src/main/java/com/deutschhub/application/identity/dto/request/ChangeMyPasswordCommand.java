package com.deutschhub.application.identity.dto.request;

import java.util.UUID;

public record ChangeMyPasswordCommand(
        UUID userId,
        String currentPassword,
        String newPassword,
        String verifyNewPassword
) {
}
