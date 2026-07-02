package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.AddSectionCommand;
import com.deutschhub.application.learning.dto.request.CreateCourseCommand;
import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.request.UpdateCourseCommand;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.dto.response.SectionResponse;
import com.deutschhub.application.learning.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.infrastructure.learning.web.request.AddSectionRequest;
import com.deutschhub.infrastructure.learning.web.request.CreateCourseRequest;
import com.deutschhub.infrastructure.learning.web.request.UpdateCourseRequest;
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


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCourseController {

    CreateCourseUseCase createCourseUseCase;
    GetCoursesUseCase getCoursesUseCase;
    GetCourseDetailUseCase getCourseDetailUseCase;
    DeleteCourseUseCase deleteCourseUseCase;
    AddSectionToCourseUseCase addSectionToCourseUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCourseRequest request) {

        UUID instructorId = UUID.fromString(jwt.getSubject());

        CreateCourseCommand command = new CreateCourseCommand(
                request.title(),
                request.description(),
                request.level(),
                request.price(),
                request.currency(),
                instructorId
        );

        CourseResponse response = createCourseUseCase.createCourse(command);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .message("Course created successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        GetCoursesQuery query = new GetCoursesQuery(keyword, page, size);

        PageResponse<CourseResponse> response = getCoursesUseCase.getCourses(query);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CourseResponse>>builder()
                        .message("Get courses successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDetails(@PathVariable UUID courseId) {
        CourseResponse response = getCourseDetailUseCase.getCourseDetail(courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .message("Get course detail successfully")
                        .result(response)
                        .build()
        );
    }

    @PatchMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID courseId,
                                                                    @AuthenticationPrincipal Jwt jwt,
                                                                    @Valid @RequestBody UpdateCourseRequest request) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        UpdateCourseCommand command = new UpdateCourseCommand(
                courseId,
                actorId,
                request.title(),
                request.description(),
                request.level(),
                request.price(),
                request.currency(),
                true
        );

        CourseResponse response = getCourseDetailUseCase.getCourseDetail(courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .message("Update course successfully")
                        .result(response)
                        .build()
        );
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        deleteCourseUseCase.deleteCourse(courseId, actorId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Delete course successfully")
                        .build()
        );
    }

    @PostMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> addSection(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddSectionRequest request
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        AddSectionCommand command = new AddSectionCommand(
                courseId,
                actorId,
                request.title(),
                request.description(),
                request.orderIndex(),
                true
        );

        SectionResponse response = addSectionToCourseUseCase.addSection(command);

        return ResponseEntity.ok(
                ApiResponse.<SectionResponse>builder()
                        .message("Add section successfully")
                        .result(response)
                        .build()
        );
    }
}
