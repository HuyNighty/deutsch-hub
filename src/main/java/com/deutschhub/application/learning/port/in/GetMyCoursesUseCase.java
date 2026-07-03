package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.MyCourseResponse;

import java.util.List;
import java.util.UUID;

public interface GetMyCoursesUseCase {

    List<MyCourseResponse> getMyCourses(UUID userId);
}
