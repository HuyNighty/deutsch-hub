package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.CompletedLessonsResponse;

import java.util.UUID;

public interface GetCompletedLessonsUseCase {

    CompletedLessonsResponse getCompletedLessons(UUID userId, UUID courseId);
}
