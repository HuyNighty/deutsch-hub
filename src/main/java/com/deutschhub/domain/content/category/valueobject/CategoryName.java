package com.deutschhub.domain.content.category.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Locale;

public record CategoryName(
        String value
) {
    private static final int MAX_LENGTH = 100;

    public CategoryName {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_NAME);
        }
    }

    public String normalizedValue() {
        return value.toLowerCase(Locale.ROOT);
    }
}
