package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.UpdateMyProfileCommand;
import com.deutschhub.application.identity.dto.response.UserResponse;

public interface UpdateMyProfileUseCase {

    UserResponse updateMyProfile(UpdateMyProfileCommand command);
}
