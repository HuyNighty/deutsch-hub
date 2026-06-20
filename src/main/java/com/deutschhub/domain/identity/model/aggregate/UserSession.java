package com.deutschhub.domain.identity.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserSession implements Auditable {
    private final UUID id;
    private final UUID userId;
    private String refreshTokenHash;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    private UserSession(
            UUID id,
            UUID userId,
            String refreshTokenHash,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt
    ) {
        if (id == null) {
            throw new BusinessException(ErrorCode.SESSION_ID_CAN_NOT_NULL);
        }

        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_ID_CAN_NOT_NULL);
        }

        validateRefreshTokenHash(refreshTokenHash);

        if (createdAt == null || expiresAt == null) {
            throw new BusinessException(ErrorCode.INVALID_SESSION_TIME);
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new BusinessException(ErrorCode.INVALID_SESSION_EXPIRATION);
        }

        this.id = id;
        this.userId = userId;
        this.refreshTokenHash = refreshTokenHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }


    public static UserSession create(
            UUID userId,
            String refreshTokenHash,
            LocalDateTime expiresAt
    ) {
        LocalDateTime now = LocalDateTime.now();

        return new UserSession(
                UUID.randomUUID(),
                userId,
                refreshTokenHash,
                now,
                now,
                expiresAt,
                null
        );
    }

    public static UserSession restore(
            UUID id,
            UUID userId,
            String refreshTokenHash,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt
    ) {
        return new UserSession(
                id,
                userId,
                refreshTokenHash,
                createdAt,
                updatedAt,
                expiresAt,
                revokedAt
        );
    }

    public void rotateRefreshToken(String newRefreshTokenHash) {
        validateCanRefresh(LocalDateTime.now());
        validateRefreshTokenHash(newRefreshTokenHash);

        this.refreshTokenHash = newRefreshTokenHash;
        touch();
    }

    public void revoke(LocalDateTime revokedAt) {
        if (isRevoked()) {
            return;
        }

        if (revokedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_SESSION_TIME);
        }

        this.revokedAt = revokedAt;
        this.updatedAt = revokedAt;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isActive(LocalDateTime now) {
        return !isRevoked() && !isExpired(now);
    }

    public void validateCanRefresh(LocalDateTime now) {
        if (isRevoked()) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }

        if (isExpired(now)) {
            throw new BusinessException(ErrorCode.SESSION_EXPIRED);
        }
    }

    private static void validateRefreshTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new BusinessException(ErrorCode.HASH_TOKEN_CAN_NOT_NULL);
        }
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
