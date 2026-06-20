package com.deutschhub.infrastructure.identity.persistence.adapter;

import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.domain.identity.model.aggregate.UserSession;
import com.deutschhub.infrastructure.identity.persistence.entity.UserSessionJpaEntity;
import com.deutschhub.infrastructure.identity.persistence.repository.SpringDataUserSessionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaUserSessionRepositoryAdapter implements UserSessionRepositoryPort {

    SpringDataUserSessionRepository repository;

    @Override
    public UserSession save(UserSession userSession) {
        UserSessionJpaEntity entity = toEntity(userSession);
        UserSessionJpaEntity saved = repository.save(entity);

        return toDomain(saved);
    }

    @Override
    public Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash) {
        return repository.findByRefreshTokenHash(refreshTokenHash).map(this::toDomain);
    }

    private UserSessionJpaEntity toEntity(UserSession userSession) {
        return UserSessionJpaEntity.builder()
                .id(userSession.getId())
                .userId(userSession.getUserId())
                .refreshTokenHash(userSession.getRefreshTokenHash())
                .createdAt(userSession.getCreatedAt())
                .updatedAt(userSession.getUpdatedAt())
                .expiresAt(userSession.getExpiresAt())
                .revokedAt(userSession.getRevokedAt())
                .build();
    }

    private UserSession toDomain(UserSessionJpaEntity entity) {
        return UserSession.restore(
                entity.getId(),
                entity.getUserId(),
                entity.getRefreshTokenHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }
}
