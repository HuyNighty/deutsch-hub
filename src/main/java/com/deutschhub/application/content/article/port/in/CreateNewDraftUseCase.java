package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.CreateNewDraftCommand;
import com.deutschhub.application.content.article.dto.response.CreateNewDraftResponse;

public interface CreateNewDraftUseCase {

    CreateNewDraftResponse createNewDraft(CreateNewDraftCommand command);
}
