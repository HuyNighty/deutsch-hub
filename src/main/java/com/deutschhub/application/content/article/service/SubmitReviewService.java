package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.SubmitReviewCommand;
import com.deutschhub.application.content.article.dto.response.SubmitReviewResponse;
import com.deutschhub.application.content.article.port.in.SubmitReviewUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.article.validator.ArticleDraftReferenceValidator;
import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.entity.ArticleVersion;
import com.deutschhub.domain.shared.valueobject.UserId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmitReviewService implements SubmitReviewUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;
    ArticleDraftReferenceValidator articleDraftReferenceValidator;

    @Override
    public SubmitReviewResponse submitReview(SubmitReviewCommand command) {

        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        UserId actorId = actor.userId();

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        authorizationPolicy.requireArticleOwnerOrAdmin(article, actor);

        ArticleVersion draft = article.getCurrentDraft();

        articleDraftReferenceValidator.validate(draft.getPrimaryCategoryId(), draft.getTopicIds(), draft.getCoverMediaId());

        article.submitReview(actorId, Instant.now());

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private SubmitReviewResponse toResponse(Article article) {
        return new SubmitReviewResponse(article.getId(), article.getDraftVersionId(), article.getEditorialStatus(), article.getPublicationStatus());
    }
}
