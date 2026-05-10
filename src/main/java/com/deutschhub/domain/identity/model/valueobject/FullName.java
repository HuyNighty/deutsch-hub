package com.deutschhub.domain.identity.model.valueobject;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

public class FullName {

    private final String firstName;
    private final String lastName;

    public FullName(String firstName, String lastName) {

        if (firstName == null || firstName.trim().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FULL_NAME);
        }

        if (lastName == null || lastName.trim().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_FULL_NAME);
        }

        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}