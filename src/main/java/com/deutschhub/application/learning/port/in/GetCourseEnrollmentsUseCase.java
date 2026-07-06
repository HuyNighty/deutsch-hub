package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.AdminCourseEnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface GetCourseEnrollmentsUseCase {

    List<AdminCourseEnrollmentResponse> getCourseEnrollments(UUID courseId);
}
