package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.CourseResponse;

import java.util.UUID;

public interface PublishCourseUseCase {

    CourseResponse publishCourse(UUID courseId, UUID actorId, boolean admin);
}
