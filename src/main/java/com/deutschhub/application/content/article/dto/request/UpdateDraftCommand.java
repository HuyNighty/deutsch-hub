package com.deutschhub.application.content.article.dto.request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UpdateDraftCommand(
        UUID articleId,
        String title,
        String summary,
        String body,
        UUID primaryCategoryId,
        Set<UUID> topicIds,
        UUID coverMediaId,
        List<SourceCommand> sources
) {
}
