package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.CompleteLessonCommand;
import com.deutschhub.application.learning.dto.response.EnrollmentProgressResponse;

public interface CompleteLessonUseCase {

    EnrollmentProgressResponse completeLesson(CompleteLessonCommand command);
}
