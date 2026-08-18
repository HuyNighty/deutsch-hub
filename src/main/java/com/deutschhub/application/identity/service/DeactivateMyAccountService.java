package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.DeactivateMyAccountCommand;
import com.deutschhub.application.identity.port.in.DeactivateMyAccountUseCase;
import com.deutschhub.application.identity.port.out.PasswordEncoderPort;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeactivateMyAccountService implements DeactivateMyAccountUseCase {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoderPort passwordEncoderPort;
    UserSessionRepositoryPort userSessionRepositoryPort;

    @Override
    public void deactivateMyAccount(DeactivateMyAccountCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        boolean passwordMatches = passwordEncoderPort.matches(
                command.password(),
                user.getPassword().getHashedValue()
        );

        if (!passwordMatches) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        user.deactivate();

        userRepositoryPort.save(user);

        userSessionRepositoryPort.revokeAllByUserId(user.getId());
    }
}