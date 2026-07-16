package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.LessonDetailResponse;
import com.deutschhub.application.learning.dto.response.LessonItemResponse;
import com.deutschhub.application.learning.port.in.GetMyLessonDetailUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.application.learning.port.out.LessonCompletionRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMyLessonDetailService implements GetMyLessonDetailUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;
    CourseRepositoryPort courseRepositoryPort;
    LessonCompletionRepositoryPort lessonCompletionRepositoryPort;

    @Override
    public LessonDetailResponse getMyLessonDetail(UUID userId, UUID courseId, UUID lessonId) {
        Enrollment enrollment = enrollmentRepositoryPort.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.isActive()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }

        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Lesson lesson = findActiveLesson(course, lessonId);

        boolean completed = lessonCompletionRepositoryPort.existsByEnrollmentIdAndLessonId(
                enrollment.getId(),
                lesson.getId()
        );

        return toResponse(lesson, completed);
    }

    private Lesson findActiveLesson(Course course, UUID lessonId) {
        return course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .flatMap(section -> section.getLessons().stream())
                .filter(lesson -> lesson.getId().equals(lessonId))
                .filter(lesson -> !lesson.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));
    }

    private LessonDetailResponse toResponse(Lesson lesson, boolean completed) {
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
                completed,
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
