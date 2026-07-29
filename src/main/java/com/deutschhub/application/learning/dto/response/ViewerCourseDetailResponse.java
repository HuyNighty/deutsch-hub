package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ViewerCourseDetailResponse(
        UUID id,
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        UUID instructorId,
        int estimatedHours,
        List<PublishedSectionResponse> sections,
        boolean enrolled,
        String enrollmentStatus,
        Integer completedLessons,
        Integer totalLessons,
        BigDecimal completionPercentage,
        Integer totalStudyMinutes,
        LocalDateTime enrolledAt,
        LocalDateTime lastProgressUpdatedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
