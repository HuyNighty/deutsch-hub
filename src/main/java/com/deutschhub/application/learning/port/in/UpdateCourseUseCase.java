package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.UpdateCourseCommand;
import com.deutschhub.application.learning.dto.response.CourseResponse;

public interface UpdateCourseUseCase {

    CourseResponse updateCourse(UpdateCourseCommand command);
}
