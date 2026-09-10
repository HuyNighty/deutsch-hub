package com.deutschhub.domain.learning.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.course.model.entity.Lesson;
import com.deutschhub.domain.learning.course.model.entity.Section;
import com.deutschhub.domain.learning.course.model.valueobject.CEFRLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SectionTest {

    @Test
    void create_shouldCreateSectionWithValidData() {
        Section section = Section.create(
                "Grammar",
                "German grammar",
                1
        );

        assertNotNull(section.getId());
        assertEquals("Grammar", section.getTitle());
        assertEquals("German grammar", section.getDescription());
        assertEquals(1, section.getOrderIndex());
        assertTrue(section.getLessons().isEmpty());
        assertFalse(section.isDeleted());
    }

    @Test
    void create_shouldTrimTitle() {
        Section section = Section.create(
                "  Grammar  ",
                "Description",
                1
        );

        assertEquals("Grammar", section.getTitle());
    }

    @Test
    void create_shouldRejectNullTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Section.create(null, "Description", 1)
        );

        assertEquals(ErrorCode.SECTION_INVALID_TITLE, exception.getErrorCode());
    }

    @Test
    void create_shouldRejectBlankTitle() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Section.create("   ", "Description", 1)
        );

        assertEquals(ErrorCode.SECTION_INVALID_TITLE, exception.getErrorCode());
    }

    @Test
    void create_shouldRejectNegativeOrderIndex() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> Section.create("Grammar", "Description", -1)
        );

        assertEquals(ErrorCode.INVALID_SECTION_ORDER, exception.getErrorCode());
    }

    @Test
    void update_shouldUpdateTitleDescriptionAndOrderIndex() {
        Section section = Section.create(
                "Old title",
                "Old description",
                1
        );

        section.update(
                "New title",
                "New description",
                5
        );

        assertEquals("New title", section.getTitle());
        assertEquals("New description", section.getDescription());
        assertEquals(5, section.getOrderIndex());
    }

    @Test
    void update_shouldTrimIncomingValues() {
        Section section = Section.create(
                "Old title",
                "Old description",
                1
        );

        section.update(
                "  New title  ",
                "  New description  ",
                5
        );

        assertEquals("New title", section.getTitle());
        assertEquals("New description", section.getDescription());
    }

    @Test
    void update_shouldIgnoreNullFields() {
        Section section = Section.create(
                "Old title",
                "Old description",
                1
        );

        section.update(null, null, null);

        assertEquals("Old title", section.getTitle());
        assertEquals("Old description", section.getDescription());
        assertEquals(1, section.getOrderIndex());
    }

    @Test
    void update_shouldRejectBlankTitle() {
        Section section = Section.create(
                "Old title",
                "Old description",
                1
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> section.update("   ", null, null)
        );

        assertEquals(ErrorCode.SECTION_INVALID_TITLE, exception.getErrorCode());
    }

    @Test
    void update_shouldRejectNegativeOrderIndex() {
        Section section = Section.create(
                "Old title",
                "Old description",
                1
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> section.update(null, null, -1)
        );

        assertEquals(ErrorCode.INVALID_SECTION_ORDER, exception.getErrorCode());
    }

    @Test
    void changeOrderIndex_shouldUpdateOrderIndex() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        section.changeOrderIndex(5);

        assertEquals(5, section.getOrderIndex());
    }

    @Test
    void changeOrderIndex_shouldRejectNegativeOrderIndex() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> section.changeOrderIndex(-1)
        );

        assertEquals(ErrorCode.INVALID_SECTION_ORDER, exception.getErrorCode());
    }

    @Test
    void addLesson_shouldAddLesson() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        Lesson lesson = createLesson();

        section.addLesson(lesson);

        assertEquals(1, section.getLessons().size());
        assertSame(lesson, section.getLessons().get(0));
    }

    @Test
    void addLesson_shouldRejectNullLesson() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> section.addLesson(null)
        );

        assertEquals(ErrorCode.LESSON_INVALID_CONTENT, exception.getErrorCode());
    }

    @Test
    void restoreLesson_shouldIgnoreNullLesson() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        section.restoreLesson(null);

        assertTrue(section.getLessons().isEmpty());
    }

    @Test
    void softDelete_shouldMarkSectionAsDeleted() {
        Section section = Section.create(
                "Grammar",
                "Description",
                1
        );

        section.softDelete();

        assertTrue(section.isDeleted());
        assertNotNull(section.getDeletedAt());
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