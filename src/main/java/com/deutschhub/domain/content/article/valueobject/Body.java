package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public record Body (
    String value
) {

    private static final int MAX_LENGTH = 50_000;

    public Body {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_BODY);
        }

        value = value.trim();

        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_BODY);
        }
    }
}
