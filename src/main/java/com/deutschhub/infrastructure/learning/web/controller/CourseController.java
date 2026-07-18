package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.*;
import com.deutschhub.application.learning.port.in.EnrollCourseUseCase;
import com.deutschhub.application.learning.port.in.GetPublishedCourseDetailUseCase;
import com.deutschhub.application.learning.port.in.GetPublishedCoursesUseCase;
import com.deutschhub.application.learning.port.in.GetViewerCourseDetailUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    GetPublishedCoursesUseCase getPublishedCoursesUseCase;
    GetPublishedCourseDetailUseCase getPublishedCourseDetailUseCase;
    EnrollCourseUseCase enrollCourseUseCase;
    GetViewerCourseDetailUseCase getViewerCourseDetailUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetCoursesQuery query = new GetCoursesQuery(keyword, page, size);

        PageResponse<CourseResponse> response = getPublishedCoursesUseCase.getPublishedCourses(query);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CourseResponse>>builder()
                        .message("Get courses successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<PublishedCourseDetailResponse>> getCourseDetail(@PathVariable UUID courseId) {
        PublishedCourseDetailResponse response = getPublishedCourseDetailUseCase.getPublishedCourseDetail(courseId);

        return ResponseEntity.ok(
                ApiResponse.<PublishedCourseDetailResponse>builder()
                        .message("Get course detail successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        EnrollmentResponse response = enrollCourseUseCase.enroll(userId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<EnrollmentResponse>builder()
                        .message("Enroll course successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}/viewer")
    public ResponseEntity<ApiResponse<ViewerCourseDetailResponse>> getViewerCourseDetail(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID viewerId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;

        ViewerCourseDetailResponse response = getViewerCourseDetailUseCase.getViewerCourseDetail(
                courseId,
                viewerId
        );

        return ResponseEntity.ok(
                ApiResponse.<ViewerCourseDetailResponse>builder()
                        .message("Get viewer course detail successfully")
                        .result(response)
                        .build()
        );
    }
}