package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.GetCoursesQuery;
import com.deutschhub.application.learning.dto.response.CourseResponse;
import com.deutschhub.common.util.PageResponse;

public interface GetPublishedCoursesUseCase {

    PageResponse<CourseResponse> getPublishedCourses(GetCoursesQuery query);
}
