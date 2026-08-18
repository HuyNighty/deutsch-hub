package com.deutschhub.application.content.article.dto.query;

import com.deutschhub.application.content.article.dto.response.SourceResponse;
import com.deutschhub.domain.content.article.enums.PublicationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ArticleQueryModel(
        UUID articleId,
        UUID versionId,
        String slug,
        String title,
        String summary,
        String body,
        UUID primaryCategoryId,
        Set<UUID> topicIds,
        UUID coverMediaId,
        List<SourceResponse> sources,
        PublicationStatus publicationStatus,
        Instant publishedAt
) {
}
