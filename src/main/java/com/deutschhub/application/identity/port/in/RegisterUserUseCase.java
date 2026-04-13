package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.domain.identity.model.aggregate.User;

public interface RegisterUserUseCase {

    User register(RegisterUserCommand command);
}
