package com.deutschhub.infrastructure.identity.web.controller;

import com.deutschhub.application.identity.dto.request.GetUsersQuery;
import com.deutschhub.application.identity.dto.response.UserDetailResponse;
import com.deutschhub.application.identity.dto.response.UserSummaryResponse;
import com.deutschhub.application.identity.port.in.GetUserDetailUseCase;
import com.deutschhub.application.identity.port.in.GetUsersUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserSummaryResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetUsersQuery query = new GetUsersQuery(page, size);

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
}