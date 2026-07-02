package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.application.learning.port.in.PublishCourseUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublishCourseService implements PublishCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public CourseResponse publishCourse(UUID courseId, UUID actorId, boolean admin) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        course.publish(actorId, admin);

        Course savedCourse = courseRepositoryPort.save(course);

        return toResponse(savedCourse);
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
