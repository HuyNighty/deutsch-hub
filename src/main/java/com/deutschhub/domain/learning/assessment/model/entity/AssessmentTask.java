package com.deutschhub.domain.learning.assessment.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;

import java.util.Objects;
import java.util.UUID;

public class AssessmentTask {

    private final UUID id;
    private int order;
    private final UUID quizRevisionId;

    private AssessmentTask(UUID id, int order, UUID quizRevisionId) {
        this.id = Objects.requireNonNull(id);
        this.order = validateOrder(order);
        this.quizRevisionId = Objects.requireNonNull(quizRevisionId);
    }


    public static AssessmentTask create(int order, UUID quizRevisionId) {
        return new AssessmentTask(UUID.randomUUID(), order, quizRevisionId);
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

    public void changeOrder(int order) {
        this.order = validateOrder(order);
    }

    private int validateOrder(int order) {
        if (order <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ASSESSMENT_TASK_ORDER);
        }

        return order;
    }
}
