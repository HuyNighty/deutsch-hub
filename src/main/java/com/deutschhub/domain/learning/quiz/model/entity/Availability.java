package com.deutschhub.domain.learning.quiz.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.enums.AvailabilityStatus;
import com.deutschhub.domain.learning.quiz.model.valueobject.AvailabilityWindow;

import java.time.Instant;

public class Availability {

    private AvailabilityStatus status;
    private AvailabilityWindow window;

    protected Availability() {
    }

    private Availability(AvailabilityStatus status, AvailabilityWindow window) {
        this.status = status;
        this.window = window;
    }

    public static Availability create() {
        return new Availability(AvailabilityStatus.INACTIVE, null);
    }

    public static Availability restore(AvailabilityStatus status, AvailabilityWindow window) {
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_AVAILABILITY_DATA);
        }

        return new Availability(status, window);
    }

    public void activate() {
        this.status = AvailabilityStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AvailabilityStatus.INACTIVE;
    }

    public void setWindow(AvailabilityWindow window) {
        this.window = window;
    }

    public void clearWindow() {
        this.window = null;
    }

    public boolean isActive() {
        return status == AvailabilityStatus.ACTIVE;
    }

    public boolean isAvailableAt(Instant now) {
        if (!isActive()) {
            return false;
        }

        return window == null || window.isOpenAt(now);
    }

    public AvailabilityStatus getStatus() {
        return status;
    }

    public AvailabilityWindow getWindow() {
        return window;
    }
}
