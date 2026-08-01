package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.LessonItemMediaContentResponse;

import java.util.UUID;

public interface GetMyLessonItemMediaUseCase {

    LessonItemMediaContentResponse getMedia(UUID userId, UUID courseId, UUID lessonId, UUID itemId);
}
