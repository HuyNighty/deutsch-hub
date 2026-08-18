package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.RefreshTokenCommand;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;
import com.deutschhub.application.identity.port.in.RefreshTokenUseCase;
import com.deutschhub.application.identity.port.out.*;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import com.deutschhub.domain.identity.aggregate.UserSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService implements RefreshTokenUseCase {

    UserSessionRepositoryPort userSessionRepositoryPort;
    UserRepositoryPort userRepositoryPort;
    RefreshTokenProvider refreshTokenProvider;
    TokenGenerator tokenGenerator;

    @Override
    public RefreshTokenResponse refresh(RefreshTokenCommand command) {
        String currentTokenHash = refreshTokenProvider.hash(command.refreshToken());

        UserSession session = userSessionRepositoryPort
                .findByRefreshTokenHash(currentTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        session.validateCanRefresh(LocalDateTime.now());

        User user = userRepositoryPort.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        GeneratedToken newAccessToken = tokenGenerator.generateAccessToken(user);

        GeneratedRefreshToken newRefreshToken = refreshTokenProvider.generate();

        session.rotateRefreshToken(newRefreshToken.hash());

        userSessionRepositoryPort.save(session);

        return new RefreshTokenResponse(
                newAccessToken.value(),
                newRefreshToken.value(),
                newAccessToken.expiresIn()
        );
    }
}
