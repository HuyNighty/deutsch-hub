package com.deutschhub.application.learning.port.in;

import java.util.UUID;

public interface DropCourseUseCase {

    void dropCourse(UUID userId, UUID courseId);
}
