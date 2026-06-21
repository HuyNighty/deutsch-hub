package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.response.LoginResponse;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.LoginUserUseCase;
import com.deutschhub.application.identity.port.out.*;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.identity.model.aggregate.User;
import com.deutschhub.domain.identity.model.aggregate.UserSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LoginUserService implements LoginUserUseCase {

    UserRepositoryPort userRepositoryPort;
    PasswordEncoderPort passwordEncoderPort;
    TokenGenerator tokenGenerator;
    UserSessionRepositoryPort userSessionRepositoryPort;
    RefreshTokenProvider refreshTokenProvider;

    @Override
    public LoginResponse login(LoginUserCommand command) {

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
        User savedUser = userRepositoryPort.save(user);
        GeneratedToken accessToken = tokenGenerator.generateAccessToken(savedUser);
        GeneratedRefreshToken refreshToken =
                refreshTokenProvider.generate();

        UserSession userSession = UserSession.create(savedUser.getId(),
                refreshToken.hash(), refreshToken.expiresAt());

        userSessionRepositoryPort.save(userSession);

        UserResponse userResponse =new UserResponse(user.getId(), user.getUsername().getValue(),
                user.getEmail().getValue(), user.getFullName().getFullName());

        return new LoginResponse(userResponse, accessToken.value(), refreshToken.value(), accessToken.expiresIn());
    }
}
