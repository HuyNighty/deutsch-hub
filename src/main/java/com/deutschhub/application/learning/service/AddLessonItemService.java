package com.deutschhub.application.learning.service;

import com.deutschhub.application.learning.dto.request.AddLessonItemCommand;
import com.deutschhub.application.learning.dto.response.LessonDetailResponse;
import com.deutschhub.application.learning.dto.response.LessonItemResponse;
import com.deutschhub.application.learning.port.in.AddLessonItemUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.media.port.out.MediaRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.domain.learning.model.enums.LessonItemType;
import com.deutschhub.domain.media.model.aggregate.Media;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddLessonItemService implements AddLessonItemUseCase {

    CourseRepositoryPort courseRepositoryPort;
    MediaRepositoryPort mediaRepositoryPort;

    @Override
    public LessonDetailResponse addLessonItem(AddLessonItemCommand command) {
        Course course = courseRepositoryPort.findById(command.courseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        LessonItem item = createLessonItem(command);

        Lesson lesson = course.addLessonItemToLesson(command.sectionId(), command.lessonId(), item,
                command.actorId(), command.admin());

        courseRepositoryPort.save(course);

        return toResponse(course, lesson);
    }

    private LessonItem createLessonItem(AddLessonItemCommand command) {
        LessonItemType type = command.type();

        return switch (type) {

            case TEXT -> LessonItem.createText(
                    command.title(),
                    command.description(),
                    command.content(),
                    command.estimatedMinutes(),
                    command.orderIndex()
            );

            case MEDIA -> {
                validateMedia(command.mediaId());

                yield LessonItem.createMedia(
                        command.type(),
                        command.title(),
                        command.description(),
                        command.mediaId(),
                        command.estimatedMinutes(),
                        command.orderIndex()
                );
            }

            case QUIZ -> LessonItem.createQuiz(
                    command.title(),
                    command.description(),
                    command.quizId(),
                    command.estimatedMinutes(),
                    command.orderIndex()
            );
        };
    }

    private Media validateMedia(UUID mediaId) {
       return mediaRepositoryPort.findById(mediaId)
               .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }

    private LessonDetailResponse toResponse(Course course, Lesson lesson) {
        List<LessonItemResponse> items = lesson.getItems()
                .stream()
                .filter(item -> !item.isDeleted())
                .sorted(Comparator.comparingInt(LessonItem::getOrderIndex))
                .map(this::toItemResponse)
                .toList();

        LessonNavigation navigation = resolveNavigation(course, lesson.getId());

        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getEstimatedMinutes(),
                lesson.getLevel().toString(),
                lesson.getOrderIndex(),
                lesson.isFreePreview(),
                false,
                navigation.previousLessonId,
                navigation.nextLessonId,
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
                item.getMediaId(),
                item.getQuizId(),
                item.getEstimatedMinutes(),
                item.getOrderIndex(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private LessonNavigation resolveNavigation(Course course, UUID lessonId) {
        List<Lesson> orderedLessons = course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .sorted(Comparator.comparingInt(Section::getOrderIndex))
                .flatMap(section -> section.getLessons()
                        .stream()
                        .filter(lesson -> !lesson.isDeleted())
                        .sorted(Comparator.comparingInt(Lesson::getOrderIndex)))
                .toList();

        for (int index = 0; index < orderedLessons.size(); index++) {
            Lesson currentLesson = orderedLessons.get(index);

            if (currentLesson.getId().equals(lessonId)) {
                UUID previousLessonId = index > 0 ? orderedLessons.get(index - 1).getId() : null;
                UUID nextLessonId = index < orderedLessons.size() - 1 ? orderedLessons.get(index + 1).getId() : null;

                return new LessonNavigation(previousLessonId, nextLessonId);
            }
        }

        return new LessonNavigation(null, null);
    }

    private record LessonNavigation(UUID previousLessonId, UUID nextLessonId) {
    }
}
