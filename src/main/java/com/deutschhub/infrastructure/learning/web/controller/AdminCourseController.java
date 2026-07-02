package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.CreateCourseCommand;
import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.CreateCourseUseCase;
import com.deutschhub.application.learning.port.in.GetCoursesUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.infrastructure.learning.web.request.CreateCourseRequest;
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
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCourseController {

    CreateCourseUseCase createCourseUseCase;
    GetCoursesUseCase getCoursesUseCase;

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
}
