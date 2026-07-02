package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.CourseResponse;

import java.util.UUID;

public interface UnpublishCourseUseCase {

    CourseResponse unpublishCourse(UUID courseId, UUID actorId, boolean admin);
}
