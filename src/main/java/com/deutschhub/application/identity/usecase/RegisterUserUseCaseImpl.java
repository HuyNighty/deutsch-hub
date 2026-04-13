package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.port.in.RegisterUserUseCase;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    @Override
    public User register(RegisterUserCommand command) {

        if (userRepositoryPort.existsByEmail(command.email().getValue())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "email");
        }

        if (userRepositoryPort.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "username");
        }

        User user = new User(
                command.username(),
                command.email(),
                command.password(),
                command.fullName(),
                command.phoneNumber()
        );

        return userRepositoryPort.save(user);
    }
}
