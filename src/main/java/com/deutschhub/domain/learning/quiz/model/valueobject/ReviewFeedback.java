package com.deutschhub.domain.learning.quiz.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public record ReviewFeedback(String content) {

    private static final int MAX_LENGTH = 2000;

    public ReviewFeedback {
        if (content == null || content.isBlank() || content.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.QUIZ_REVIEW_INVALID_FEEDBACK);
        }
    }
}