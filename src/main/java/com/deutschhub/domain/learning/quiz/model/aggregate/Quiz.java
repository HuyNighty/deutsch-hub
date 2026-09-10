package com.deutschhub.domain.learning.quiz.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.entity.QuizRevision;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;
import com.deutschhub.domain.learning.quiz.model.enums.QuizVisibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Quiz implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID createdBy;

    private QuizVisibility visibility;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private final List<QuizRevision> revisions = new ArrayList<>();

    private Quiz(UUID id, UUID createdBy, QuizVisibility visibility) {
        this.id = Objects.requireNonNull(id);
        this.createdBy = Objects.requireNonNull(createdBy);

        this.visibility = Objects.requireNonNull(visibility);

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static Quiz createDraft(UUID createdBy) {
        return new Quiz(UUID.randomUUID(), createdBy, QuizVisibility.PRIVATE);
    }

    public void addRevision(QuizRevision revision) {
        ensureCanMutateBy(createdBy, false);
        addRevisionInternal(revision);
    }

    public void addRevision(QuizRevision revision, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        addRevisionInternal(revision);
    }

    public void submitRevisionForReview(UUID revisionId) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        if (hasInReviewRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_IN_REVIEW_REVISION);
        }

        revision.submitForReview();
    }

    public void withdrawRevisionSubmission(UUID revisionId) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        revision.withdrawSubmission();
    }

    public void publishRevision(UUID revisionId) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        if (revision.getStatus() != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        if (hasPublishedRevision()) {
            QuizRevision currentPublished = findPublishedRevision();
            currentPublished.markHistorical();
        }

        revision.publish();
    }

    private QuizRevision findRevision(UUID revisionId) {
        if (revisionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_REVISION);
        }

        return revisions.stream()
                .filter(r -> r.getId().equals(revisionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_REVISION_NOT_FOUND));
    }

    private QuizRevision findPublishedRevision() {
        return revisions.stream()
                .filter(r -> r.getStatus() == QuizRevisionStatus.PUBLISHED)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_REVISION_NOT_FOUND));
    }

    private void addRevisionInternal(QuizRevision revision) {
        ensureNotDeleted();

        if (revision == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_REVISION);
        }

        if (revision.getStatus() == QuizRevisionStatus.DRAFT && hasDraftRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_DRAFT_REVISION);
        }

        if (revision.getStatus() == QuizRevisionStatus.IN_REVIEW && hasInReviewRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_IN_REVIEW_REVISION);
        }

        if (revision.getStatus() == QuizRevisionStatus.PUBLISHED && hasPublishedRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_PUBLISHED_REVISION);
        }

        revisions.add(revision);
        touch();
    }

    private boolean hasDraftRevision() {
        return revisions.stream().anyMatch(r -> r.getStatus() == QuizRevisionStatus.DRAFT);
    }

    private boolean hasPublishedRevision() {
        return revisions.stream().anyMatch(r -> r.getStatus() == QuizRevisionStatus.PUBLISHED);
    }

    private boolean hasInReviewRevision() {
        return revisions.stream().anyMatch(r -> r.getStatus() == QuizRevisionStatus.IN_REVIEW);
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_DELETED);
        }
    }

    private void ensureCanMutateBy(UUID actorId, boolean isAdmin) {
        ensureNotDeleted();

        if (isAdmin) {
            return;
        }

        if (!createdBy.equals(actorId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN_ACTION);
        }
    }

    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public void softDelete() {
        ensureCanMutateBy(createdBy, false);
        softDeleteInternal();
    }

    public void softDelete(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        softDeleteInternal();
    }

    private void softDeleteInternal() {
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public QuizVisibility getVisibility() {
        return visibility;
    }

    public List<QuizRevision> getRevisions() {
        return Collections.unmodifiableList(revisions);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}