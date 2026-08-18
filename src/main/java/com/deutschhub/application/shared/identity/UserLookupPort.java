package com.deutschhub.application.shared.identity;

import com.deutschhub.domain.shared.valueobject.UserId;

public interface UserLookupPort {

    boolean isActiveContentEditor(UserId userId);

}
