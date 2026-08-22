package com.deutschhub.infrastructure.content.article.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestChangesRequest(

        @NotBlank(message = "Feedback must not be blank")
        @Size(min = 10, max = 2000, message = "Feedback must be between 10 and 2000 characters")
        String feedback
) {
}
