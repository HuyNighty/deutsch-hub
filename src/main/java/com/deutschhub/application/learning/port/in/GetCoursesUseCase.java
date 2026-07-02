package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.common.util.PageResponse;

public interface GetCoursesUseCase {

    PageResponse<CourseResponse> getCourses(GetCoursesQuery query);
}
