package com.deutschhub.domain.content.article.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.entity.ArticleVersion;
import com.deutschhub.domain.content.article.entity.ReviewCycle;
import com.deutschhub.domain.content.article.enums.EditorialStatus;
import com.deutschhub.domain.content.article.enums.PublicationStatus;
import com.deutschhub.domain.content.article.valueobject.*;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.*;

public class Article {

    private UUID id;
    private UserId ownerId;

    private Slug slug;

    private EditorialStatus editorialStatus;
    private PublicationStatus publicationStatus;

    private UUID draftVersionId;
    private UUID publishedVersionId;

    private List<ArticleVersion> versions;

    private Instant createdAt;
    private UserId createdBy;

    private Instant publishedAt;
    private UserId publishedBy;

    private Instant archivedAt;
    private UserId archivedBy;

    public static Article createDraft(UserId owner, Slug slug, Instant createdAt) {
        if (owner == null || slug == null || createdAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        Article article = new Article();

        article.id = UUID.randomUUID();

        article.slug = slug;

        article.ownerId = owner;

        article.editorialStatus = EditorialStatus.DRAFT;

        article.publicationStatus = PublicationStatus.UNPUBLISHED;

        article.createdAt = createdAt;
        article.createdBy = owner;

        article.versions = new ArrayList<>();

        ArticleVersion draft = ArticleVersion.createFirstDraft(UUID.randomUUID(), owner, createdAt);

        article.draftVersionId = draft.getId();

        article.versions.add(draft);

        return article;
    }

    public void updateDraft(ArticleTitle title, Summary summary, Body body, UUID primaryCategoryId, List<UUID> topicIds,
                            UUID coverMediaId, List<Source> sources, UserId modifiedBy, Instant modifiedAt) {
        ensureDraftEditable();

        ArticleVersion draft = getDraftVersion();

        draft.updateContent(title, summary, body, primaryCategoryId, topicIds, coverMediaId, sources, modifiedBy, modifiedAt);
    }

    public void submitReview(UserId submittedBy, Instant submittedAt) {
        ensureCanSubmitReview();

        ArticleVersion draft = getDraftVersion();

        ensureEditorialCompleteness(draft);

        ReviewCycle reviewCycle = new ReviewCycle(UUID.randomUUID(), submittedBy, submittedAt);

        draft.addReviewCycle(reviewCycle);

        editorialStatus = EditorialStatus.IN_REVIEW;
    }

    private void ensureCanSubmitReview() {
        boolean canSubmit = editorialStatus == EditorialStatus.DRAFT || editorialStatus == EditorialStatus.CHANGES_REQUESTED;

        if (!canSubmit) {
            throw new BusinessException(ErrorCode.ARTICLE_CAN_NOT_SUBMIT_REVIEW);
        }
    }

    private void ensureEditorialCompleteness(ArticleVersion draft) {
        if (draft.getTitle() == null || draft.getSummary() == null
                || draft.getBody() == null || draft.getPrimaryCategoryId() == null
                || draft.getCoverMediaId() == null || draft.getTopicIds().isEmpty()
                || draft.getSources().isEmpty()) {
            throw new BusinessException(ErrorCode.ARTICLE_DRAFT_INCOMPLETE);
        }
    }

    private void ensureDraftEditable() {
        if (editorialStatus != EditorialStatus.DRAFT && editorialStatus != EditorialStatus.CHANGES_REQUESTED) {
            throw new BusinessException(ErrorCode.ARTICLE_DRAFT_NOT_EDITABLE);
        }
    }

    public void ensureOwnedBy(UserId actorId) {
        if (actorId == null ||  !ownerId.equals(actorId)) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_OWNED_BY_ACTOR);
        }
    }

    private ArticleVersion getDraftVersion() {
        return versions.stream()
                .filter(version -> version.getId().equals(draftVersionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_DRAFT_VERSION_NOT_FOUND));
    }
}

