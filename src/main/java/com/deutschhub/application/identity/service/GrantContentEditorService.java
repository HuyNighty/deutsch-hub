package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.GrantContentEditorCommand;
import com.deutschhub.application.identity.dto.response.GrantContentEditorResponse;
import com.deutschhub.application.identity.port.in.GrantContentEditorUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import com.deutschhub.domain.identity.enums.RoleType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class GrantContentEditorService implements GrantContentEditorUseCase {

    UserRepositoryPort userRepositoryPort;
    CurrentActorPort currentActorPort;

    @Override
    public GrantContentEditorResponse grantContentEditor(GrantContentEditorCommand command) {
        if (command == null || command.userId() == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        if (!actor.hasRole(RoleType.ADMIN)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_DEACTIVATED);
        }

        user.addRole(RoleType.CONTENT_EDITOR);

        User savedUser = userRepositoryPort.save(user);

        return toResponse(savedUser);
    }

    private GrantContentEditorResponse toResponse(User user) {
        return new GrantContentEditorResponse(user.getId(), user.getUsername().getValue(), user.getRoles(), user.isActive());
    }
}
