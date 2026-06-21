package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.RefreshTokenCommand;
import com.deutschhub.application.identity.dto.response.RefreshTokenResponse;

public interface RefreshTokenUseCase {

    RefreshTokenResponse refresh(RefreshTokenCommand command);

}
