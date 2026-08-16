package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.ArchiveArticleCommand;
import com.deutschhub.application.content.article.dto.response.ArchiveArticleResponse;
import com.deutschhub.application.content.article.port.in.ArchiveArticleUseCase;
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
public class ArchiveArticleService implements ArchiveArticleUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentUserPort currentUserPort;

    @Override
    public ArchiveArticleResponse archive(ArchiveArticleCommand command) {

        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        UserId actorId = currentUserPort.getCurrentUserId();

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        Instant now = Instant.now();

        article.archive(actorId, now);

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private ArchiveArticleResponse toResponse(Article article) {
        return new ArchiveArticleResponse(article.getId(), article.getEditorialStatus(), article.getPublicationStatus(), article.getArchivedAt());
    }
}