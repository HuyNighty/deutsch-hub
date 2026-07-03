package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.AddLessonCommand;
import com.deutschhub.application.learning.dto.response.LessonResponse;

public interface AddLessonToSectionUseCase {

    LessonResponse addLessonToSection(AddLessonCommand command);
}
