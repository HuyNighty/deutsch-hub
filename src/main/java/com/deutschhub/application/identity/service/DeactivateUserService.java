package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.response.UserDetailResponse;
import com.deutschhub.application.identity.port.in.DeactivateUserUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeactivateUserService implements DeactivateUserUseCase {

    UserRepositoryPort userRepositoryPort;

    @Override
    public UserDetailResponse deactivate(UUID userId, UUID currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new BusinessException(ErrorCode.CANNOT_DEACTIVATE_YOURSELF);
        }

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.deactivate();

        User savedUser = userRepositoryPort.save(user);

        return toResponse(savedUser);
    }

    private UserDetailResponse toResponse(User user) {
        Set<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserDetailResponse(
                user.getId(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getFullName().getFullName(),
                user.getPhoneNumber(),
                user.isActive(),
                roles,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }
}
