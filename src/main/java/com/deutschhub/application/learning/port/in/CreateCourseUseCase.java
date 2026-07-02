package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.CreateCourseCommand;
import com.deutschhub.application.learning.dto.response.CourseResponse;

public interface CreateCourseUseCase {

    CourseResponse createCourse(CreateCourseCommand command);
}
