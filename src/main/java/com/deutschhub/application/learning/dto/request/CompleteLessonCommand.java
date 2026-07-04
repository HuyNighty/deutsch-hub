package com.deutschhub.application.learning.dto.request;

import java.util.UUID;

public record CompleteLessonCommand(
        UUID userId,
        UUID courseId,
        UUID lessonId,
        int studyMinutes
) {
}
