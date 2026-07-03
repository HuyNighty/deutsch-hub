package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID userId,
        UUID courseId,
        String status,
        int completedLessons,
        int totalLessons,
        double completionPercentage,
        int totalStudyMinutes,
        LocalDateTime enrolledAt
) {
}
