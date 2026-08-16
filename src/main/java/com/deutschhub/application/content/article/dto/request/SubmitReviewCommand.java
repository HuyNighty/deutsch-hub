package com.deutschhub.application.content.article.dto.request;

import java.util.UUID;

public record SubmitReviewCommand(
        UUID articleId
) {
}
