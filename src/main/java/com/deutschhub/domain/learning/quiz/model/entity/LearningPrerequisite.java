package com.deutschhub.domain.learning.quiz.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.enums.LearningTargetType;

import java.util.UUID;

public class LearningPrerequisite {

    private final UUID id;
    private final LearningTargetType targetType;
    private final UUID targetId;

    protected LearningPrerequisite() {
        this.id = null;
        this.targetType = null;
        this.targetId = null;
    }

    public LearningPrerequisite(UUID id, LearningTargetType targetType, UUID targetId) {
        if (id == null || targetType == null || targetId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_LEARNING_PREREQUISITE_DATA);
        }

        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public static LearningPrerequisite create(LearningTargetType targetType, UUID targetId) {
        return new LearningPrerequisite(UUID.randomUUID(), targetType, targetId);
    }

    public UUID getId() {
        return id;
    }

    public LearningTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }
}