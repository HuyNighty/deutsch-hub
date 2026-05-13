package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.LoginUserUseCase;
import com.deutschhub.application.identity.port.out.PasswordEncoderPort;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginUserService implements LoginUserUseCase {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoderPort passwordEncoderPort;

    @Override
    public UserResponse login(LoginUserCommand command) {

        Optional<User> userOptional;

        if (command.usernameOrEmail().contains("@")) {
            userOptional = userRepositoryPort.findByEmail(command.usernameOrEmail());
        }
        else  {
            userOptional = userRepositoryPort.findByUsername(command.usernameOrEmail());
        }

        User user = userOptional.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        boolean matches =  passwordEncoderPort.matches(
                command.password(),
                user.getPassword().getHashedValue()
        );

        if (!matches) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        user.updateLastLogin();

        return new UserResponse(user.getId(), user.getUsername().getValue(),
                user.getEmail().getValue(), user.getFullName().getFullName());
    }
}
