package com.deutschhub.application.content.article.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TransferOwnershipResponse(
        UUID articleId,
        UUID ownerId,
        UUID transferredBy,
        Instant transferredAt
) {
}