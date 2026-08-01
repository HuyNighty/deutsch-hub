package com.deutschhub.infrastructure.learning.web.controller;

import com.deutschhub.application.learning.dto.request.*;
import com.deutschhub.application.learning.dto.response.*;
import com.deutschhub.application.learning.port.in.*;
import com.deutschhub.common.util.ApiResponse;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.infrastructure.learning.web.request.*;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    GetCourseSectionUseCase getCourseSectionUseCase;
    UpdateSectionUseCase updateSectionUseCase;
    DeleteSectionUseCase deleteSectionUseCase;
    PublishCourseUseCase publishCourseUseCase;
    UnpublishCourseUseCase unpublishCourseUseCase;
    UpdateCourseUseCase updateCourseUseCase;
    AddLessonToSectionUseCase addLessonToSectionUseCase;
    GetSectionLessonsUseCase getSectionLessonsUseCase;
    UpdateLessonUseCase updateLessonUseCase;
    DeleteLessonUseCase deleteLessonUseCase;
    GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;
    GetEnrollmentDetailUseCase getEnrollmentDetailUseCase;
    AddLessonItemUseCase addLessonItemUseCase;


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
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetails(@PathVariable UUID courseId) {
        CourseDetailResponse response = getCourseDetailUseCase.getCourseDetail(courseId);

        return ResponseEntity.ok(
                ApiResponse.<CourseDetailResponse>builder()
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

        CourseResponse response = updateCourseUseCase.updateCourse(command);

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

    @GetMapping("/{courseId}/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSection(@PathVariable UUID courseId) {
        List<SectionResponse> responses = getCourseSectionUseCase.getSections(courseId);

        return ResponseEntity.ok(
                ApiResponse.<List<SectionResponse>>builder()
                        .message("Get section successfully")
                        .result(responses)
                        .build()
        );
    }

    @PatchMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(@PathVariable UUID courseId,
                                                                      @PathVariable UUID sectionId,
                                                                      @AuthenticationPrincipal Jwt jwt,
                                                                      @Valid @RequestBody UpdateSectionRequest request) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        UpdateSectionCommand command = new UpdateSectionCommand(courseId, sectionId, actorId,
                request.title(), request.description(), request.orderIndex(), true);

        SectionResponse response = updateSectionUseCase.updateSection(command);

        return ResponseEntity.ok(
                ApiResponse.<SectionResponse>builder()
                        .message("Update section successfully")
                        .result(response)
                        .build());
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        deleteSectionUseCase.deleteSection(
                courseId,
                sectionId,
                actorId,
                true
        );

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Delete section successfully")
                        .build()
        );
    }

    @PostMapping("/{courseId}/publish")
    public ResponseEntity<ApiResponse<CourseResponse>> publishCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        CourseResponse response = publishCourseUseCase.publishCourse(
                courseId,
                actorId,
                true
        );

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .message("Publish course successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{courseId}/unpublish")
    public ResponseEntity<ApiResponse<CourseResponse>> unpublishCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        CourseResponse response = unpublishCourseUseCase.unpublishCourse(
                courseId,
                actorId,
                true
        );

        return ResponseEntity.ok(
                ApiResponse.<CourseResponse>builder()
                        .message("Unpublish course successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<LessonResponse>> addLesson(
            @PathVariable UUID courseId,
            @PathVariable UUID sectionId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddLessonRequest request
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        AddLessonCommand command = new AddLessonCommand(
                courseId,
                sectionId,
                actorId,
                request.title(),
                request.description(),
                request.estimatedMinutes(),
                request.level(),
                request.orderIndex(),
                true
        );

        LessonResponse response = addLessonToSectionUseCase.addLessonToSection(command);

        return ResponseEntity.ok(
                ApiResponse.<LessonResponse>builder()
                        .message("Add lesson successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessons(@PathVariable UUID courseId,
                                                                        @PathVariable UUID sectionId) {
        List<LessonResponse> responses = getSectionLessonsUseCase.getLessons(courseId, sectionId);

        return ResponseEntity.ok(
                ApiResponse.<List<LessonResponse>>builder()
                        .message("Get lessons successfully")
                        .result(responses)
                        .build()
        );
    }

    @PatchMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(@PathVariable UUID courseId, @PathVariable UUID sectionId,
                                                                    @PathVariable UUID lessonId, @AuthenticationPrincipal Jwt jwt,
                                                                    @Valid @RequestBody UpdateLessonRequest request) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        UpdateLessonCommand command = new UpdateLessonCommand(courseId, sectionId, lessonId, actorId, request.title(),
                request.description(), request.estimatedMinutes(), request.level(), request.orderIndex(),
                request.freePreview(), true);

        LessonResponse response = updateLessonUseCase.updateLesson(command);

        return ResponseEntity.ok(
                ApiResponse.<LessonResponse>builder()
                        .message("Update lesson successfully")
                        .result(response)
                        .build()
        );
    }

    @DeleteMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable UUID courseId, @PathVariable UUID sectionId,
                                                          @PathVariable UUID lessonId, @AuthenticationPrincipal Jwt jwt) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        deleteLessonUseCase.deleteLesson(courseId, sectionId, lessonId, actorId, true);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Delete lesson successfully")
                        .build()
        );
    }

    @PostMapping("/{courseId}/sections/{sectionId}/lessons/{lessonId}/items")
    public ResponseEntity<ApiResponse<LessonDetailResponse>> addLessonItem(@PathVariable UUID courseId,
                                                                           @PathVariable UUID sectionId,
                                                                           @PathVariable UUID lessonId,
                                                                           @AuthenticationPrincipal Jwt jwt,
                                                                           @Valid @RequestBody AddLessonItemRequest request) {
        UUID actorId = UUID.fromString(jwt.getSubject());

        AddLessonItemCommand command = new AddLessonItemCommand(courseId, sectionId, lessonId, actorId, request.type(),
                request.title(), request.description(), request.content(), request.mediaId(), request.quizId(),
                request.estimatedMinutes(), request.orderIndex(), true
        );

        LessonDetailResponse response = addLessonItemUseCase.addLessonItem(command);

        return ResponseEntity.ok(
                ApiResponse.<LessonDetailResponse>builder()
                        .message("Add lesson item successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{courseId}/enrollments")
    public ResponseEntity<ApiResponse<List<AdminCourseEnrollmentResponse>>> getCourseEnrollments(
            @PathVariable UUID courseId
    ) {
        List<AdminCourseEnrollmentResponse> response =
                getCourseEnrollmentsUseCase.getCourseEnrollments(courseId);

        return ResponseEntity.ok(
                ApiResponse.<List<AdminCourseEnrollmentResponse>>builder()
                        .message("Get course enrollments successfully")
                        .result(response)
                        .build()
        );
    }
}
