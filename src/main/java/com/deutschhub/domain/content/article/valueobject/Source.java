package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.net.URI;

public record Source(
        String title,
        String url
) {
    public Source {
        title = title == null ? null : title.trim();
        url = url == null ? null : url.trim();

        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_TITLE);
        }

        if (url == null || url.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_URL);
        }

        URI uri;

        try {
            uri =  URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_URL);
        }

        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null ||  uri.getHost().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_SOURCE_URL);
        }
    }
}
