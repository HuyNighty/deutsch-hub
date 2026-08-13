package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.GetUsersQuery;
import com.deutschhub.application.identity.dto.request.UpdateUserRolesCommand;
import com.deutschhub.application.identity.dto.response.UserDetailResponse;
import com.deutschhub.application.identity.dto.response.UserSummaryResponse;
import com.deutschhub.application.identity.port.in.DeactivateUserUseCase;
import com.deutschhub.application.identity.port.in.GetUserDetailUseCase;
import com.deutschhub.application.identity.port.in.GetUsersUseCase;
import com.deutschhub.application.identity.port.in.UpdateUserRolesUseCase;
import com.deutschhub.application.identity.service.ActivateUserService;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.infrastructure.identity.web.request.UpdateUserRolesRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    GetUsersUseCase getUsersUseCase;
    GetUserDetailUseCase getUserDetailUseCase;
    DeactivateUserUseCase deactivateUserUseCase;
    ActivateUserService activateUserUseCase;
    UpdateUserRolesUseCase updateUserRolesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetUsersQuery query = new GetUsersQuery(keyword, page, size);

        PageResponse<UserSummaryResponse> response = getUsersUseCase.getUsers(query);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<UserSummaryResponse>>builder()
                        .message("Get users successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable UUID userId) {
        UserDetailResponse response = getUserDetailUseCase.getUserDetail(userId);

        return ResponseEntity.ok(
                ApiResponse.<UserDetailResponse>builder()
                        .message("Get user detail successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<UserDetailResponse>> deactivateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentAdminId = UUID.fromString(jwt.getSubject());

        UserDetailResponse response = deactivateUserUseCase.deactivate(userId, currentAdminId);

        return ResponseEntity.ok(
                ApiResponse.<UserDetailResponse>builder()
                        .message("Deactivate user successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<UserDetailResponse>> activateUser(@PathVariable UUID userId) {
        UserDetailResponse response = activateUserUseCase.activate(userId);

        return ResponseEntity.ok(
                ApiResponse.<UserDetailResponse>builder()
                        .message("Activate user successfully")
                        .result(response)
                        .build()
        );
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserRoles(
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdateUserRolesRequest request
    ) {
        UUID currentAdminId = UUID.fromString(jwt.getSubject());

        UpdateUserRolesCommand command = new UpdateUserRolesCommand(
                userId,
                currentAdminId,
                request.roles()
        );

        UserDetailResponse response = updateUserRolesUseCase.updateRoles(command);

        return ResponseEntity.ok(
                ApiResponse.<UserDetailResponse>builder()
                        .message("Update user roles successfully")
                        .result(response)
                        .build()
        );
    }
}