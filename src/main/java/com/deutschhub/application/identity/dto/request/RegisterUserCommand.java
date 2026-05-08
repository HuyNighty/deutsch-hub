package com.deutschhub.application.identity.dto.request;

import com.deutschhub.domain.identity.model.valueObject.Email;
import com.deutschhub.domain.identity.model.valueObject.FullName;
import com.deutschhub.domain.identity.model.valueObject.Password;

public record RegisterUserCommand (
        String username,
        Email email,
        Password password,
        FullName fullName,
        String phoneNumber
) {}
