package com.deutschhub.application.content.category.dto.request;

import java.util.UUID;

public record DeactivateCategoryCommand(
        UUID categoryId
) {
}
