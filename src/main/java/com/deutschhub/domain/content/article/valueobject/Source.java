package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.net.URI;

public record Source(
        String title,
        String url
) {
    public Source {
        if (title() == null || title().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_TITLE);
        }

        if (url() == null || url().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_URL);
        }

        try {
            URI.create(url());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_URL);
        }
    }
}
