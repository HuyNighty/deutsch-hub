package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.RegisterUserCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;

public interface RegisterUserUseCase {

    UserResponse register(RegisterUserCommand command);
}
