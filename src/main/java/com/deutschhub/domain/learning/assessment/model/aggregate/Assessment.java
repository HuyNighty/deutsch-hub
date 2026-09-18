package com.deutschhub.domain.learning.assessment.model.aggregate;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.assessment.model.entity.AssessmentComponent;
import com.deutschhub.domain.learning.assessment.model.entity.AssessmentTask;
import com.deutschhub.domain.learning.assessment.model.enums.AssessmentStatus;
import com.deutschhub.domain.learning.assessment.model.enums.ExecutionMode;

import java.util.*;

public class Assessment {

    private final UUID id;
    private AssessmentStatus status;

    private final List<AssessmentComponent> components = new ArrayList<>();

    private Assessment(UUID id) {
        this.id = id;
        this.status = AssessmentStatus.DRAFT;
    }

    public static Assessment create() {
        return new Assessment(UUID.randomUUID());
    }

    public void addComponent(AssessmentComponent component) {
        ensureDraft();

        Objects.requireNonNull(component);

        boolean duplicateDimension = components.stream()
                .anyMatch(existing ->
                        existing.getSkillDimension().equals(component.getSkillDimension())
                );

        if (duplicateDimension) {
            throw new BusinessException(ErrorCode.ASSESSMENT_COMPONENT_DUPLICATE_SKILL_DIMENSION);
        }

        components.add(component);
    }

    public void removeComponent(UUID componentId) {
        ensureDraft();

        Objects.requireNonNull(componentId);

        boolean removed = components.removeIf(component -> component.getId().equals(componentId));

        if (!removed) {
            throw new BusinessException(ErrorCode.ASSESSMENT_COMPONENT_NOT_FOUND);
        }
    }

    public void activate() {
        ensureDraft();

        if (components.isEmpty() || components.size() > 4) {
            throw new BusinessException(ErrorCode.ASSESSMENT_INVALID_COMPONENT_COUNT);
        }

        for (AssessmentComponent component : components) {
            if (component.getTasks().isEmpty()) {
                throw new BusinessException(ErrorCode.ASSESSMENT_COMPONENT_HAS_NO_TASKS);
            }
        }

        this.status = AssessmentStatus.ACTIVE;
    }

    public void deactivate() {
        if (status != AssessmentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ASSESSMENT_INVALID_STATUS);
        }

        this.status = AssessmentStatus.DRAFT;
    }

    public void archive() {
        if (status != AssessmentStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ASSESSMENT_INVALID_STATUS);
        }

        this.status = AssessmentStatus.ARCHIVED;
    }

    public void addTask(UUID componentId, AssessmentTask task) {
        ensureDraft();

        AssessmentComponent component = findComponent(componentId);
        component.addTask(task);
    }

    public void removeTask(UUID componentId, UUID taskId) {
        ensureDraft();

        AssessmentComponent component = findComponent(componentId);
        component.removeTask(taskId);
    }

    public void reorderTask(UUID componentId, UUID taskId, int newOrder) {
        ensureDraft();

        AssessmentComponent component = findComponent(componentId);
        component.reorderTask(taskId, newOrder);
    }

    public void changeComponentExecutionMode(UUID componentId, ExecutionMode executionMode) {
        ensureDraft();

        AssessmentComponent component = findComponent(componentId);
        component.changeExecutionMode(executionMode);
    }

    public UUID getId() {
        return id;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public List<AssessmentComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    private void ensureDraft() {
        if (status != AssessmentStatus.DRAFT) {
            throw new BusinessException(ErrorCode.ASSESSMENT_INVALID_STATUS);
        }
    }

    private AssessmentComponent findComponent(UUID componentId) {
        return components.stream()
                .filter(component -> component.getId().equals(componentId))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ASSESSMENT_COMPONENT_NOT_FOUND)
                );
    }
}
