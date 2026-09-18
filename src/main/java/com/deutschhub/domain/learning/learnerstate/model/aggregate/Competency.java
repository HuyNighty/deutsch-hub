package com.deutschhub.domain.learning.learnerstate.model.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.learnerstate.model.entity.CompetencyDimension;
import com.deutschhub.domain.learning.learnerstate.model.enums.LearningDomain;
import com.deutschhub.domain.learning.learnerstate.model.enums.SkillDimension;

import java.util.*;

public class Competency {

    private final UUID id;
    private final UUID learnerId;
    private final LearningDomain learningDomain;
    private final Map<SkillDimension, CompetencyDimension> dimensions;

    private Competency(UUID id, UUID learnerId, LearningDomain learningDomain, Map<SkillDimension, CompetencyDimension> dimensions) {
        this.id = Objects.requireNonNull(id, "Competency id cannot be null");
        this.learnerId = Objects.requireNonNull(learnerId, "Learner id cannot be null");
        this.learningDomain = Objects.requireNonNull(learningDomain, "Learning domain cannot be null");
        this.dimensions = new HashMap<>(Objects.requireNonNull(dimensions, "Dimensions cannot be null"));
    }

    public static Competency create(UUID learnerId, LearningDomain learningDomain) {
        return new Competency(UUID.randomUUID(), learnerId, learningDomain, new HashMap<>());
    }

    public static Competency restore(UUID id, UUID learnerId, LearningDomain learningDomain, Map<SkillDimension, CompetencyDimension> dimensions) {
        return new Competency(id, learnerId, learningDomain, dimensions);
    }

    public void addDimension(SkillDimension skillDimension) {

        Objects.requireNonNull(skillDimension, "Skill dimension cannot be null");

        if (dimensions.containsKey(skillDimension)) {
            throw new BusinessException(ErrorCode.COMPETENCY_DIMENSION_ALREADY_EXISTS);
        }

        CompetencyDimension dimension = CompetencyDimension.create(skillDimension);

        dimensions.put(skillDimension, dimension);
    }

    public Optional<CompetencyDimension> getDimension(SkillDimension skillDimension) {
        return Optional.ofNullable(dimensions.get(skillDimension));
    }

    public Map<SkillDimension, CompetencyDimension> getDimensions() {
        return Collections.unmodifiableMap(dimensions);
    }
}
