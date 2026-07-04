package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.UUID;

public class LessonCompletion {

    private final UUID id;
    private final UUID enrollmentId;
    private final UUID lessonId;
    private final LocalDateTime completionAt;

    private LessonCompletion(UUID id, UUID enrollmentId, UUID lessonId, LocalDateTime completionAt) {
        if (id == null) {
            throw new BusinessException(ErrorCode.LESSON_COMPLETION_ID_CAN_NOT_NULL);
        }

        if (enrollmentId == null) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ID_CAN_NOT_NULL);
        }

        if (lessonId == null) {
            throw new BusinessException(ErrorCode.LESSON_ID_CAN_NOT_NULL);
        }

        this.id = id;
        this.enrollmentId = enrollmentId;
        this.lessonId = lessonId;
        this.completionAt = completionAt;
    }

    public static LessonCompletion create(UUID enrollmentId, UUID lessonId) {
        return new LessonCompletion(UUID.randomUUID(), enrollmentId, lessonId, LocalDateTime.now());
    }

    public static LessonCompletion restore(UUID id, UUID enrollmentId, UUID lessonId, LocalDateTime completionAt) {
        return new LessonCompletion(id, enrollmentId, lessonId, completionAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public LocalDateTime getCompletionAt() {
        return completionAt;
    }
}
