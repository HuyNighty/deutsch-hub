package com.deutschhub.application.content.article.dto.query;

import java.util.UUID;

public record TopicSummaryQuery (
        UUID id,
        UUID categoryId,
        String name
) {
}
