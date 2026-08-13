package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.response.UserSessionResponse;
import com.deutschhub.application.identity.port.in.GetMySessionsUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.aggregate.UserSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMySessionsService implements GetMySessionsUseCase {

    UserSessionRepositoryPort userSessionRepositoryPort;
    UserRepositoryPort userRepositoryPort;

    @Override
    public List<UserSessionResponse> getMySessions(UUID userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        LocalDateTime now = LocalDateTime.now();

        return userSessionRepositoryPort.findByUserId(userId)
                .stream()
                .map(userSession -> toResponse(userSession, now))
                .toList();
    }

    private UserSessionResponse toResponse(UserSession userSession, LocalDateTime now) {
        return new UserSessionResponse(
                userSession.getId(),
                userSession.getCreatedAt(),
                userSession.getExpiresAt(),
                userSession.getRevokedAt(),
                userSession.isActive(now)
        );
    }
}
