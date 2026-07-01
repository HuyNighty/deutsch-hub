package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.ChangeMyPasswordCommand;

public interface ChangeMyPasswordUseCase {

    void changeMyPassword(ChangeMyPasswordCommand command);
}
