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

    public void withdrawReview(Instant withdrawnAt) {
        ReviewCycle reviewCycle = getCurrentReviewCycle();

        reviewCycle.markWithdrawn(withdrawnAt);

        editorialStatus = EditorialStatus.DRAFT;
    }

    public void requestChanges(UserId reviewer, ReviewFeedback feedback, Instant reviewedAt ) {
        ReviewCycle reviewCycle = getCurrentReviewCycle();

        reviewCycle.markChangesRequested(reviewer, feedback, reviewedAt);

        editorialStatus = EditorialStatus.CHANGES_REQUESTED;
    }

    public void publish(UserId publishedBy, Instant publishedAt) {
        ensurePublish(publishedBy, publishedAt);

        ArticleVersion draft = getDraftVersion();

        ensureEditorialCompleteness(draft);

        ReviewCycle reviewCycle = getCurrentReviewCycle();

        reviewCycle.markApproved(publishedBy, publishedAt);

        this.publishedVersionId = draft.getId();
        this.draftVersionId = null;

        this.publicationStatus = PublicationStatus.PUBLISHED;
        this.editorialStatus = EditorialStatus.IDLE;

        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
    }

    private void ensurePublish(UserId publishedBy, Instant publishedAt) {
        if (publishedBy == null || publishedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_PUBLICATION_DATA);
        }
    }

    private ReviewCycle getCurrentReviewCycle() {
        ensureReviewInProgress();

        ArticleVersion draft = getDraftVersion();

        return draft.getCurrentReviewCycle();
    }

    private void ensureReviewInProgress() {
        if (editorialStatus != EditorialStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.ARTICLE_REVIEW_NOT_IN_PROGRESS);
        }
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

