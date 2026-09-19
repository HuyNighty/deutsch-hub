package com.deutschhub.domain.learning.assessmentresult.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.assessment.model.enums.SkillDimension;
import com.deutschhub.domain.learning.learnerstate.model.enums.CurrentLevel;

import java.util.Objects;
import java.util.UUID;

public class ComponentResult {

    private final UUID componentId;
    private final SkillDimension skillDimension;
    private final double performance;
    private final CurrentLevel achievedLevel;

    private ComponentResult(UUID componentId, SkillDimension skillDimension, double performance, CurrentLevel achievedLevel) {
        this.componentId = Objects.requireNonNull(componentId, "Component Id cannot be null");
        this.skillDimension = Objects.requireNonNull(skillDimension, "Skill dimension cannot be null");
        this.performance = performance;
        this.achievedLevel = Objects.requireNonNull(achievedLevel, "Achieved level cannot be null");
    }

    public static ComponentResult create(UUID componentId, SkillDimension skillDimension, double performance, CurrentLevel achievedLevel) {
        if (performance < 0 || performance > 100) {
            throw new BusinessException(ErrorCode.COMPONENT_RESULT_INVALID_PERFORMANCE);
        }

        return new ComponentResult(componentId, skillDimension, performance, achievedLevel);
    }

    public UUID getComponentId() {
        return componentId;
    }

    public SkillDimension getSkillDimension() {
        return skillDimension;
    }

    public double getPerformance() {
        return performance;
    }

    public CurrentLevel getAchievedLevel() {
        return achievedLevel;
    }
}
