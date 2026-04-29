package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.Section;
import com.deutschhub.domain.learning.model.valueobject.CEFRLevel;
import com.deutschhub.domain.learning.model.valueobject.Money;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Course implements Auditable, SoftDeletable {

    private final UUID id;
    private String title;
    private String description;
    private CEFRLevel level;
    private Money price;
    private boolean published = false;
    private UUID instructorId;
    private int estimatedHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private final List<Section> sections = new ArrayList<>();

    private Course(UUID id, String title, String description, CEFRLevel level, Money price, UUID instructorId) {
        this.id = id;
        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";
        this.level = validateLevel(level);
        this.price = validatePrice(price);
        this.instructorId = validateInstructorId(instructorId);
        this.estimatedHours = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Course create(String title, String description, CEFRLevel level,
                                Money price, UUID instructorId) {
        return new Course(UUID.randomUUID(), title, description, level, price, instructorId);
    }

    public void updateMetadata(String title, String description, CEFRLevel level, Money price, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }
        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";
        this.level = validateLevel(level);
        this.price = validatePrice(price);
        this.touch();
    }

    private UUID validateInstructorId(UUID instructorId) {
        if (instructorId == null) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_INSTRUCTOR);
        }
        return this.instructorId =  instructorId;
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_TITLE);
        }
        return title.trim();
    }

    private CEFRLevel validateLevel(CEFRLevel level) {
        if (level == null) {
            throw new BusinessException(ErrorCode.INVALID_CEFR_LEVEL);
        }
        return level;
    }

    private Money validatePrice(Money price) {
        if (price == null || price.isNegative()) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_PRICE);
        }
        return price;
    }

    public void publish() {
        ensureCanMutateBy(instructorId, false);
        publishInternal();
    }

    public void publish(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        publishInternal();
    }

    private void publishInternal() {
        if (sections.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_HAS_NO_SECTIONS);
        }
        if (published) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_PUBLISHED);
        }
        this.published = true;
        this.touch();
    }

    public void addSection(Section section) {
        ensureCanMutateBy(instructorId, false);
        addSectionInternal(section);
    }

    public void addSection(Section section, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        addSectionInternal(section);
    }

    private void addSectionInternal(Section section) {
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }
        if (section == null) {
            throw new BusinessException(ErrorCode.SECTION_NOT_FOUND);
        }
        ensureNotDeleted();
        this.sections.add(section);
        this.touch();
        recalculateEstimatedHours();
    }

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_DELETED);
        }
    }

    public void removeSection(UUID sectionId) {
        ensureCanMutateBy(instructorId, false);
        removeSectionInternal(sectionId);
    }

    public void removeSection(UUID sectionId, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        removeSectionInternal(sectionId);
    }

    private void removeSectionInternal(UUID sectionId) {
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }
        boolean removed = sections.removeIf(s -> s.getId().equals(sectionId));

        if (!removed) {
            throw new BusinessException(ErrorCode.SECTION_NOT_FOUND);
        }

        this.touch();
        recalculateEstimatedHours();
    }

    private void recalculateEstimatedHours() {
        int totalMinutes = sections.stream()
                .flatMap(section -> section.getLessons().stream())
                .mapToInt(Lesson::getEstimatedMinutes)
                .sum();

        this.estimatedHours = (int) Math.ceil(totalMinutes / 60.0);
    }


    @Override
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public void softDelete() {
        ensureCanMutateBy(instructorId, false);
        softDeleteInternal();
    }

    public void softDelete(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        softDeleteInternal();
    }

    private void softDeleteInternal() {
        this.deletedAt = LocalDateTime.now();
        this.touch();
    }

    private void ensureCanMutateBy(UUID actorId, boolean isAdmin) {
        ensureNotDeleted();
        if (isAdmin) {
            return;
        }
        if (!instructorId.equals(actorId)) {
            throw new BusinessException(ErrorCode.COURSE_FORBIDDEN_ACTION);
        }
    }


    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    @Override
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public CEFRLevel getLevel() {
        return level;
    }

    public Money getPrice() {
        return price;
    }

    public boolean isPublished() {
        return published;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public int getEstimatedHours() {
        return estimatedHours;
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }
}
