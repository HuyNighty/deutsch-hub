package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.Lesson;
import com.deutschhub.domain.learning.model.entity.LessonItem;
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

    public static Course restore( UUID id, String title, String description, CEFRLevel level, Money price,
                                  boolean published, UUID instructorId, int estimatedHours, LocalDateTime createdAt, LocalDateTime updatedAt,
                                  LocalDateTime deletedAt) {
        Course course = new Course(id, title, description, level, price, instructorId);
        course.published = published;
        course.estimatedHours = estimatedHours;
        course.createdAt = createdAt;
        course.updatedAt = updatedAt;
        course.deletedAt = deletedAt;

        return course;
    }

    public void restoreSection(Section section) {
        if (section == null) {
            return;
        }

        this.sections.add(section);
    }

    public void publish(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        ensureNotDeleted();

        if (published) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_PUBLISHED);
        }

        boolean hasActiveSection = sections.stream()
                .anyMatch(section -> !section.isDeleted());

        if (!hasActiveSection) {
            throw new BusinessException(ErrorCode.COURSE_MUST_HAVE_SECTION_BEFORE_PUBLISH);
        }

        boolean hasActiveLesson = sections.stream()
                .filter(section -> !section.isDeleted())
                .flatMap(section -> section.getLessons().stream())
                .anyMatch(lesson -> !lesson.isDeleted());

        if (!hasActiveLesson) {
            throw new BusinessException(ErrorCode.COURSE_MUST_HAVE_LESSON_BEFORE_PUBLISH);
        }

        this.published = true;
        touch();
    }

    public void unpublish(UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        ensureNotDeleted();

        if (!published) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }

        this.published = false;
        touch();
    }

    public void addSection(Section section, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        addSectionInternal(section);
    }

    public Lesson addLessonToSection(UUID sectionId, Lesson lesson, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        if (lesson == null) {
            throw new BusinessException(ErrorCode.LESSON_NOT_FOUND);
        }

        Section section = sections.stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        section.addLesson(lesson);

        touch();
        recalculateEstimatedHours();
        return lesson;
    }

    public Section updateSection(UUID sectionId, String title, String description,
                                 Integer orderIndex, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        Section section = sections.stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        section.update(title, description, orderIndex);

        touch();

        recalculateEstimatedHours();

        return section;
    }

    public Lesson updateLesson(UUID sectionId, UUID lessonId, String title, String description,
                               Integer estimatedMinutes, CEFRLevel level, Integer orderIndex, Boolean freePreview,
                               UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        Section section = sections.stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        Lesson lesson = section.getLessons()
                .stream()
                .filter(item -> item.getId().equals(lessonId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        lesson.update(title, description, estimatedMinutes, level, orderIndex, freePreview);

        touch();
        recalculateEstimatedHours();
        return lesson;
    }

    public void deleteSection(UUID sectionId, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        Section section = sections.stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        section.softDelete();
        touch();
        recalculateEstimatedHours();
    }

    public Lesson deleteLesson(UUID sectionId, UUID lessonId, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        Section section = sections.stream()
                .filter(item -> item.getId().equals(sectionId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        Lesson lesson = section.getLessons()
                .stream()
                .filter(item -> item.getId().equals(lessonId))
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        lesson.softDelete();

        touch();
        recalculateEstimatedHours();
        return lesson;
    }

    public Lesson addLessonItemToLesson(UUID sectionId, UUID lessonId, LessonItem item, UUID actorId, boolean isAdmin) {
        ensureCanMutateBy(actorId, isAdmin);

        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }

        ensureNotDeleted();

        if (item == null) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }

        Section section = sections.stream()
                .filter(currentSection -> currentSection.getId().equals(sectionId))
                .filter(currentSection -> !currentSection.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SECTION_NOT_FOUND));

        Lesson lesson = section.getLessons()
                .stream()
                .filter(currentLesson -> currentLesson.getId().equals(lessonId))
                .filter(currentLesson -> !currentLesson.isDeleted())
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LESSON_NOT_FOUND));

        lesson.addItem(item);
        touch();
        recalculateEstimatedHours();

        return lesson;
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

    private void ensureNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_DELETED);
        }
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
