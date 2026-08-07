package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public record Summary (
        String value
) {
    private static final int MAX_LENGTH = 500;

    public Summary {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_SUMMARY);
        }

        value = value.trim();

        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_SUMMARY);
        }
    }
}
