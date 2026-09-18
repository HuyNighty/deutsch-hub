package com.deutschhub.domain.learning.learnerstate.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.learnerstate.model.enums.CurrentLevel;
import com.deutschhub.domain.learning.learnerstate.model.enums.SkillDimension;

import java.util.Objects;
import java.util.UUID;

public class CompetencyDimension {

    private final UUID id;
    private final SkillDimension skillDimension;
    private CurrentLevel currentLevel;

    private CompetencyDimension(UUID id, SkillDimension skillDimension, CurrentLevel currentLevel) {
        this.id = Objects.requireNonNull(id, "Competency dimension id cannot be null");
        this.skillDimension = Objects.requireNonNull(skillDimension, "Skill dimension cannot be null");
        this.currentLevel = Objects.requireNonNull(currentLevel, "Current level cannot be null");
    }

    public static CompetencyDimension create(SkillDimension skillDimension) {
        return new CompetencyDimension(UUID.randomUUID(), skillDimension, CurrentLevel.A0);
    }

    public static CompetencyDimension restore(UUID id, SkillDimension skillDimension, CurrentLevel currentLevel) {
        return new CompetencyDimension(id, skillDimension, currentLevel);
    }

    public void promoteTo(CurrentLevel newLevel) {
        Objects.requireNonNull(newLevel, "New level cannot be null");

        if (newLevel.isLowerThan(currentLevel)) {
            throw new BusinessException(ErrorCode.COMPETENCY_DIMENSION_LEVEL_CANNOT_DECREASE);
        }

        this.currentLevel = newLevel;
    }

    public UUID getId() {
        return id;
    }

    public SkillDimension getSkillDimension() {
        return skillDimension;
    }

    public CurrentLevel getCurrentLevel() {
        return currentLevel;
    }
}
