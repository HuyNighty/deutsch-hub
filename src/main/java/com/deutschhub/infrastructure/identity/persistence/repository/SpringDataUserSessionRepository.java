package com.deutschhub.infrastructure.identity.persistence.repository;

import com.deutschhub.infrastructure.identity.persistence.entity.UserSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserSessionRepository extends JpaRepository<UserSessionJpaEntity, UUID> {

    Optional<UserSessionJpaEntity> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("""
            update UserSessionJpaEntity s
            set s.revokedAt = :revokedAt,
                s.updatedAt = :revokedAt
            where s.userId = :userId
              and s.revokedAt is null
            """)
    void revokeAllByUserId(
            @Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt);

    List<UserSessionJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
