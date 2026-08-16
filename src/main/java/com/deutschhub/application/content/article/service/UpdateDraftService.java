package com.deutschhub.application.content.article.service;

import com.deutschhub.application.content.article.dto.request.SourceCommand;
import com.deutschhub.application.content.article.dto.request.UpdateDraftCommand;
import com.deutschhub.application.content.article.dto.response.UpdateDraftResponse;
import com.deutschhub.application.content.article.port.in.UpdateDraftUseCase;
import com.deutschhub.application.content.article.port.out.ArticleRepositoryPort;
import com.deutschhub.application.content.article.port.out.CurrentUserPort;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.aggregate.Article;
import com.deutschhub.domain.content.article.valueobject.ArticleTitle;
import com.deutschhub.domain.content.article.valueobject.Body;
import com.deutschhub.domain.content.article.valueobject.Source;
import com.deutschhub.domain.content.article.valueobject.Summary;
import com.deutschhub.domain.shared.valueobject.UserId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class UpdateDraftService implements UpdateDraftUseCase {

    ArticleRepositoryPort articleRepositoryPort;
    CurrentUserPort currentUserPort;

    @Override
    public UpdateDraftResponse updateDraft(UpdateDraftCommand command) {
        if (command == null || command.articleId() == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        UserId actorId = currentUserPort.getCurrentUserId();

        Article article = articleRepositoryPort.findById(command.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        article.ensureOwnedBy(actorId);

        ArticleTitle title = new ArticleTitle(command.title());
        Summary summary = toSummary(command.summary());
        Body body = toBody(command.body());

        List<Source> sources = command.sources()
                .stream()
                .map(this::toSource)
                .toList();

        Instant now = Instant.now();

        article.updateDraft(title, summary, body, command.primaryCategoryId(), command.topicIds(), command.coverMediaId(),
                sources, actorId, now);

        articleRepositoryPort.save(article);

        return toResponse(article);
    }

    private Summary toSummary(String value) {
        return value == null ? null : new Summary(value);
    }

    private Body toBody(String value) {
        return value == null ? null : new Body(value);
    }

    private Source toSource(SourceCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        return new Source(command.title(), command.url());
    }

    private UpdateDraftResponse toResponse(Article article) {
        return new UpdateDraftResponse(article.getId(), article.getDraftVersionId(), article.getEditorialStatus(),
                article.getPublicationStatus());
    }
}
