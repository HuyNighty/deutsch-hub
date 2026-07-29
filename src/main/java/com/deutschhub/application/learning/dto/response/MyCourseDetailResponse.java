package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MyCourseDetailResponse(
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
        BigDecimal completionPercentage,
        int totalStudyMinutes,
        List<SectionDetailResponse> sections,
        LocalDateTime enrolledAt,
        LocalDateTime lastProgressUpdatedAt
) {
}
