package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public record ReviewFeedback(String value) {

    private static final int MAX_LENGTH = 2_000;

    public ReviewFeedback {

        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_FEEDBACK);
        }

        value = value.trim();

        if (value.isEmpty() || value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_FEEDBACK);
        }
    }

}