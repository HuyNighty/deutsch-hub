package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record LessonResponse(
        UUID id,
        String title,
        String description,
        int estimatedMinutes,
        String level,
        int orderIndex,
        boolean freePreview,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
