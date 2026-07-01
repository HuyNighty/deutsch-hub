package com.deutschhub.application.identity.port.in;

import java.util.UUID;

public interface LogoutMySessionUseCase {

    void logoutMySession(UUID userId, UUID sessionId);
}
