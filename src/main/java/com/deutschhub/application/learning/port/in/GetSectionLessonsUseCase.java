package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.LessonResponse;

import java.util.List;
import java.util.UUID;

public interface GetSectionLessonsUseCase {

    List<LessonResponse> getLessons(UUID courseId, UUID sectionId);
}
