package com.deutschhub.infrastructure.content.article.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDraftRequest(

        @NotBlank(message = "Article title is required")
        @Size(max = 255, message = "Article title must not exceed 255 characters")
        String title
) {
}
