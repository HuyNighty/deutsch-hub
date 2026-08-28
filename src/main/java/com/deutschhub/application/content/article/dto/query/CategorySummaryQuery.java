package com.deutschhub.application.content.article.dto.query;

import java.util.UUID;

public record CategorySummaryQuery (
    UUID id,
    String name
) {
}
