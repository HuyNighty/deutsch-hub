package com.deutschhub.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("error.uncategorized", 9999),

    INVALID_EMAIL("error.identity.invalid.email", 3001),
    INVALID_ROLE_NAME("error.identity.invalid.role.name", 3002),
    INVALID_USERNAME("error.identity.invalid.username", 3003),
    INVALID_PASSWORD("error.identity.invalid.password", 3004),

    USER_ALREADY_EXISTS("error.user.already.exists", 4001),

    INVALID_COURSE_TITLE("error.course.invalid.title", 5001),
    INVALID_CEFR_LEVEL("error.course.invalid.level", 5002),
    INVALID_COURSE_PRICE("error.course.invalid.price", 5003),
    COURSE_HAS_NO_LESSONS("error.course.has.no.lessons", 5004),
    COURSE_ALREADY_PUBLISHED("error.course.already.published", 5005),
    CANNOT_MODIFY_PUBLISHED_COURSE("error.course.cannot.modify.published", 5006),
    INVALID_LESSON("error.course.invalid.lesson", 5007),

    INVALID_LESSON_ORDER("error.lesson.invalid.order", 6001),
    LESSON_INVALID_TITLE("error.lesson.invalid.title", 6002),
    LESSON_INVALID_DURATION("error.lesson.invalid.duration", 6003),
    ;

    private final String messageKey;
    private final int errorCode;

    ErrorCode(String messageKey, int errorCode) {
        this.messageKey = messageKey;
        this.errorCode = errorCode;
    }

}
