package com.deutschhub.application.learning.port.in;

import java.util.UUID;

public interface DeleteLessonUseCase {

    void deleteLesson(UUID courseId, UUID sectionId, UUID lessonId, UUID actorId, boolean admin);
}
