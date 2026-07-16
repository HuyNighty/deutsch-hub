package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.LessonDetailResponse;

import java.util.UUID;

public interface GetMyLessonDetailUseCase {

    LessonDetailResponse getMyLessonDetail(UUID userId, UUID courseId, UUID lessonId);
}
