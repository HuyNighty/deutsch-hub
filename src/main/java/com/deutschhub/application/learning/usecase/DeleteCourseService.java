package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.port.in.DeleteCourseUseCase;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.infrastructure.learning.port.out.CourseRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteCourseService implements DeleteCourseUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public void deleteCourse(UUID courseId, UUID actorId) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        course.softDelete(actorId, true);

        courseRepositoryPort.save(course);
    }
}
