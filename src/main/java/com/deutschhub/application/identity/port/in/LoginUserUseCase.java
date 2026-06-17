package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.LoginUserCommand;
import com.deutschhub.application.identity.dto.response.LoginResponse;

public interface LoginUserUseCase {

    LoginResponse login(LoginUserCommand command);
}
