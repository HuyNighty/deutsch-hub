package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.RequestChangesCommand;
import com.deutschhub.application.content.article.dto.response.RequestChangesResponse;

public interface RequestChangesUseCase {

    RequestChangesResponse requestChanges(RequestChangesCommand command);
}
