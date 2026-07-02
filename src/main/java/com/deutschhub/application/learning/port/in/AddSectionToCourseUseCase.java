package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.AddSectionCommand;
import com.deutschhub.application.learning.dto.response.SectionResponse;

public interface AddSectionToCourseUseCase {

    SectionResponse addSection(AddSectionCommand command);
}
