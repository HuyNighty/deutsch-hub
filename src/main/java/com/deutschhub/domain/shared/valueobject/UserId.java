package com.deutschhub.domain.shared.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.UUID;

public record UserId(
        UUID value
) {
    public UserId {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }
}
