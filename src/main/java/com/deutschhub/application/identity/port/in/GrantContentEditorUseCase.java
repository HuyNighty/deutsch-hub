package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.GrantContentEditorCommand;
import com.deutschhub.application.identity.dto.response.GrantContentEditorResponse;

public interface GrantContentEditorUseCase {

    GrantContentEditorResponse grantContentEditor(GrantContentEditorCommand command);
}
