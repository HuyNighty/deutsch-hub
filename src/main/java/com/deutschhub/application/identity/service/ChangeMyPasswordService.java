package com.deutschhub.application.identity.service;

import com.deutschhub.application.identity.dto.request.ChangeMyPasswordCommand;
import com.deutschhub.application.identity.port.in.ChangeMyPasswordUseCase;
import com.deutschhub.application.identity.port.out.PasswordEncoderPort;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.aggregate.User;
import com.deutschhub.domain.identity.valueobject.Password;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChangeMyPasswordService implements ChangeMyPasswordUseCase {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoderPort passwordEncoderPort;
    UserSessionRepositoryPort userSessionRepositoryPort;

    @Override
    public void changeMyPassword(ChangeMyPasswordCommand command) {
        User user = userRepositoryPort.findById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.validateCanLogin();

        boolean currentPasswordMatches = passwordEncoderPort.matches(
                command.currentPassword(),
                user.getPassword().getHashedValue()
        );

        if (!currentPasswordMatches) {
            throw new BusinessException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        if (!command.newPassword().equals(command.verifyNewPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRMATION_NOT_MATCH);
        }

        boolean samePassword = passwordEncoderPort.matches(
                command.newPassword(),
                user.getPassword().getHashedValue()
        );

        if (samePassword) {
            throw new BusinessException(ErrorCode.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }


        String newPasswordHash = passwordEncoderPort.encode(command.newPassword());

        user.changePassword(Password.fromHashed(newPasswordHash));

        userRepositoryPort.save(user);

        userSessionRepositoryPort.revokeAllByUserId(user.getId());
    }
}
