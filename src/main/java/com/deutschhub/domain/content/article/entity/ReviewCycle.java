package com.deutschhub.domain.content.article.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.content.article.enums.ReviewResult;
import com.deutschhub.domain.content.article.valueobject.ReviewFeedback;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

public class ReviewCycle {

    private UUID id;

    private UserId submittedBy;
    private Instant submittedAt;

    private UserId reviewedBy;
    private Instant reviewedAt;

    private UserId withdrawnBy;
    private Instant withdrawnAt;

    private ReviewResult result;

    private ReviewFeedback feedback;

    protected ReviewCycle() {
    }

    private ReviewCycle(UUID id, UserId submittedBy, Instant submittedAt, UserId reviewedBy, Instant reviewedAt,
                        UserId withdrawnBy, Instant withdrawnAt, ReviewResult result, ReviewFeedback feedback) {
        this.id = id;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.withdrawnBy = withdrawnBy;
        this.withdrawnAt = withdrawnAt;
        this.result = result;
        this.feedback = feedback;
    }

    public ReviewCycle(UUID id, UserId submittedBy, Instant submittedAt) {
        if (id == null || submittedBy == null || submittedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_CYCLE_DATA);
        }

        this.id = id;
        this.submittedBy = submittedBy;
        this.submittedAt = submittedAt;

        this.result = ReviewResult.PENDING;
    }

    public static ReviewCycle restore(UUID id, UserId submittedBy, Instant submittedAt, UserId reviewedBy,
                                      Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt, ReviewResult result,
                                      ReviewFeedback feedback) {
        validateRestoredState(id, submittedBy, submittedAt, reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, result, feedback);

        return new ReviewCycle(id, submittedBy, submittedAt, reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, result, feedback);
    }

    public void markApproved(UserId reviewer, Instant reviewedAt) {
        completeReview(reviewer, reviewedAt, ReviewResult.APPROVED, null);
    }

    public void markChangesRequested(UserId reviewer, ReviewFeedback feedback, Instant reviewedAt) {
        if (feedback == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_FEEDBACK);
        }

        completeReview(reviewer, reviewedAt, ReviewResult.CHANGES_REQUESTED, feedback);
    }

    public void markWithdrawn(UserId withdrawnBy, Instant withdrawnAt) {
        ensurePending();

        if (withdrawnAt == null || withdrawnBy == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_WITHDRAWAL_DATA);
        }

        this.withdrawnBy = withdrawnBy;
        this.withdrawnAt = withdrawnAt;
        this.result = ReviewResult.WITHDRAWN;
    }

    private void completeReview(UserId reviewer, Instant reviewedAt, ReviewResult result, ReviewFeedback feedback) {
        ensurePending();

        if (reviewer == null || reviewedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_COMPLETION_DATA);
        }

        if (result == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_RESULT);
        }

        this.reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
        this.result = result;
        this.feedback = feedback;
    }

    private void ensurePending() {

        if (this.result != ReviewResult.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATE);
        }
    }

    private static void validateRestoredState(UUID id, UserId submittedBy, Instant submittedAt, UserId reviewedBy,
                                              Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt,
                                              ReviewResult result, ReviewFeedback feedback) {
        if (id == null || submittedBy == null || submittedAt == null || result == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_CYCLE_DATA);
        }

        switch (result) {
            case PENDING -> requirePendingState(
                    reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, feedback
            );
            case APPROVED -> requireApprovedState(
                    reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, feedback
            );
            case CHANGES_REQUESTED -> requireChangesRequestedState(
                    reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, feedback
            );
            case WITHDRAWN -> requireWithdrawnState(
                    reviewedBy, reviewedAt, withdrawnBy, withdrawnAt, feedback
            );
        }
    }

    private static void requirePendingState(UserId reviewedBy, Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt, ReviewFeedback feedback) {
        if (reviewedBy != null || reviewedAt != null || withdrawnBy != null || withdrawnAt != null || feedback != null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATE);
        }
    }

    private static void requireApprovedState(UserId reviewedBy, Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt, ReviewFeedback feedback) {
        if (reviewedBy == null || reviewedAt == null || withdrawnBy != null || withdrawnAt != null || feedback != null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATE);
        }
    }

    private static void requireChangesRequestedState(UserId reviewedBy, Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt, ReviewFeedback feedback) {
        if (reviewedBy == null || reviewedAt == null || withdrawnBy != null || withdrawnAt != null || feedback == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATE);
        }
    }

    private static void requireWithdrawnState(UserId reviewedBy, Instant reviewedAt, UserId withdrawnBy, Instant withdrawnAt, ReviewFeedback feedback) {
        if (reviewedBy != null || reviewedAt != null || withdrawnBy == null || withdrawnAt == null || feedback != null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_STATE);
        }
    }

    public boolean isPending() {
        return result == ReviewResult.PENDING;
    }

    public boolean isApproved() {
        return result == ReviewResult.APPROVED;
    }

    public boolean isWithdrawn() {
        return result == ReviewResult.WITHDRAWN;
    }

    public boolean isChangesRequested() {
        return result == ReviewResult.CHANGES_REQUESTED;
    }

    public UUID getId() {
        return id;
    }

    public UserId getSubmittedBy() {
        return submittedBy;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public UserId getReviewedBy() {
        return reviewedBy;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public UserId getWithdrawnBy() {
        return withdrawnBy;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    public ReviewResult getResult() {
        return result;
    }

    public ReviewFeedback getFeedback() {
        return feedback;
    }
}
