package com.deutschhub.application.content.article.dto.request;

import java.util.UUID;

public record RequestChangesCommand(
        UUID articleId,
        String feedback
) {
}
