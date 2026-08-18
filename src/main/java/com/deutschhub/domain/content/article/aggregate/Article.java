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
import java.util.stream.Collectors;

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

    private UserId ownershipTransferredBy;
    private Instant ownershipTransferredAt;

    public static Article createDraft(UserId ownerId, ArticleTitle title, Slug slug, Instant createdAt) {
        if (ownerId == null || title == null || slug == null || createdAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        Article article = new Article();

        article.id = UUID.randomUUID();

        article.slug = slug;

        article.ownerId = ownerId;

        article.editorialStatus = EditorialStatus.DRAFT;

        article.publicationStatus = PublicationStatus.UNPUBLISHED;

        article.createdAt = createdAt;
        article.createdBy = ownerId;

        article.versions = new ArrayList<>();

        ArticleVersion draft = ArticleVersion.createFirstDraft(UUID.randomUUID(), title, ownerId, createdAt);

        article.draftVersionId = draft.getId();

        article.versions.add(draft);

        return article;
    }

    public static Article restore(UUID id, UserId ownerId, Slug slug, EditorialStatus editorialStatus,
                                  PublicationStatus publicationStatus, UUID draftVersionId, UUID publishedVersionId,
                                  List<ArticleVersion> versions, Instant createdAt, UserId createdBy, Instant publishedAt,
                                  UserId publishedBy, Instant archivedAt, UserId archivedBy, UserId ownershipTransferredBy,
                                  Instant ownershipTransferredAt) {
        validateRestoredData(id, ownerId, slug, editorialStatus, publicationStatus, draftVersionId, publishedVersionId,
                versions, createdAt, createdBy, publishedAt, publishedBy, archivedAt, archivedBy, ownershipTransferredBy, ownershipTransferredAt);

        Article article = new Article();

        article.id = id;
        article.ownerId = ownerId;
        article.slug = slug;
        article.editorialStatus = editorialStatus;
        article.publicationStatus = publicationStatus;
        article.draftVersionId = draftVersionId;
        article.publishedVersionId = publishedVersionId;
        article.versions = new ArrayList<>(versions);
        article.createdAt = createdAt;
        article.createdBy = createdBy;
        article.publishedAt = publishedAt;
        article.publishedBy = publishedBy;
        article.archivedAt = archivedAt;
        article.archivedBy = archivedBy;
        article.ownershipTransferredBy = ownershipTransferredBy;
        article.ownershipTransferredAt = ownershipTransferredAt;

        return article;
    }

    public void updateDraft(ArticleTitle title, Summary summary, Body body, UUID primaryCategoryId, Set<UUID> topicIds,
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

    public void withdrawReview(UserId withdrawnBy, Instant withdrawnAt) {
        ReviewCycle reviewCycle = getCurrentReviewCycle();

        reviewCycle.markWithdrawn(withdrawnBy, withdrawnAt);

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

    public void createNewDraft(UserId createdBy, Instant createdAt) {
        ensureCanCreateNewDraft();

        ArticleVersion published = getPublishedVersion();

        VersionNumber versionNumber = getNextVersionNumber();

        ArticleVersion draft = ArticleVersion.createFrom(published, versionNumber, createdBy, createdAt);

        versions.add(draft);

        draftVersionId = draft.getId();

        editorialStatus = EditorialStatus.DRAFT;
    }

    public void archive(UserId archivedBy, Instant archivedAt) {
        ensureCanArchive(archivedBy, archivedAt);

        if (editorialStatus == EditorialStatus.IN_REVIEW) {
            ReviewCycle reviewCycle = getCurrentReviewCycle();

            reviewCycle.markWithdrawn(archivedBy, archivedAt);

            editorialStatus = EditorialStatus.DRAFT;
        }


        this.publicationStatus = PublicationStatus.ARCHIVED;

        this.archivedBy = archivedBy;
        this.archivedAt = archivedAt;
    }

    public void transferOwnership( UserId newOwner, UserId transferredBy, Instant transferredAt) {
        ensureCanTransferOwnership(newOwner, transferredBy, transferredAt);

        this.ownerId = newOwner;
        this.ownershipTransferredBy = transferredBy;
        this.ownershipTransferredAt = transferredAt;

    }

    private VersionNumber getNextVersionNumber() {
        int maxVersion = versions
                .stream()
                .mapToInt(version -> version.getVersionNumber().value())
                .max()
                .orElse(0);

        return new VersionNumber(maxVersion + 1);
    }

    private void ensureCanTransferOwnership(UserId newOwner, UserId transferredBy, Instant transferredAt) {
        if (newOwner == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_OWNER);
        }

        if (transferredBy == null || transferredAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_OWNERSHIP_TRANSFER_DATA);
        }

        if (editorialStatus == EditorialStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.ARTICLE_OWNERSHIP_TRANSFER_NOT_ALLOWED);
        }

        if (ownerId.equals(newOwner)) {
            throw new BusinessException(ErrorCode.ARTICLE_ALREADY_OWNED_BY_ACTOR);
        }
    }

    private void ensureCanArchive(UserId archivedBy, Instant archivedAt) {
        if (archivedBy == null || archivedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_ARCHIVE_DATA);
        }

        if (publicationStatus != PublicationStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.ARTICLE_CAN_NOT_ARCHIVE);
        }
    }

    private void ensureCanCreateNewDraft() {
        boolean validEditorialState = editorialStatus == EditorialStatus.IDLE;

        boolean validPublicationState = publicationStatus == PublicationStatus.PUBLISHED
                || publicationStatus == PublicationStatus.ARCHIVED;

        if (!validEditorialState || !validPublicationState) {
            throw new BusinessException(ErrorCode.ARTICLE_NEW_DRAFT_NOT_ALLOWED);
        }

        if (publishedVersionId == null) {
            throw new BusinessException(ErrorCode.ARTICLE_PUBLISHED_VERSION_NOT_FOUND);
        }

        if (draftVersionId != null) {
            throw new BusinessException(ErrorCode.ARTICLE_DRAFT_ALREADY_EXISTS);
        }
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
        if (actorId == null || !ownerId.equals(actorId)) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_OWNED_BY_ACTOR);
        }
    }

    private ArticleVersion getDraftVersion() {
        return versions.stream()
                .filter(version -> version.getId().equals(draftVersionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_DRAFT_VERSION_NOT_FOUND));
    }

    private ArticleVersion getPublishedVersion() {
        return versions
                .stream()
                .filter(version -> version.getId().equals(publishedVersionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_PUBLISHED_VERSION_NOT_FOUND));
    }

    private static void validateRestoredData(UUID id, UserId ownerId, Slug slug, EditorialStatus editorialStatus,
                                             PublicationStatus publicationStatus, UUID draftVersionId, UUID publishedVersionId,
                                             List<ArticleVersion> versions, Instant createdAt, UserId createdBy, Instant publishedAt,
                                             UserId publishedBy, Instant archivedAt, UserId archivedBy, UserId ownershipTransferredBy,
                                             Instant ownershipTransferredAt) {
        if (id == null || ownerId == null || slug == null || editorialStatus == null || publicationStatus == null
                || versions == null || createdAt == null || createdBy == null) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_DATA);
        }

        if (versions.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(ErrorCode.INVALID_ARTICLE_VERSION_DATA);
        }

        validateVersionReferences(draftVersionId, publishedVersionId, versions);

        validateAuditState(publishedAt, publishedBy, archivedAt, archivedBy, ownershipTransferredBy, ownershipTransferredAt);
    }

    private static void validateVersionReferences(UUID draftVersionId, UUID publishedVersionId, List<ArticleVersion> versions) {
        Set<UUID> versionIds = versions.stream()
                .map(ArticleVersion::getId)
                .collect(Collectors.toSet());

        if (draftVersionId != null && !versionIds.contains(draftVersionId)) {
            throw new BusinessException(ErrorCode.ARTICLE_DRAFT_VERSION_NOT_FOUND);
        }

        if (publishedVersionId != null && !versionIds.contains(publishedVersionId)) {
            throw new BusinessException(ErrorCode.ARTICLE_PUBLISHED_VERSION_NOT_FOUND);
        }
    }

    private static void validateAuditState(Instant publishedAt, UserId publishedBy, Instant archivedAt, UserId archivedBy,
                                           UserId ownershipTransferredBy, Instant ownershipTransferredAt) {
        validatePair(publishedAt, publishedBy, ErrorCode.INVALID_ARTICLE_PUBLICATION_DATA);

        validatePair(archivedAt, archivedBy, ErrorCode.INVALID_ARTICLE_ARCHIVE_DATA);

        validatePair(ownershipTransferredAt, ownershipTransferredBy, ErrorCode.INVALID_ARTICLE_OWNERSHIP_TRANSFER_DATA);
    }

    private static void validatePair(Object first, Object second, ErrorCode errorCode) {
        if ((first == null) != (second == null)) {
            throw new BusinessException(errorCode);
        }
    }

    public UUID getId() {
        return id;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public Slug getSlug() {
        return slug;
    }

    public EditorialStatus getEditorialStatus() {
        return editorialStatus;
    }

    public PublicationStatus getPublicationStatus() {
        return publicationStatus;
    }

    public UUID getDraftVersionId() {
        return draftVersionId;
    }

    public UUID getPublishedVersionId() {
        return publishedVersionId;
    }

    public List<ArticleVersion> getVersions() {
        return versions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public UserId getPublishedBy() {
        return publishedBy;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public UserId getArchivedBy() {
        return archivedBy;
    }

    public UserId getOwnershipTransferredBy() {
        return ownershipTransferredBy;
    }

    public Instant getOwnershipTransferredAt() {
        return ownershipTransferredAt;
    }

    public ArticleVersion getCurrentDraft() {
        return getDraftVersion();
    }
}

