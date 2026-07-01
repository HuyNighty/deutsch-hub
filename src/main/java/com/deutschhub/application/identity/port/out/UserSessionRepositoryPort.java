package com.deutschhub.application.identity.port.out;

import com.deutschhub.domain.identity.model.aggregate.UserSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepositoryPort {

    UserSession save(UserSession userSession);

    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    void revokeAllByUserId(UUID userId);

    List<UserSession> findByUserId(UUID userId);

    Optional<UserSession> findById(UUID sessionId);
}
