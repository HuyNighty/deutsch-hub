package com.deutschhub.application.learning.dto.request;

import com.deutschhub.domain.learning.model.valueobject.LessonItemType;

import java.util.UUID;

public record AddLessonItemCommand(
        UUID courseId,
        UUID sectionId,
        UUID lessonId,
        UUID actorId,
        LessonItemType type,
        String title,
        String description,
        String content,
        UUID mediaId,
        UUID quizId,
        int estimatedMinutes,
        int orderIndex,
        boolean admin
) {
}
