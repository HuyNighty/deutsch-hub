package com.deutschhub.application.identity.dto.request;

import com.deutschhub.domain.identity.model.valueobject.Email;
import com.deutschhub.domain.identity.model.valueobject.FullName;
import com.deutschhub.domain.identity.model.valueobject.Password;

public record RegisterUserCommand (
        String username,
        Email email,
        Password password,
        FullName fullName,
        String phoneNumber
) {
}
