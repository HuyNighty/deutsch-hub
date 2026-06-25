package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.LogoutCommand;

public interface LogoutUseCase {

    void logout(LogoutCommand command);
}
