package com.deutschhub.application.content.article.dto.query;

import com.deutschhub.domain.content.article.enums.PublicationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PublishedArticleDetailQueryModel(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        String body,
        CategorySummaryQuery primaryCategory,
        Set<TopicSummaryQuery> topics,
        UUID coverMediaId,
        List<SourceQueryModel> sources,
        PublicationStatus publicationStatus,
        Instant publishedAt
) {
}
