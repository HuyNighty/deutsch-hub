package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.RegisterUserUseCase;
import com.deutschhub.application.identity.port.out.PasswordEncoderPort;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;
import com.deutschhub.domain.identity.model.valueobject.Username;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class RegisterUserService implements RegisterUserUseCase {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoderPort passwordEncoderPort;

    @Override
    public UserResponse register(RegisterUserCommand command) {

        Username username = new Username(command.username());

        Email email = new Email(command.email());

        Password.validateStrength(command.password());

        String encodedPassword = passwordEncoderPort.encode(command.password());

        Password password = Password.fromHashed(encodedPassword);

        FullName fullName = new FullName(command.firstName(), command.lastName());

        if (userRepositoryPort.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepositoryPort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = User.register(
                username,
                email,
                password,
                fullName,
                command.phoneNumber()
        );

        User savedUser = userRepositoryPort.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getUsername().getValue(),
                savedUser.getEmail().getValue(), savedUser.getFullName().getFullName());
    }
}