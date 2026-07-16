package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.AddLessonItemCommand;
import com.deutschhub.application.learning.dto.response.LessonDetailResponse;

public interface AddLessonItemUseCase {
    LessonDetailResponse addLessonItem(AddLessonItemCommand command);
}
