package com.deutschhub.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("error.uncategorized", 9999),

    INVALID_EMAIL("error.identity.invalid.email", 3001),
    INVALID_ROLE_NAME("error.identity.invalid.role.name", 3002),
    INVALID_USERNAME("error.identity.invalid.username", 3003),
    ;

    private final String messageKey;
    private final int errorCode;

    ErrorCode(String messageKey, int errorCode) {
        this.messageKey = messageKey;
        this.errorCode = errorCode;
    }

}
