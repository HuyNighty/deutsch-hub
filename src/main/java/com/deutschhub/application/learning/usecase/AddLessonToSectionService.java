package com.deutschhub.application.learning.usecase;
import com.deutschhub.application.learning.dto.request.AddLessonCommand;
import com.deutschhub.application.learning.dto.response.LessonResponse;
import com.deutschhub.application.learning.port.in.AddLessonToSectionUseCase;
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
public class AddLessonToSectionService implements AddLessonToSectionUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public LessonResponse addLessonToSection(AddLessonCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        Lesson lesson = Lesson.create(
                command.title(),
                command.description(),
                command.estimatedMinutes(),
                new CEFRLevel(command.level()),
                command.orderIndex()
        );

        Lesson addLesson = course.addLessonToSection(command.sectionId(), lesson, command.actorId(), command.admin());

        courseRepositoryPort.save(course);

        return toResponse(addLesson);
    }

    private LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
