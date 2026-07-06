package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompletedLessonDetailResponse(
        UUID lessonId,
        LocalDateTime completedAt
) {
}
