package com.deutschhub.application.learning.usecase;

import com.deutschhub.application.learning.dto.response.LessonItemMediaContentResponse;
import com.deutschhub.application.learning.port.in.GetMyLessonItemMediaUseCase;
import com.deutschhub.application.learning.port.out.CourseRepositoryPort;
import com.deutschhub.application.learning.port.out.EnrollmentRepositoryPort;
import com.deutschhub.application.media.port.out.MediaRepositoryPort;
import com.deutschhub.application.media.port.out.MediaStoragePort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.aggregate.Course;
import com.deutschhub.domain.learning.model.aggregate.Enrollment;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
import com.deutschhub.domain.learning.model.valueobject.LessonItemType;
import com.deutschhub.domain.media.model.aggregate.Media;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetMyLessonItemMediaService implements GetMyLessonItemMediaUseCase {

    EnrollmentRepositoryPort enrollmentRepositoryPort;
    CourseRepositoryPort courseRepositoryPort;
    MediaRepositoryPort mediaRepositoryPort;
    MediaStoragePort mediaStoragePort;

    @Override
    public LessonItemMediaContentResponse getMedia(UUID userId, UUID courseId, UUID lessonId, UUID itemId) {
        Enrollment enrollment = enrollmentRepositoryPort
                .findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (!enrollment.isActive()) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_ACTIVE);
        }

        Course course = courseRepositoryPort.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        if (course.isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Lesson lesson = course.getSections()
                .stream()
                .filter(section -> !section.isDeleted())
                .flatMap(section -> section.getLessons().stream())
                .filter(lessonItem -> !lessonItem.isDeleted())
                .filter(lessonItem -> lessonItem.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        LessonItem item = lesson.getItems()
                .stream()
                .filter(lessonItem -> !lessonItem.isDeleted())
                .filter(lessonItem -> lessonItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_ITEM_NOT_FOUND));

        if (item.getType() != LessonItemType.MEDIA || item.getMediaId() == null) {
            throw new BusinessException(ErrorCode.LESSON_ITEM_MEDIA_NOT_FOUND);
        }

        Media media = mediaRepositoryPort.findById(item.getMediaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));

        InputStream inputStream = mediaStoragePort.load(media.getStorageKey());

        return new LessonItemMediaContentResponse(inputStream, media.getMimeType(),
                media.getSizeBytes(), media.getOriginalFileName());
    }
}