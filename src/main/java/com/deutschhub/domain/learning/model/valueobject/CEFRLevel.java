package com.deutschhub.domain.learning.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Arrays;
import java.util.List;

public final class CEFRLevel {

    private static final List<String> VALID_LEVELS = Arrays.asList(
            "A1", "A2", "B1", "B2", "C1", "C2"
    );

    private final String value;

    public CEFRLevel(String value) {
        this.value = validate(value);
    }

    private String validate(String level) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_CEFR_LEVEL, "Level cannot be empty");
        }

        String upperLevel = level.trim().toUpperCase();

        if (!VALID_LEVELS.contains(upperLevel)) {
            throw new BusinessException(ErrorCode.INVALID_CEFR_LEVEL,
                    "Invalid CEFR level: " + level + ". Allowed levels: A1, A2, B1, B2, C1, C2");
        }

        return upperLevel;
    }

    public boolean isBeginner() {
        return "A1".equals(value) || "A2".equals(value);
    }

    public boolean isIntermediate() {
        return "B1".equals(value) || "B2".equals(value);
    }

    public boolean isAdvanced() {
        return "C1".equals(value) || "C2".equals(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CEFRLevel other = (CEFRLevel) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
