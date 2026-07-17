package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record LessonDetailResponse(
        UUID id,
        String title,
        String description,
        String content,
        int estimatedMinutes,
        String level,
        int orderIndex,
        boolean freePreview,
        boolean completed,
        UUID previousLessonId,
        UUID nextLessonId,
        List<LessonItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
