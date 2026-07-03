package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SectionDetailResponse(
        UUID id,
        String title,
        String description,
        int orderIndex,
        List<LessonResponse> lessons,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
