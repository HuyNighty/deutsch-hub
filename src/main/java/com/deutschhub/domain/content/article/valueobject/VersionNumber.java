package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public record VersionNumber(
        int value
) {
    public VersionNumber {
        if (value < 1) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_VERSION_NUMBER);
        }
    }

    public VersionNumber next() {
        return new VersionNumber(this.value + 1);
    }

    public static VersionNumber first() {
        return new VersionNumber(1);
    }
}
