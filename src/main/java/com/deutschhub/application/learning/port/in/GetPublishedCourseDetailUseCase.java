package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.CourseDetailResponse;

import java.util.UUID;

public interface GetPublishedCourseDetailUseCase {

    CourseDetailResponse getPublishedCourseDetail(UUID courseId);
}
