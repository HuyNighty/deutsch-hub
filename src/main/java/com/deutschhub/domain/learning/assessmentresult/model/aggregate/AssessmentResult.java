package com.deutschhub.domain.learning.assessmentresult.model.aggregate;

import com.deutschhub.domain.learning.assessmentresult.model.entity.ComponentResult;
import com.deutschhub.domain.learning.learnerstate.model.enums.CurrentLevel;

import java.util.*;

public class AssessmentResult {

    private final UUID id;
    private final UUID assessmentAttemptId;
    private final CurrentLevel targetLevel;
    private final List<ComponentResult> componentResults = new ArrayList<>();
    private final boolean passed;

    private AssessmentResult(UUID id, UUID assessmentAttemptId, CurrentLevel targetLevel, List<ComponentResult> componentResults, boolean passed) {
        this.id = Objects.requireNonNull(id, "Assessment result id cannot be null");
        this.assessmentAttemptId = Objects.requireNonNull(assessmentAttemptId, "Assessment attempt id cannot be null");
        this.targetLevel = Objects.requireNonNull(targetLevel, "Target level cannot be null");
        this.componentResults.addAll(Objects.requireNonNull(componentResults, "Component results cannot be null"));
        this.passed = passed;
    }

    public static AssessmentResult create(UUID assessmentAttemptId, CurrentLevel targetLevel, List<ComponentResult> componentResults, boolean passed) {
        return new AssessmentResult(UUID.randomUUID(), assessmentAttemptId, targetLevel, componentResults, passed);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssessmentAttemptId() {
        return assessmentAttemptId;
    }

    public CurrentLevel getTargetLevel() {
        return targetLevel;
    }

    public List<ComponentResult> getComponentResults() {
        return Collections.unmodifiableList(componentResults);
    }

    public boolean isPassed() {
        return passed;
    }
}