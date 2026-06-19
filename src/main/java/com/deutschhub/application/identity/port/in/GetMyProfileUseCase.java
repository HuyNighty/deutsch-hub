package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.response.UserResponse;

import java.util.UUID;

public interface GetMyProfileUseCase {

    UserResponse getMyProfile(UUID userId);
}
