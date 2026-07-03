package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.EnrollmentResponse;

import java.util.UUID;

public interface EnrollCourseUseCase {

    EnrollmentResponse enroll(UUID userId, UUID courseId);
}
