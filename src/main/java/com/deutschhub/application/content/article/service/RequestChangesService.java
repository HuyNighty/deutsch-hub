package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.RequestChangesCommand;
import com.deutschhub.application.content.article.dto.response.RequestChangesResponse;
import com.deutschhub.application.content.article.port.in.RequestChangesUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.valueobject.ReviewFeedback;
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
public class RequestChangesService implements RequestChangesUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public RequestChangesResponse requestChanges(RequestChangesCommand command) {

        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        UserId reviewer = actor.userId();

        authorizationPolicy.requireAdmin(actor);

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        ReviewFeedback feedback = new ReviewFeedback(command.feedback());

        article.requestChanges(reviewer, feedback, Instant.now());

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private RequestChangesResponse toResponse(Article article) {
        return new RequestChangesResponse(article.getId(), article.getDraftVersionId(), article.getEditorialStatus(), article.getPublicationStatus());
    }
}
