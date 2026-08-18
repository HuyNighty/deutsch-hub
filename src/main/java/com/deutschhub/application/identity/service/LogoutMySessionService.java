package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.port.in.LogoutMySessionUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogoutMySessionService implements LogoutMySessionUseCase {

    UserRepositoryPort userRepositoryPort;
    UserSessionRepositoryPort userSessionRepositoryPort;

    @Override
    public void logoutMySession(UUID userId, UUID sessionId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        UserSession session = userSessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        session.revoke(LocalDateTime.now());

        userSessionRepositoryPort.save(session);
    }
}