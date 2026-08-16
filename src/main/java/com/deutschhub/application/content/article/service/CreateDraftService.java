package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.CreateDraftCommand;
import com.deutschhub.application.content.article.dto.response.CreateDraftResponse;
import com.deutschhub.application.content.article.port.in.CreateDraftUseCase;
import com.deutschhub.application.content.article.port.out.CurrentUserPort;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.service.SlugGenerator;
import com.deutschhub.domain.content.article.valueobject.ArticleTitle;
import com.deutschhub.domain.content.article.valueobject.Slug;
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
public class CreateDraftService implements CreateDraftUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentUserPort currentUserPort;
    SlugGenerator slugGenerator;

    @Override
    public CreateDraftResponse createDraft(CreateDraftCommand command) {

        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        UserId ownerId = currentUserPort.getCurrentUserId();

        ArticleTitle title = new ArticleTitle(command.title());

        Slug slug =  slugGenerator.generateFromTitle(title);

        if (articleRepositoryPort.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.ARTICLE_SLUG_ALREADY_EXISTS);
        }

        Instant now = Instant.now();

        Article article = Article.createDraft(ownerId, title, slug, now);

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private CreateDraftResponse toResponse(Article article) {
        return new CreateDraftResponse(article.getId(), article.getDraftVersionId(), article.getSlug().value(),
                article.getEditorialStatus(), article.getPublicationStatus(), article.getCreatedAt());
    }
}
