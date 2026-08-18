package com.deutschhub.application.content.category.dto.response;

import java.util.UUID;

public record CategorySummaryResponse(
        UUID id,
        String name
) {
}
