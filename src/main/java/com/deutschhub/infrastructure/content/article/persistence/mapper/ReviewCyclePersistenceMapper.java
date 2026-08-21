package com.deutschhub.infrastructure.content.article.persistence.mapper;

import com.deutschhub.domain.content.article.entity.ReviewCycle;
import com.deutschhub.domain.content.article.enums.ReviewResult;
import com.deutschhub.domain.content.article.valueobject.ReviewFeedback;
import com.deutschhub.domain.shared.valueobject.UserId;
import com.deutschhub.infrastructure.content.article.persistence.entity.ReviewCycleJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewCyclePersistenceMapper {

    public ReviewCycleJpaEntity toJpa(ReviewCycle reviewCycle) {
        if (reviewCycle == null) {
            return null;
        }

        return ReviewCycleJpaEntity
                .builder()
                .id(reviewCycle.getId())
                .submittedBy(reviewCycle.getSubmittedBy().value())
                .submittedAt(reviewCycle.getSubmittedAt())
                .reviewedBy(toUuid(reviewCycle.getReviewedBy()))
                .reviewedAt(reviewCycle.getReviewedAt())
                .withdrawnBy(toUuid(reviewCycle.getWithdrawnBy()))
                .withdrawnAt(reviewCycle.getWithdrawnAt())
                .result(reviewCycle.getResult().name())
                .feedback(toString(reviewCycle.getFeedback()))
                .build();
    }

    public ReviewCycle toDomain(ReviewCycleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ReviewCycle.restore(
                entity.getId(),
                toUserId(entity.getSubmittedBy()),
                entity.getSubmittedAt(),
                toUserId(entity.getReviewedBy()),
                entity.getReviewedAt(),
                toUserId(entity.getWithdrawnBy()),
                entity.getWithdrawnAt(),
                toReviewResult(entity.getResult()),
                toReviewFeedback(entity.getFeedback())
        );
    }

    private UUID toUuid(UserId userId) {
        return userId == null ? null : userId.value();
    }

    private String toString(ReviewFeedback feedback) {
        return feedback == null ? null : feedback.value();
    }

    private UserId toUserId(UUID userId) {
        return userId == null ? null : UserId.of(userId);
    }

    private ReviewResult toReviewResult(String result) {
        return result == null ? null : ReviewResult.valueOf(result);
    }

    private ReviewFeedback toReviewFeedback(String feedback) {
        return feedback == null ? null : new ReviewFeedback(feedback);
    }
}

