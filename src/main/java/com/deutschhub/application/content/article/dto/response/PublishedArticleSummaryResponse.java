package com.deutschhub.application.content.article.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PublishedArticleSummaryResponse(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        UUID primaryCategoryId,
        UUID coverMediaId,
        Instant publishedAt
) {
}
