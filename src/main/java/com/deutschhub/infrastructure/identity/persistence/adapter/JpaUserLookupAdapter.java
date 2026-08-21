package com.deutschhub.infrastructure.identity.persistence.adapter;

import com.deutschhub.application.shared.identity.UserLookupPort;
import com.deutschhub.domain.identity.enums.RoleType;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.identity.persistence.repository.SpringDataUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JpaUserLookupAdapter implements UserLookupPort {

    SpringDataUserRepository springDataUserRepository;

    @Override
    public boolean isActiveContentEditor(UserId userId) {
        if (userId == null) {
            return false;
        }

        return springDataUserRepository.existsActiveContentEditor(userId.value(), RoleType.CONTENT_EDITOR);
    }
}
