package com.deutschhub.domain.learning.assessment.model.aggregate;

import com.deutschhub.domain.learning.assessment.model.entity.AssessmentComponent;
import com.deutschhub.domain.learning.assessment.model.enums.AssessmentStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public List<AssessmentComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }
}
