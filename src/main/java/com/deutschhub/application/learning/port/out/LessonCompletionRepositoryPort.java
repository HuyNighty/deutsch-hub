package com.deutschhub.application.learning.port.out;

import com.deutschhub.domain.learning.model.entity.LessonCompletion;

import java.util.UUID;

public interface LessonCompletionRepositoryPort {

    LessonCompletion save(LessonCompletion lessonCompletion);

    boolean existsByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    long countByEnrollmentId(UUID enrollmentId);
}
