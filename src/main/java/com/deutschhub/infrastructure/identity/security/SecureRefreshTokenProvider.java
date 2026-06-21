package com.deutschhub.infrastructure.identity.security;

import com.deutschhub.application.identity.port.out.GeneratedRefreshToken;
import com.deutschhub.application.identity.port.out.RefreshTokenProvider;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecureRefreshTokenProvider implements RefreshTokenProvider {

    private static final int TOKEN_SIZE_BYTES = 32;

    JwtProperties jwtProperties;
    SecureRandom secureRandom = new SecureRandom();

    @Override
    public GeneratedRefreshToken generate() {
        byte[] randomBytes = new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(randomBytes);

        String value = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String hash = hash(value);

        LocalDateTime expiresAt = LocalDateTime.now().plus(
                Duration.ofMillis(jwtProperties.refreshTokenExpiration()));

        return new GeneratedRefreshToken(value, hash, expiresAt);
    }

    @Override
    public String hash(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.HASH_TOKEN_CAN_NOT_NULL);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");

            byte[] hashedBytes = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-512 algorithm is not available", exception);
        }
    }
}

