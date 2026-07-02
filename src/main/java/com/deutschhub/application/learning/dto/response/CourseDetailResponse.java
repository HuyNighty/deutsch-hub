package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourseDetailResponse(
        UUID id,
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        boolean published,
        UUID instructorId,
        int estimatedHours,
        List<SectionResponse> sections,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
