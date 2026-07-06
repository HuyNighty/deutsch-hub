package com.deutschhub.application.learning.dto.response;

import java.util.List;
import java.util.UUID;

public record CompletedLessonsResponse(
        UUID courseId,
        List<UUID> completedLessonIds
) {
}
