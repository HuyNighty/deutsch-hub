package com.deutschhub.domain.learning.model.aggregate;

import com.deutschhub.common.domain.Auditable;
import com.deutschhub.common.domain.SoftDeletable;
import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.model.entity.Lesson;
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

    private final List<Lesson> lessons = new ArrayList<>();

    private Course(UUID id, String title, String description, CEFRLevel level, Money price, UUID instructorId) {
        this.id = id;
        this.title = validateTitle(title);
        this.description = description != null ? description.trim() : "";
        this.level = validateLevel(level);
        this.price = validatePrice(price);
        this.instructorId = instructorId;
        this.estimatedHours = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Course create(String title, String description, CEFRLevel level,
                                Money price, UUID instructorId) {
        return new Course(UUID.randomUUID(), title, description, level, price, instructorId);
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_COURSE_TITLE);
        }
        return title;
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
        if (lessons.isEmpty()) {
            throw new BusinessException(ErrorCode.COURSE_HAS_NO_LESSONS);
        }
        if (published) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_PUBLISHED);
        }
        this.published = true;
        this.touch();
    }

    public void addLesson(Lesson lesson) {
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }
        if (lesson == null) {
            throw new BusinessException(ErrorCode.INVALID_LESSON);
        }
        this.lessons.add(lesson);
        this.touch();
        this.estimatedHours += lesson.getEstimatedMinutes() / 60;
    }

    public void removeLesson(UUID lessonId) {
        if (published) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_PUBLISHED_COURSE);
        }
        lessons.removeIf(l -> l.getId().equals(lessonId));
        this.touch();
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
        this.deletedAt = LocalDateTime.now();
        this.touch();
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

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

}
