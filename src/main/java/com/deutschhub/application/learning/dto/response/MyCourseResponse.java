package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MyCourseResponse(
        UUID courseId,
        String title,
        String description,
        String level,
        BigDecimal price,
        String currency,
        int estimatedHours,
        String enrollmentStatus,
        int completedLessons,
        int totalLessons,
        double completionPercentage,
        int totalStudyMinutes,
        LocalDateTime enrolledAt,
        LocalDateTime lastProgressUpdatedAt
) {
}
