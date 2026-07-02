package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseDetailResponse;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.GetPublishedCourseDetailUseCase;
import com.deutschhub.application.learning.port.in.GetPublishedCoursesUseCase;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    GetPublishedCoursesUseCase getPublishedCoursesUseCase;
    GetPublishedCourseDetailUseCase getPublishedCourseDetailUseCase;

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
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(@PathVariable UUID courseId) {
        CourseDetailResponse response = getPublishedCourseDetailUseCase.getPublishedCourseDetail(courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseDetailResponse>builder()
                        .message("Get course detail successfully")
                        .result(response)
                        .build()
        );
    }
}