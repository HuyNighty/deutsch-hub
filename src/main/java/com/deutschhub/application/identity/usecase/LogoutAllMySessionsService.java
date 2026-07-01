package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.port.in.LogoutAllMySessionsUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class LogoutAllMySessionsService implements LogoutAllMySessionsUseCase {

    UserRepositoryPort userRepositoryPort;
    UserSessionRepositoryPort userSessionRepositoryPort;

    @Override
    public void logoutAll(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();
        userSessionRepositoryPort.revokeAllByUserId(userId);
    }
}
