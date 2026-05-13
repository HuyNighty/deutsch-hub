package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;

public interface LoginUserUseCase {

    UserResponse login(LoginUserCommand command);
}
