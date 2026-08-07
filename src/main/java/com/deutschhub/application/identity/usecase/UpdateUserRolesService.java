package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.UpdateUserRolesCommand;
import com.deutschhub.application.identity.dto.response.UserDetailResponse;
import com.deutschhub.application.identity.port.in.UpdateUserRolesUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.enums.RoleType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateUserRolesService implements UpdateUserRolesUseCase {

    UserRepositoryPort userRepositoryPort;

    @Override
    public UserDetailResponse updateRoles(UpdateUserRolesCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<RoleType> roles = parseRoles(command.roles());

        if (command.userId().equals(command.currentAdminId())
                && !roles.contains(RoleType.ADMIN)) {
            throw new BusinessException(ErrorCode.CANNOT_REMOVE_YOUR_OWN_ADMIN_ROLE);
        }

        user.replaceRoles(roles);

        User savedUser = userRepositoryPort.save(user);

        return toResponse(savedUser);
    }

    private Set<RoleType> parseRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_MUST_HAVE_AT_LEAST_ONE_ROLE);
        }

        return roles.stream()
                .map(this::parseRole)
                .collect(Collectors.toSet());
    }

    private RoleType parseRole(String role) {
        try {
            return RoleType.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_ROLE_NAME);
        }
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
