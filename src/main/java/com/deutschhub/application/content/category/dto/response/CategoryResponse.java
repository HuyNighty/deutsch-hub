package com.deutschhub.application.content.category.dto.response;

import com.deutschhub.domain.content.category.enums.CategoryStatus;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryStatus status
) {
}
