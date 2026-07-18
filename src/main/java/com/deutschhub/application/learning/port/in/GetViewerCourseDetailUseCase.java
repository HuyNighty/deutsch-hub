package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.ViewerCourseDetailResponse;

import java.util.UUID;

public interface GetViewerCourseDetailUseCase {

    ViewerCourseDetailResponse getViewerCourseDetail(UUID courseId, UUID viewerId);
}
