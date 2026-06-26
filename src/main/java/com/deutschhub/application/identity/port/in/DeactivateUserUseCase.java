package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.response.UserDetailResponse;

import java.util.UUID;

public interface DeactivateUserUseCase {

    UserDetailResponse deactivate(UUID userId, UUID currentAdminId);

}
