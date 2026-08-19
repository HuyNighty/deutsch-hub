package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.query.ArticleDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.ArticleVersionDetailQueryModel;
import com.deutschhub.application.content.article.dto.query.SourceQueryModel;
import com.deutschhub.application.content.article.dto.response.ArticleDetailResponse;
import com.deutschhub.application.content.article.dto.response.ArticleVersionDetailResponse;
import com.deutschhub.application.content.article.dto.response.SourceResponse;
import com.deutschhub.application.content.article.port.in.GetArticleDetailUseCase;
import com.deutschhub.application.content.article.port.out.ArticleQueryPort;
import com.deutschhub.application.content.shared.authorization.ContentAuthorizationPolicy;
import com.deutschhub.application.shared.authorization.CurrentActor;
import com.deutschhub.application.shared.authorization.CurrentActorPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.shared.valueobject.UserId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class GetArticleDetailService implements GetArticleDetailUseCase {

    ArticleQueryPort articleQueryPort;
    CurrentActorPort currentActorPort;
    ContentAuthorizationPolicy authorizationPolicy;

    @Override
    public ArticleDetailResponse getById(UUID articleId) {
        if (articleId == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        CurrentActor actor = currentActorPort.getCurrentActor();

        ArticleDetailQueryModel article = articleQueryPort.findDetailById(articleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        authorizationPolicy.requireArticleOwnerOrAdmin(article.ownerId(), actor);

        return toResponse(article);
    }

    private ArticleDetailResponse toResponse(ArticleDetailQueryModel article) {
        return new ArticleDetailResponse(article.articleId(), article.ownerId(), article.slug(), article.editorialStatus(),
                article.publicationStatus(), article.draftVersionId(), article.publishedVersionId(), article.createdAt(),
                article.publishedAt(), article.archivedAt(), toVersionResponse(article.draft()), toVersionResponse(article.published()));
    }

    private ArticleVersionDetailResponse toVersionResponse(ArticleVersionDetailQueryModel version) {
        if (version == null) {
            return null;
        }

        return new ArticleVersionDetailResponse(version.versionId(), version.versionNumber(), version.title(), version.summary(),
                version.body(), version.primaryCategoryId(), version.topicIds(), version.coverMediaId(),
                version.sources().stream().map(this::toSourceResponse).toList(),
                version.createdBy(), version.createdAt(), version.lastModifiedBy(), version.lastModifiedAt());
    }

    private SourceResponse toSourceResponse(SourceQueryModel source) {
        return new SourceResponse(source.url(), source.title());
    }
}
