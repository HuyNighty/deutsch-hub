package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.PublishArticleCommand;
import com.deutschhub.application.content.article.dto.response.PublishArticleResponse;
import com.deutschhub.application.content.article.port.in.PublishArticleUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.article.port.out.CurrentUserPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.shared.valueobject.UserId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class PublishArticleService implements PublishArticleUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentUserPort currentUserPort;

    @Override
    public PublishArticleResponse publish(PublishArticleCommand command) {

        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        UserId actorId = currentUserPort.getCurrentUserId();

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        Instant now = Instant.now();

        article.publish(actorId, now);

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private PublishArticleResponse toResponse(Article article) {
        return new PublishArticleResponse(article.getId(), article.getPublishedVersionId(), article.getEditorialStatus(),
                article.getPublicationStatus(), article.getPublishedAt());
    }
}
