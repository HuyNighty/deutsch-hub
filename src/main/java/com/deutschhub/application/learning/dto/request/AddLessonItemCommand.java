package com.deutschhub.application.learning.dto.request;

import java.util.UUID;

public record AddLessonItemCommand(
        UUID courseId,
        UUID sectionId,
        UUID lessonId,
        UUID actorId,
        String type,
        String title,
        String description,
        String content,
        String resourceUrl,
        UUID quizId,
        int estimatedMinutes,
        int orderIndex,
        boolean admin
) {
}
