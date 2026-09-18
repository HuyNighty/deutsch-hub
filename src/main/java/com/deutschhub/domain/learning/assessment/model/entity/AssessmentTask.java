package com.deutschhub.domain.learning.assessment.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Objects;
import java.util.UUID;

public class AssessmentTask {

    private final UUID id;
    private int order;
    private final UUID quizRevisionId;

    private AssessmentTask(UUID id, UUID quizRevisionId) {
        this.id = Objects.requireNonNull(id);
        this.quizRevisionId = Objects.requireNonNull(quizRevisionId);
    }

    public static AssessmentTask create(UUID quizRevisionId) {
        return new AssessmentTask(UUID.randomUUID(), quizRevisionId);
    }

    public UUID getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public UUID getQuizRevisionId() {
        return quizRevisionId;
    }

    void changeOrder(int order) {
        if (order <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ASSESSMENT_TASK_ORDER);
        }

        this.order = order;
    }
}
