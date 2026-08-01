package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonItemResponse(
        UUID id,
        String type,
        String title,
        String description,
        String content,
        UUID mediaId,
        UUID quizId,
        int estimatedMinutes,
        int orderIndex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
