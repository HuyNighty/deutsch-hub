package com.deutschhub.application.content.article.dto.response;

import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

public record ArticleDetailResponse(
        UUID articleId,
        UserId ownerId,
        String slug,
        EditorialStatus editorialStatus,
        PublicationStatus publicationStatus,
        UUID draftVersionId,
        UUID publishedVersionId,
        Instant createdAt,
        Instant publishedAt,
        Instant archivedAt,
        ArticleVersionDetailResponse draft,
        ArticleVersionDetailResponse published
) {
}
