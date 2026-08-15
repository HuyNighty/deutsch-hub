package com.deutschhub.application.content.category.dto.request;

import java.util.UUID;

public record RenameCategoryCommand(
        UUID categoryId,
        String name
) {
}
