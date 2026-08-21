package com.deutschhub.infrastructure.content.article.persistence.projection;

import java.time.Instant;
import java.util.UUID;

public record PublishedArticleDetailProjection(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        String body,
        UUID primaryCategoryId,
        UUID coverMediaId,
        String publicationStatus,
        Instant publishedAt
) {
}
