package com.deutschhub.application.shared.authorization;

import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.util.Set;

public record CurrentActor(
        UserId userId,
        Set<RoleType> roles
) {
    public boolean hasRole(RoleType roleType) {
        return roles != null && roles.contains(roleType);
    }
}
