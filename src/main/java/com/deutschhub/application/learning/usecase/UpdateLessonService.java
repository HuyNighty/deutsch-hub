package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.UpdateLessonCommand;
import com.deutschhub.application.learning.dto.response.LessonResponse;
import com.deutschhub.application.learning.port.in.UpdateLessonUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateLessonService implements UpdateLessonUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public LessonResponse updateLesson(UpdateLessonCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        CEFRLevel level = command.level() == null ? null : new CEFRLevel(command.level());

        Lesson lesson = course.updateLesson(command.sectionId(),
                command.lessonId(),
                command.title(),
                command.description(),
                command.estimatedMinutes(),
                level,
                command.orderIndex(),
                command.freePreview(),
                command.actorId(),
                command.admin());

        courseRepositoryPort.save(course);

        return toResponse(lesson);
    }

    private LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getDescription(),
                lesson.getEstimatedMinutes(), lesson.getLevel().toString(), lesson.getOrderIndex(), lesson.isFreePreview(),
                lesson.getCreatedAt(),  lesson.getUpdatedAt()
        );
    }
}
