package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.CreateDraftCommand;
import com.deutschhub.application.content.article.dto.response.CreateDraftResponse;

public interface CreateDraftUseCase {

    CreateDraftResponse createDraft(CreateDraftCommand command);
}
