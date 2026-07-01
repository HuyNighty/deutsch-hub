package com.deutschhub.application.identity.port.in;

import java.util.UUID;

public interface LogoutAllMySessionsUseCase {

    void logoutAll(UUID userId);
}
