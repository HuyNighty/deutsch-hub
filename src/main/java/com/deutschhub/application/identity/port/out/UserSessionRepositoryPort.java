package com.deutschhub.application.identity.port.out;

import com.deutschhub.domain.identity.model.aggregate.UserSession;

import java.util.Optional;

public interface UserSessionRepositoryPort {

    UserSession save(UserSession userSession);
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
}
