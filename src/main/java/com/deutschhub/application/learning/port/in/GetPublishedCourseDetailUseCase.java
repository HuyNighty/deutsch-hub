package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.PublishedCourseDetailResponse;

import java.util.UUID;

public interface GetPublishedCourseDetailUseCase {

    PublishedCourseDetailResponse getPublishedCourseDetail(UUID courseId);
}
