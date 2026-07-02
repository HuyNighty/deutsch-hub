package com.deutschhub.application.learning.port.in;

import java.util.UUID;

public interface DeleteCourseUseCase {

    void deleteCourse(UUID courseId, UUID actorId);
}
