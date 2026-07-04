package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.MyCourseDetailResponse;

import java.util.UUID;

public interface GetMyCourseDetailUseCase {

    MyCourseDetailResponse getMyCourseDetail(UUID userId, UUID courseId);
}
