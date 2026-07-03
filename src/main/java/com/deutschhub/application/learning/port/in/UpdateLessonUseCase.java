package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.UpdateLessonCommand;
import com.deutschhub.application.learning.dto.response.LessonResponse;

public interface UpdateLessonUseCase {

    LessonResponse updateLesson(UpdateLessonCommand command);
}
