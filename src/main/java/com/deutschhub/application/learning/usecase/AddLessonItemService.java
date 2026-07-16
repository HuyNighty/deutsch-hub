package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.request.AddLessonItemCommand;
import com.deutschhub.application.learning.dto.response.LessonDetailResponse;
import com.deutschhub.application.learning.dto.response.LessonItemResponse;
import com.deutschhub.application.learning.port.in.AddLessonItemUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
import com.deutschhub.domain.learning.model.valueobject.LessonItemType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddLessonItemService implements AddLessonItemUseCase {

    CourseRepositoryPort courseRepositoryPort;

    @Override
    public LessonDetailResponse addLessonItem(AddLessonItemCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        LessonItem item = createLessonItem(command);

        Lesson lesson = course.addLessonItemToLesson(command.sectionId(), command.lessonId(), item,
                command.actorId(), command.admin());

        courseRepositoryPort.save(course);

        return toResponse(lesson);
    }

    private LessonItem createLessonItem(AddLessonItemCommand command) {
        LessonItemType type = toType(command.type());

        if (type.requiresContent()) {
            return LessonItem.createText(
                    command.title(),
                    command.description(),
                    command.content(),
                    command.estimatedMinutes(),
                    command.orderIndex()
            );
        }

        if (type.requiresResourceUrl()) {
            return LessonItem.createResource(
                    type,
                    command.title(),
                    command.description(),
                    command.resourceUrl(),
                    command.estimatedMinutes(),
                    command.orderIndex()
            );
        }

        return LessonItem.createQuiz(
                command.title(),
                command.description(),
                command.quizId(),
                command.estimatedMinutes(),
                command.orderIndex()
        );
    }

    private LessonItemType toType(String type) {
        try {
            return LessonItemType.valueOf(type.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }
    }

    private LessonDetailResponse toResponse(Lesson lesson) {
        List<LessonItemResponse> items = lesson.getItems()
                .stream()
                .filter(item -> !item.isDeleted())
                .sorted(Comparator.comparingInt(LessonItem::getOrderIndex))
                .map(this::toItemResponse)
                .toList();

        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getContent(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                false,
                items,
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private LessonItemResponse toItemResponse(LessonItem item) {
        return new LessonItemResponse(
                item.getId(),
                item.getType().name(),
                item.getTitle(),
                item.getDescription(),
                item.getContent(),
                item.getResourceUrl(),
                item.getQuizId(),
                item.getEstimatedMinutes(),
                item.getOrderIndex(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
