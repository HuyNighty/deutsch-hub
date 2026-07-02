package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.GetPublishedCoursesUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.util.PageResponse;
import com.deutschhub.domain.learning.model.aggregate.Course;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetPublishedCoursesService implements GetPublishedCoursesUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public PageResponse<CourseResponse> getPublishedCourses(GetCoursesQuery query) {
        PageResponse<Course> page = courseRepositoryPort.findPublishedCourses(
                query.keyword(),
                query.page(),
                query.size()
        );

        return PageResponse.<CourseResponse>builder()
                .items(page.items()
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .page(page.page())
                .size(page.size())
                .totalElements(page.totalElements())
                .totalPages(page.totalPages())
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