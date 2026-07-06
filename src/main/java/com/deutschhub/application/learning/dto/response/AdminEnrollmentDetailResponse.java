package com.deutschhub.application.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AdminEnrollmentDetailResponse(
        UUID enrollmentId,
        UUID userId,
        UUID courseId,
        String status,
        int completedLessons,
        int totalLessons,
        double completionPercentage,
        int totalStudyMinutes,
        List<CompletedLessonDetailResponse> completedLessonDetails,
        LocalDateTime enrolledAt,
        LocalDateTime completedAt,
        LocalDateTime droppedAt,
        LocalDateTime expiredAt,
        LocalDateTime lastProgressUpdatedAt
) {
}
