package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.response.UserSessionResponse;

import java.util.List;
import java.util.UUID;

public interface GetMySessionsUseCase {

    List<UserSessionResponse> getMySessions(UUID userId);
}
