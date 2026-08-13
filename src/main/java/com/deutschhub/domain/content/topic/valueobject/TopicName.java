package com.deutschhub.domain.content.topic.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Locale;

public record TopicName(
        String value
) {
    private static final int MAX_LENGTH = 100;

    public TopicName {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_TOPIC_NAME);
        }
    }

    public String normalizedValue() {
        return value.toLowerCase(Locale.ROOT);
    }
}