package com.deutschhub.domain.learning.quiz.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.entity.QuizRevision;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;
import com.deutschhub.domain.learning.quiz.model.enums.QuizStatus;
import com.deutschhub.domain.learning.quiz.model.enums.QuizVisibility;
import com.deutschhub.domain.learning.quiz.model.valueobject.ReviewFeedback;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Quiz implements Auditable, SoftDeletable {

    private final UUID id;
    private final UUID createdBy;
    private UUID author;

    private QuizVisibility visibility;
    private QuizStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private final List<QuizRevision> revisions = new ArrayList<>();

    private Quiz(UUID id, UUID createdBy, UUID author, QuizVisibility visibility, QuizStatus status) {
        this.id = Objects.requireNonNull(id);
        this.createdBy = Objects.requireNonNull(createdBy);
        this.author =  Objects.requireNonNull(author);

        this.visibility = Objects.requireNonNull(visibility);
        this.status = Objects.requireNonNull(status);

        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.deletedAt = null;
    }

    public static Quiz createDraft(UUID createdBy) {
        Quiz quiz = new Quiz(UUID.randomUUID(), createdBy, createdBy, QuizVisibility.PRIVATE, QuizStatus.DRAFT);

        quiz.revisions.add(QuizRevision.createDraft(1));

        return quiz;
    }

    public QuizRevision createRevision() {
        ensureNotDeleted();

        if (hasDraftRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_DRAFT_REVISION);
        }

        int nextRevisionNumber = getNextRevisionNumber();

        QuizRevision revision = QuizRevision.createDraft(nextRevisionNumber);

        revisions.add(revision);
        touch();

        return revision;
    }

    public void addRevision(QuizRevision revision) {
        ensureCanMutateBy(createdBy, false);
        addRevisionInternal(revision);
    }

    public void addRevision(QuizRevision revision, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        addRevisionInternal(revision);
    }

    public void submitRevisionForReview(UUID revisionId, UserId submittedBy, Instant submittedAt) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        if (hasInReviewRevision()) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_HAS_IN_REVIEW_REVISION);
        }

        revision.submitForReview(submittedBy, submittedAt);
        touch();
    }

    public void withdrawRevisionSubmission(UUID revisionId, UserId withdrawnBy, Instant withdrawnAt) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        revision.withdrawSubmission(withdrawnBy, withdrawnAt);

        touch();
    }

    public void publishRevision(UUID revisionId, UserId reviewer, Instant reviewedAt) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        if (revision.getStatus() != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        QuizRevision currentPublished = hasPublishedRevision() ? findPublishedRevision() : null;

        revision.publish(reviewer, reviewedAt);

        if (currentPublished != null) {
            currentPublished.markHistorical();
        }

        touch();
    }

    public void discardDraftRevision(UUID revisionId) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        if (revision.getStatus() != QuizRevisionStatus.DRAFT) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        revisions.remove(revision);
        touch();
    }

    public void activate() {
        ensureNotDeleted();

        if (status == QuizStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_STATUS);
        }

        status = QuizStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        ensureNotDeleted();

        if (status != QuizStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_STATUS);
        }

        status = QuizStatus.DRAFT;
        touch();
    }

    public void requestRevisionChanges(UUID revisionId, UserId reviewer, ReviewFeedback feedback, Instant reviewedAt) {
        ensureNotDeleted();

        QuizRevision revision = findRevision(revisionId);

        revision.requestChanges(reviewer, feedback, reviewedAt);

        touch();
    }

    public void changeAuthor(UUID authorId, UUID newAuthorId, boolean isAdmin) {
        ensureNotDeleted();
        ensureCanChangeAuthor();

        if (newAuthorId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_AUTHOR);
        }

        if (!isAdmin && !author.equals(authorId)) {
            throw new BusinessException(ErrorCode.QUIZ_FORBIDDEN_ACTION);
        }

        if (author.equals(newAuthorId)) {
            throw new BusinessException(ErrorCode.QUIZ_AUTHOR_ALREADY_ASSIGNED);
        }

        this.author = newAuthorId;
        touch();
    }

    public void archive() {
        ensureNotDeleted();

        if (status != QuizStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_STATUS);
        }

        status = QuizStatus.ARCHIVED;
        touch();
    }

    public void changeVisibility(QuizVisibility visibility, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        this.visibility = Objects.requireNonNull(visibility);
        touch();
    }

    private void ensureCanChangeAuthor() {
        if (status != QuizStatus.ACTIVE && status != QuizStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_STATUS);
        }
    }

    private int getNextRevisionNumber() {
        return revisions.stream()
                .mapToInt(QuizRevision::getRevisionNumber)
                .max()
                .orElse(0) + 1;
    }

    private QuizRevision findRevision(UUID revisionId) {
        if (revisionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_REVISION);
        }

        return revisions.stream()
                .filter(revision -> revision.getId().equals(revisionId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_REVISION_NOT_FOUND));
    }

    private QuizRevision findPublishedRevision() {
        return revisions.stream()
                .filter(revision -> revision.getStatus() == QuizRevisionStatus.PUBLISHED)
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
        return revisions.stream()
                .anyMatch(revision -> revision.getStatus() == QuizRevisionStatus.DRAFT);
    }

    private boolean hasPublishedRevision() {
        return revisions.stream()
                .anyMatch(revision -> revision.getStatus() == QuizRevisionStatus.PUBLISHED);
    }

    private boolean hasInReviewRevision() {
        return revisions.stream()
                .anyMatch(revision -> revision.getStatus() == QuizRevisionStatus.IN_REVIEW);
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

    public QuizStatus getStatus() {
        return status;
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

    public UUID getAuthor() {
        return author;
    }
}