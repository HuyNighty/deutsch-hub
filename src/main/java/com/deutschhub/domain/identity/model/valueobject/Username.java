package com.deutschhub.domain.identity.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public class Username {

    private final String value;

    public Username(String value) {

        if (value == null || value.trim().length() < 3) {
            throw new BusinessException(ErrorCode.INVALID_USERNAME);
        }

        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }
}

