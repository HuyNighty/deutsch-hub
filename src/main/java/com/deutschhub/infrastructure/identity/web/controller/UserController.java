package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.ChangeMyPasswordCommand;
import com.deutschhub.application.identity.dto.request.DeactivateMyAccountCommand;
import com.deutschhub.application.identity.dto.request.UpdateMyProfileCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;
import com.deutschhub.application.identity.dto.response.UserSessionResponse;
import com.deutschhub.application.identity.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.identity.web.request.ChangeMyPasswordRequest;
import com.deutschhub.infrastructure.identity.web.request.DeactivateMyAccountRequest;
import com.deutschhub.infrastructure.identity.web.request.UpdateMyProfileRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UpdateMyProfileUseCase updateMyProfileUseCase;
    ChangeMyPasswordUseCase changeMyPasswordUseCase;
    LogoutAllMySessionsUseCase logoutAllMySessionsUseCase;
    GetMySessionsUseCase getMySessionsUseCase;
    LogoutMySessionUseCase logoutMySessionUseCase;
    DeactivateMyAccountUseCase deactivateMyAccountUseCase;

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdateMyProfileRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        UpdateMyProfileCommand command = new UpdateMyProfileCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.phoneNumber()
        );

        UserResponse response = updateMyProfileUseCase.updateMyProfile(command);

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .message("Update profile successfully")
                        .result(response)
                        .build()
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changeMyPassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChangeMyPasswordRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        ChangeMyPasswordCommand command = new ChangeMyPasswordCommand(
                userId,
                request.currentPassword(),
                request.newPassword(),
                request.verifyNewPassword()
        );

        changeMyPasswordUseCase.changeMyPassword(command);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Change password successfully")
                        .build()
        );
    }

    @PostMapping("/me/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllMySessions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        logoutAllMySessionsUseCase.logoutAll(userId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Logout all sessions successfully")
                        .build()
        );
    }
    @GetMapping("/me/sessions")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getMySessions(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        List<UserSessionResponse> response = getMySessionsUseCase.getMySessions(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<UserSessionResponse>>builder()
                        .message("Get my sessions successfully")
                        .result(response)
                        .build()
        );
    }

    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> logoutMySession(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID sessionId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        logoutMySessionUseCase.logoutMySession(userId, sessionId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Logout session successfully")
                        .build()
        );
    }

    @PatchMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMyAccount(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid DeactivateMyAccountRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        DeactivateMyAccountCommand command = new DeactivateMyAccountCommand(
                userId,
                request.password()
        );

        deactivateMyAccountUseCase.deactivateMyAccount(command);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Deactivate account successfully")
                        .build()
        );
    }
}