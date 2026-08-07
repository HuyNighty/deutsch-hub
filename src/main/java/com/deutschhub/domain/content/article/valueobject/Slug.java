package com.deutschhub.domain.content.article.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.regex.Pattern;

public record Slug(String value) {

    private static final int MAX_LENGTH = 200;

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

    public Slug {

        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_SLUG);
        }

        value = value.trim();

        if (value.isEmpty() || value.length() > MAX_LENGTH || !SLUG_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_SLUG);
        }
    }
}