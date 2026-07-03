package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.response.MyCourseResponse;
import com.deutschhub.application.learning.port.in.GetMyCoursesUseCase;
import com.deutschhub.common.util.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyLearningController {

    GetMyCoursesUseCase getMyCoursesUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> getMyCourses(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        List<MyCourseResponse> response = getMyCoursesUseCase.getMyCourses(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<MyCourseResponse>>builder()
                        .message("Get my courses successfully")
                        .result(response)
                        .build()
        );
    }
}
