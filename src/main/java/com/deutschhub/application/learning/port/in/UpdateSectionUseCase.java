package com.deutschhub.application.learning.port.in;

import com.deutschhub.application.learning.dto.request.UpdateSectionCommand;
import com.deutschhub.application.learning.dto.response.SectionResponse;

public interface UpdateSectionUseCase {

    SectionResponse updateSection(UpdateSectionCommand command);
}
