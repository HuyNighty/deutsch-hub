package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.WithdrawReviewCommand;
import com.deutschhub.application.content.article.dto.response.WithdrawReviewResponse;
import com.deutschhub.application.content.article.port.in.WithdrawReviewUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
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
public class WithdrawReviewService implements WithdrawReviewUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public WithdrawReviewResponse withdrawReview(WithdrawReviewCommand command) {

        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        UserId actorId = actor.userId();

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        authorizationPolicy.requireArticleOwnerOrAdmin(article, actor);

        article.withdrawReview(actorId, Instant.now());

        articleRepositoryPort.save(article);

        return toRepsonse(article);
    }

    private WithdrawReviewResponse toRepsonse(Article article) {
        return new WithdrawReviewResponse(article.getId(), article.getDraftVersionId(), article.getEditorialStatus(), article.getPublicationStatus());
    }
}
