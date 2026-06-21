package com.deutschhub.application.identity.usecase;

import com.deutschhub.application.identity.dto.request.RefreshTokenCommand;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;
import com.deutschhub.application.identity.port.in.RefreshTokenUseCase;
import com.deutschhub.application.identity.port.out.RefreshTokenProvider;
import com.deutschhub.application.identity.port.out.TokenGenerator;
import com.deutschhub.application.identity.port.out.UserRepositoryPort;
import com.deutschhub.application.identity.port.out.UserSessionRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenService implements RefreshTokenUseCase {

    UserSessionRepositoryPort userSessionRepositoryPort;
    UserRepositoryPort userRepositoryPort;
    RefreshTokenProvider refreshTokenProvider;
    TokenGenerator tokenGenerator;

    @Override
    public RefreshTokenResponse refresh(RefreshTokenCommand command) {
        return null;
    }
}
