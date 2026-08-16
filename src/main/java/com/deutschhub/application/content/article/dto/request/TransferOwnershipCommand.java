package com.deutschhub.application.content.article.dto.request;

import java.util.UUID;

public record TransferOwnershipCommand(
        UUID articleId,
        UUID newOwnerId
) {
}