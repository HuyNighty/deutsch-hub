package com.deutschhub.infrastructure.shared.authorization;

import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.identity.security.JwtRoleConverter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SpringSecurityCurrentActorAdapter implements CurrentActorPort {

    JwtRoleConverter jwtRoleConverter;

    @Override
    public CurrentActor getCurrentActor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }

        UUID userId = UUID.fromString(authentication.getName());

        Set<RoleType> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .map(RoleType::valueOf)
                .collect(java.util.stream.Collectors.toSet());

        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCode.CONTENT_FORBIDDEN_ACTION);
        }
        return new CurrentActor(UserId.of(userId), roles);
    }
}
