package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.request.LogoutCommand;
import com.deutschhub.application.identity.dto.request.RefreshTokenCommand;
import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.response.LoginResponse;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.identity.web.request.LoginUserRequest;
import com.deutschhub.infrastructure.identity.web.request.LogoutUserRequest;
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
    LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterUserRequest request) {

        RegisterUserCommand command = new RegisterUserCommand(request.username(),
                request.email(), request.password(), request.firstName(), request.lastName(),
                        request.phoneNumber());

        UserResponse response = registerUserUseCase.register(command);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                        .result(response)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginUserRequest request) {

        LoginUserCommand command = new LoginUserCommand(
                request.usernameOrEmail(),
                request.password()
        );

        LoginResponse response = loginUserUseCase.login(command);

        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                        .result(response)
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponse response = getMyProfileUseCase.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                        .result(response)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        RefreshTokenCommand command =  new RefreshTokenCommand(request.refreshToken());

        RefreshTokenResponse response = refreshTokenUseCase.refresh(command);

        return ResponseEntity.ok(ApiResponse.<RefreshTokenResponse>builder()
                        .result(response)
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid LogoutUserRequest request) {
        LogoutCommand command = new LogoutCommand(request.refreshToken());

        logoutUseCase.logout(command);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                        .message("Logout successfully")
                .build());
    }
}
