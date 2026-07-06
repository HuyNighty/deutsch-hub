package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.response.AdminEnrollmentDetailResponse;
import com.deutschhub.application.learning.port.in.GetEnrollmentDetailUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/enrollments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminEnrollmentController {

    GetEnrollmentDetailUseCase getEnrollmentDetailUseCase;

    @GetMapping("/{enrollmentId}")
    public ResponseEntity<ApiResponse<AdminEnrollmentDetailResponse>> getEnrollmentDetail(
            @PathVariable UUID enrollmentId
    ) {
        AdminEnrollmentDetailResponse response =
                getEnrollmentDetailUseCase.getEnrollmentDetail(enrollmentId);

        return ResponseEntity.ok(
                ApiResponse.<AdminEnrollmentDetailResponse>builder()
                        .message("Get enrollment detail successfully")
                        .result(response)
                        .build()
        );
    }
}