package com.deutschhub.domain.identity.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public class Password {

    private final String hashedValue;

    public Password(String hashedValue) {

        if (hashedValue == null || hashedValue.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        this.hashedValue = hashedValue;
    }

    public static Password fromHashed(String hashedPassword) {

        if (hashedPassword == null || hashedPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return new Password(hashedPassword);
    }

    public static void validateStrength(String rawPassword) {

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        String p = rawPassword.trim();

        if (p.length() < 8) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!p.matches(".*[A-Z].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!p.matches(".*[a-z].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        if (!p.matches(".*[0-9].*")) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public String getHashedValue() {
        return hashedValue;
    }
}