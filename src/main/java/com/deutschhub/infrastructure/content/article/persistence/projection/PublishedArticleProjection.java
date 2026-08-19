package com.deutschhub.infrastructure.content.article.persistence.projection;

import java.time.Instant;
import java.util.UUID;

public record PublishedArticleProjection(
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
