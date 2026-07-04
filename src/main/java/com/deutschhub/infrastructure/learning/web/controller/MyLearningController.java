package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.CompleteLessonCommand;
import com.deutschhub.application.learning.dto.response.EnrollmentProgressResponse;
import com.deutschhub.application.learning.dto.response.MyCourseDetailResponse;
import com.deutschhub.application.learning.dto.response.MyCourseResponse;
import com.deutschhub.application.learning.port.in.CompleteLessonUseCase;
import com.deutschhub.application.learning.port.in.GetMyCourseDetailUseCase;
import com.deutschhub.application.learning.port.in.GetMyCourseProgressUseCase;
import com.deutschhub.application.learning.port.in.GetMyCoursesUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.infrastructure.learning.web.request.CompleteLessonRequest;
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
@RequestMapping("/api/v1/me/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyLearningController {

    GetMyCoursesUseCase getMyCoursesUseCase;
    GetMyCourseDetailUseCase getMyCourseDetailUseCase;
    CompleteLessonUseCase completeLessonUseCase;
    GetMyCourseProgressUseCase getMyCourseProgressUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MyCourseResponse>>> getMyCourses(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        List<MyCourseResponse> response = getMyCoursesUseCase.getMyCourses(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<MyCourseResponse>>builder()
                        .message("Get my courses successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<MyCourseDetailResponse>> getMyCourseDetail(@PathVariable UUID courseId,
                                                                                 @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        MyCourseDetailResponse response = getMyCourseDetailUseCase.getMyCourseDetail(
                userId,
                courseId
        );

        return ResponseEntity.ok(
                ApiResponse.<MyCourseDetailResponse>builder()
                        .message("Get my course detail successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<ApiResponse<EnrollmentProgressResponse>> completeLesson(@PathVariable UUID courseId,
                                                                                  @PathVariable UUID lessonId,
                                                                                  @AuthenticationPrincipal Jwt jwt,
                                                                                  @RequestBody @Valid CompleteLessonRequest request) {

        UUID userId = UUID.fromString(jwt.getSubject());

        CompleteLessonCommand command = new CompleteLessonCommand(userId, courseId, lessonId, request.studyMinutes());

        EnrollmentProgressResponse response = completeLessonUseCase.completeLesson(command);

        return ResponseEntity.ok(
                ApiResponse.<EnrollmentProgressResponse>builder()
                        .message("Complete lesson successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}/progress")
    public ResponseEntity<ApiResponse<EnrollmentProgressResponse>> getMyCourseProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        EnrollmentProgressResponse response = getMyCourseProgressUseCase.getProgress(
                userId,
                courseId
        );

        return ResponseEntity.ok(
                ApiResponse.<EnrollmentProgressResponse>builder()
                        .message("Get my course progress successfully")
                        .result(response)
                        .build()
        );
    }
}
