package com.deutschhub.infrastructure.identity.web.request;

import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(

        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber
) {
}
