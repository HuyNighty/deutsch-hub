package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.response.SectionResponse;

import java.util.List;
import java.util.UUID;

public interface GetCourseSectionUseCase {

    List<SectionResponse> getSections(UUID courseId);
}
