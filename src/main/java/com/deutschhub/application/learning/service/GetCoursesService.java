package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.GetCoursesUseCase;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetCoursesService implements GetCoursesUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public PageResponse<CourseResponse> getCourses(GetCoursesQuery query) {
        PageResponse<Course> courses = courseRepositoryPort.findAll(
                query.keyword(),
                query.page(),
                query.size()
        );

        return PageResponse.<CourseResponse>builder()
                .items(courses.items()
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .page(courses.page())
                .size(courses.size())
                .totalElements(courses.totalElements())
                .totalPages(courses.totalPages())
                .build();
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel().toString(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.isPublished(),
                course.getInstructorId(),
                course.getEstimatedHours(),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
