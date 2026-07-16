package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PublishedCourseDetailResponse(
        UUID courseId,
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        UUID instructorId,
        int estimatedHours,
        List<PublishedSectionResponse> sections,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}