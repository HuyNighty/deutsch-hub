package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.LogoutCommand;
import com.deutschhub.application.identity.port.in.LogoutUseCase;
import com.deutschhub.application.identity.port.out.RefreshTokenProvider;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.UserSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogoutService implements LogoutUseCase {

    UserSessionRepositoryPort userSessionRepositoryPort;
    RefreshTokenProvider refreshTokenProvider;

    @Override
    public void logout(LogoutCommand command) {
        String refreshTokenHash = refreshTokenProvider.hash(command.refreshToken());

        UserSession userSession = userSessionRepositoryPort.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        userSession.revoke(LocalDateTime.now());

        userSessionRepositoryPort.save(userSession);
    }
}
