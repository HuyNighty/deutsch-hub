package com.deutschhub.infrastructure.learning.web.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCourseRequest (

        @NotBlank(message = "Course title is required")
        @Size(max = 150, message = "Course title must not exceed 150 characters")
        String title,

        @Size(max = 2000, message = "Course description must not exceed 2000 characters")
        String description,

        @NotBlank(message = "CERF level is required")
        String level,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", message = "Price must not be negative")
        BigDecimal price,

        String currency
) {
}
