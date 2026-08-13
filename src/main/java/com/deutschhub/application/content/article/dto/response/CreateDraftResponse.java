package com.deutschhub.application.content.article.dto.response;

import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateDraftResponse(
        UUID articleId,
        UUID draftVersionId,
        String slug,
        EditorialStatus editorialStatus,
        PublicationStatus publicationStatus,
        Instant createdAt
) {
}
