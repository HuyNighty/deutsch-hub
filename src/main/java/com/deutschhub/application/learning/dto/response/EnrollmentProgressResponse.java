package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrollmentProgressResponse(
        UUID enrollmentId,
        UUID courseId,
        String status,
        int completedLessons,
        int totalLessons,
        double completionPercentage,
        int totalStudyMinutes,
        LocalDateTime lastProgressUpdatedAt
) {
}
