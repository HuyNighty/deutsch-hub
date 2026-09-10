package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.course.model.entity.Lesson;
import com.deutschhub.domain.learning.course.model.valueobject.CEFRLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LessonTest {

    @Test
    void create_shouldCreateLessonWithValidData() {
        Lesson lesson = createLesson();

        assertNotNull(lesson.getId());
        assertEquals("Nouns", lesson.getTitle());
        assertEquals("German nouns", lesson.getDescription());
        assertEquals(30, lesson.getEstimatedMinutes());
        assertEquals(new CEFRLevel("A1"), lesson.getLevel());
        assertEquals(1, lesson.getOrderIndex());
        assertFalse(lesson.isFreePreview());
        assertTrue(lesson.getItems().isEmpty());
        assertFalse(lesson.isDeleted());
    }

    @Test
    void create_shouldTrimTitle() {
        Lesson lesson = Lesson.create(
                "  Nouns  ",
                "German nouns",
                30,
                new CEFRLevel("A1"),
                1
        );

        assertEquals("Nouns", lesson.getTitle());
    }

    @Test
    void create_shouldRejectBlankTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Lesson.create(
                        "   ",
                        "Description",
                        30,
                        new CEFRLevel("A1"),
                        1
                )
        );

        assertEquals(ErrorCode.LESSON_INVALID_TITLE, exception.getErrorCode());
    }

    @Test
    void create_shouldRejectNullTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Lesson.create(
                        null,
                        "Description",
                        30,
                        new CEFRLevel("A1"),
                        1
                )
        );

        assertEquals(ErrorCode.LESSON_INVALID_TITLE, exception.getErrorCode());
    }

    @Test
    void create_shouldRejectInvalidDuration() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Lesson.create(
                        "Nouns",
                        "Description",
                        0,
                        new CEFRLevel("A1"),
                        1
                )
        );

        assertEquals(ErrorCode.LESSON_INVALID_DURATION, exception.getErrorCode());
    }

    @Test
    void create_shouldRejectNullLevel() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Lesson.create(
                        "Nouns",
                        "Description",
                        30,
                        null,
                        1
                )
        );

        assertEquals(ErrorCode.INVALID_CEFR_LEVEL, exception.getErrorCode());
    }

    @Test
    void update_shouldUpdateAllProvidedFields() {
        Lesson lesson = createLesson();

        lesson.update(
                "Verbs",
                "German verbs",
                45,
                new CEFRLevel("A2"),
                5,
                true
        );

        assertEquals("Verbs", lesson.getTitle());
        assertEquals("German verbs", lesson.getDescription());
        assertEquals(45, lesson.getEstimatedMinutes());
        assertEquals(new CEFRLevel("A2"), lesson.getLevel());
        assertEquals(5, lesson.getOrderIndex());
        assertTrue(lesson.isFreePreview());
    }

    @Test
    void update_shouldIgnoreNullFields() {
        Lesson lesson = createLesson();

        lesson.update(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals("Nouns", lesson.getTitle());
        assertEquals("German nouns", lesson.getDescription());
        assertEquals(30, lesson.getEstimatedMinutes());
        assertEquals(new CEFRLevel("A1"), lesson.getLevel());
        assertEquals(1, lesson.getOrderIndex());
        assertFalse(lesson.isFreePreview());
    }

    @Test
    void update_shouldTrimDescription() {
        Lesson lesson = createLesson();

        lesson.update(
                null,
                "  New description  ",
                null,
                null,
                null,
                null
        );

        assertEquals("New description", lesson.getDescription());
    }

    @Test
    void update_shouldRejectInvalidDuration() {
        Lesson lesson = createLesson();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> lesson.update(
                        null,
                        null,
                        0,
                        null,
                        null,
                        null
                )
        );

        assertEquals(ErrorCode.INVALID_LESSON_ESTIMATED_MINUTES, exception.getErrorCode());
    }

    @Test
    void update_shouldRejectNegativeOrderIndex() {
        Lesson lesson = createLesson();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> lesson.update(
                        null,
                        null,
                        null,
                        null,
                        -1,
                        null
                )
        );

        assertEquals(ErrorCode.INVALID_LESSON_ORDER, exception.getErrorCode());
    }

    @Test
    void changeOrderIndex_shouldUpdateOrderIndex() {
        Lesson lesson = createLesson();

        lesson.update(
                null,
                null,
                null,
                null,
                5,
                null
        );

        assertEquals(5, lesson.getOrderIndex());
    }

    @Test
    void addItem_shouldRejectNullItem() {
        Lesson lesson = createLesson();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> lesson.addItem(null)
        );

        assertEquals(ErrorCode.INVALID_LESSON, exception.getErrorCode());
    }

    @Test
    void softDelete_shouldMarkLessonAsDeleted() {
        Lesson lesson = createLesson();

        lesson.softDelete();

        assertTrue(lesson.isDeleted());
        assertNotNull(lesson.getDeletedAt());
    }

    private Lesson createLesson() {
        CEFRLevel level = new CEFRLevel("A1");
        return Lesson.create(
                "Nouns",
                "German nouns",
                30,
                level,
                1
        );
    }
}