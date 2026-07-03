package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.port.in.DeleteLessonUseCase;
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

@Transactional
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeleteLessonService implements DeleteLessonUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public void deleteLesson(UUID courseId, UUID sectionId, UUID lessonId, UUID actorId, boolean admin) {
        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        course.deleteLesson(sectionId, lessonId, actorId, admin);

        courseRepositoryPort.save(course);
    }
}
