package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.EnrollmentProgressResponse;

import java.util.UUID;

public interface GetMyCourseProgressUseCase {

    EnrollmentProgressResponse getProgress(UUID userId, UUID courseId);
}
