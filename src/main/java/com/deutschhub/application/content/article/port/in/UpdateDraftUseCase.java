package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.UpdateDraftCommand;
import com.deutschhub.application.content.article.dto.response.UpdateDraftResponse;

public interface UpdateDraftUseCase {

    UpdateDraftResponse updateDraft(UpdateDraftCommand command);

}
