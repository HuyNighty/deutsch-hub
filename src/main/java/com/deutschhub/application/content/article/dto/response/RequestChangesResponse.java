package com.deutschhub.application.content.article.dto.response;

import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;

import java.util.UUID;

public record RequestChangesResponse(
        UUID articleId,
        UUID draftVersionId,
        EditorialStatus editorialStatus,
        PublicationStatus publicationStatus
) {
}
