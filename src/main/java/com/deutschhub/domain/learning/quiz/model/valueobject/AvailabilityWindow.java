package com.deutschhub.domain.learning.quiz.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.time.Instant;

public record AvailabilityWindow(Instant startsAt, Instant endsAt) {

    public AvailabilityWindow {
        if (startsAt != null && endsAt != null && !startsAt.isBefore(endsAt)) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_AVAILABILITY_WINDOW);
        }
    }

    public boolean isOpenAt(Instant now) {
        if (now == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_AVAILABILITY_TIME);
        }

        boolean started = startsAt == null || !now.isBefore(startsAt);
        boolean notEnded = endsAt == null || now.isBefore(endsAt);

        return started && notEnded;
    }
}
