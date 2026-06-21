package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.request.RefreshTokenCommand;
import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.response.LoginResponse;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.GetMyProfileUseCase;
import com.deutschhub.application.identity.port.in.LoginUserUseCase;
import com.deutschhub.application.identity.port.in.RefreshTokenUseCase;
import com.deutschhub.application.identity.port.in.RegisterUserUseCase;
import com.deutschhub.infrastructure.identity.web.request.LoginUserRequest;
import com.deutschhub.infrastructure.identity.web.request.RefreshTokenRequest;
import com.deutschhub.infrastructure.identity.web.request.RegisterUserRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    RegisterUserUseCase registerUserUseCase;
    LoginUserUseCase loginUserUseCase;
    GetMyProfileUseCase getMyProfileUseCase;
    RefreshTokenUseCase refreshTokenUseCase;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterUserRequest request) {

        RegisterUserCommand command = new RegisterUserCommand(request.username(),
                request.email(), request.password(), request.firstName(), request.lastName(),
                        request.phoneNumber());

        UserResponse response = registerUserUseCase.register(command);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginUserRequest request) {

        LoginUserCommand command = new LoginUserCommand(
                request.usernameOrEmail(),
                request.password()
        );

        LoginResponse response = loginUserUseCase.login(command);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponse response = getMyProfileUseCase.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        RefreshTokenCommand command =  new RefreshTokenCommand(request.refreshToken());

        RefreshTokenResponse response = refreshTokenUseCase.refresh(command);

        return ResponseEntity.ok(response);
    }
}
