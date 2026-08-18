package com.deutschhub.application.shared.authorization;

import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.domain.shared.valueobject.UserId;

public record CurrentActor(
        UserId userId,
        RoleType roleType
) {
}
