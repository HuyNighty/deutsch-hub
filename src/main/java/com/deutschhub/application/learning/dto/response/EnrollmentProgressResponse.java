package com.deutschhub.application.learning.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentProgressResponse(
        UUID enrollmentId,
        UUID courseId,
        String status,
        int completedLessons,
        int totalLessons,
        BigDecimal completionPercentage,
        int totalStudyMinutes,
        LocalDateTime lastProgressUpdatedAt
) {
}
