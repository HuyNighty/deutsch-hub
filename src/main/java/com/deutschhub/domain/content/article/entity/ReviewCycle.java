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

    private ReviewResult result;

    private ReviewFeedback feedback;

    protected ReviewCycle() {
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

    public void markApproved(UserId reviewer, Instant reviewedAt) {
        completeReview(reviewer, reviewedAt, ReviewResult.APPROVED, null);
    }

    public void markChangesRequested(UserId reviewer, ReviewFeedback feedback, Instant reviewedAt) {
        if (feedback == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_FEEDBACK);
        }

        completeReview(reviewer, reviewedAt, ReviewResult.CHANGES_REQUESTED, feedback);
    }

    public void markWithdrawn(Instant withdrawnAt) {
        if (withdrawnAt == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_CYCLE_DATA);
        }
        ensurePending();

        this.reviewedAt = withdrawnAt;
        this.result = ReviewResult.WITHDRAWN;
    }

    private void completeReview(UserId reviewer, Instant reviewedAt, ReviewResult result, ReviewFeedback feedback) {
        ensurePending();

        if (id == null || submittedBy == null || submittedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_REVIEW_CYCLE_DATA);
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


}
