package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.DeactivateMyAccountCommand;

public interface DeactivateMyAccountUseCase {

    void deactivateMyAccount(DeactivateMyAccountCommand command);
}
