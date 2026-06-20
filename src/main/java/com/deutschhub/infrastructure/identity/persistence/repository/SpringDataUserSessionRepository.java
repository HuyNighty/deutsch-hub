package com.deutschhub.infrastructure.identity.persistence.repository;

import com.deutschhub.infrastructure.identity.persistence.entity.UserSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserSessionRepository extends JpaRepository<UserSessionJpaEntity, UUID> {

    Optional<UserSessionJpaEntity> findByRefreshTokenHash(String refreshTokenHash);
}
