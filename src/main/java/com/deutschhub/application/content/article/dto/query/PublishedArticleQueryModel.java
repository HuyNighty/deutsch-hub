package com.deutschhub.application.content.article.dto.query;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record PublishedArticleQueryModel(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        CategorySummaryQuery primaryCategory,
        Set<TopicSummaryQuery> topics,
        UUID coverMediaId,
        Instant publishedAt
) {
}
