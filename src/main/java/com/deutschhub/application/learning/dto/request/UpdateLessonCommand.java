package com.deutschhub.application.learning.dto.request;

import java.util.UUID;

public record UpdateLessonCommand (
        UUID courseId,
        UUID sectionId,
        UUID lessonId,
        UUID actorId,
        String title,
        String description,
        String content,
        Integer estimatedMinutes,
        String level,
        Integer orderIndex,
        boolean freePreview,
        boolean admin
) {
}
