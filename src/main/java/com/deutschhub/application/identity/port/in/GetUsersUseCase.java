package com.deutschhub.application.identity.port.in;

import com.deutschhub.application.identity.dto.request.GetUsersQuery;
import com.deutschhub.application.identity.dto.response.UserSummaryResponse;
import com.deutschhub.common.util.PageResponse;

public interface GetUsersUseCase {

    PageResponse<UserSummaryResponse> getUsers(GetUsersQuery query);

}
