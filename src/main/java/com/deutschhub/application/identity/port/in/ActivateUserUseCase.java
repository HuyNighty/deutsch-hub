package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.response.UserDetailResponse;

import java.util.UUID;

public interface ActivateUserUseCase {

    UserDetailResponse activate(UUID userId);
}
