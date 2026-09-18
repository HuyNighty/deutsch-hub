package com.deutschhub.domain.learning.assessment.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.assessment.model.enums.ExecutionMode;
import com.deutschhub.domain.learning.assessment.model.enums.SkillDimension;

import java.util.*;

public class AssessmentComponent {

    private final UUID id;
    private final SkillDimension skillDimension;
    private ExecutionMode executionMode;

    private final List<AssessmentTask> tasks = new ArrayList<>();

    private AssessmentComponent(UUID id, SkillDimension skillDimension, ExecutionMode executionMode) {
        this.id = Objects.requireNonNull(id);
        this.skillDimension = Objects.requireNonNull(skillDimension);
        this.executionMode = Objects.requireNonNull(executionMode);
    }

    public static AssessmentComponent create(SkillDimension skillDimension, ExecutionMode executionMode) {
        return new AssessmentComponent(UUID.randomUUID(), skillDimension, executionMode);
    }

    public void changeExecutionMode(ExecutionMode executionMode) {
        this.executionMode = Objects.requireNonNull(executionMode);
    }

    public void addTask(AssessmentTask task) {
        Objects.requireNonNull(task);

        int nextOrder = tasks.size() + 1;

        task.changeOrder(nextOrder);

        tasks.add(task);
    }

    public void removeTask(UUID taskId) {
        Objects.requireNonNull(taskId);

        boolean removed = tasks.removeIf(task -> task.getId().equals(taskId));

        if (!removed) {
            throw new BusinessException(ErrorCode.ASSESSMENT_TASK_NOT_FOUND);
        }

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).changeOrder(i + 1);
        }
    }

    public void reorderTask(UUID taskId, int newOrder) {
        Objects.requireNonNull(taskId);

        if (newOrder <= 0 || newOrder > tasks.size()) {
            throw new BusinessException(ErrorCode.INVALID_ASSESSMENT_TASK_ORDER);
        }

        int currentIndex = -1;

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            throw new BusinessException(ErrorCode.ASSESSMENT_TASK_NOT_FOUND);
        }

        AssessmentTask task = tasks.remove(currentIndex);

        tasks.add(newOrder - 1, task);

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).changeOrder(i + 1);
        }
    }

    public UUID getId() {
        return id;
    }

    public SkillDimension getSkillDimension() {
        return skillDimension;
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public List<AssessmentTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }
}
