package com.deutschhub.infrastructure.content.article.persistence.projection;

import java.time.Instant;
import java.util.UUID;

public record ArticleDetailProjection(
        UUID articleId,
        UUID ownerId,
        String slug,
        String editorialStatus,
        String publicationStatus,
        UUID draftVersionId,
        UUID publishedVersionId,
        Instant createdAt,
        Instant publishedAt,
        Instant archivedAt
) {
}
