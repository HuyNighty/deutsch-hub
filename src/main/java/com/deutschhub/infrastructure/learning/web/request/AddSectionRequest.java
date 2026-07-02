package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSectionRequest(

        @NotBlank(message = "Section title is required")
        @Size(max = 150, message = "Section title must not exceed 150 characters")
        String title,

        @Size(max = 1000, message = "Section description must not exceed 1000 characters")
        String description,

        @Min(value = 0, message = "Section order index must not be negative")
        int orderIndex
) {
}
