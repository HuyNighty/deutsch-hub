package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.UpdateUserRolesCommand;
import com.deutschhub.application.identity.dto.response.UserDetailResponse;

public interface UpdateUserRolesUseCase {

    UserDetailResponse updateRoles(UpdateUserRolesCommand command);
}
