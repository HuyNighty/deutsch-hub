package com.deutschhub.domain.learning.quiz.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizRevisionTest {

    @Test
    void shouldCreateDraftRevision() {
        QuizRevision revision = QuizRevision.createDraft(1);

        assertNotNull(revision.getId());
        assertEquals(1, revision.getRevisionNumber());
        assertEquals(QuizRevisionStatus.DRAFT, revision.getStatus());

        assertNull(revision.getTitle());
        assertNull(revision.getDescription());
        assertNull(revision.getDifficulty());
        assertNull(revision.getTimeLimitMinutes());
        assertNull(revision.getPassingPercentage());
        assertNull(revision.getMaxAttempts());
        assertNull(revision.getCompletionPolicy());

        assertTrue(revision.getQuestions().isEmpty());
        assertTrue(revision.getReviewCycles().isEmpty());
        assertTrue(revision.getLearningPrerequisites().isEmpty());

        assertNotNull(revision.getAvailability());
    }

    @Test
    void shouldAddQuestionToDraftRevision() {
        QuizRevision revision = QuizRevision.createDraft(1);
        Question question = Question.create(
                "What is the correct answer?",
                10,
                null
        );

        revision.addQuestion(question);

        assertEquals(1, revision.getQuestions().size());
        assertSame(question, revision.getQuestions().get(0));
    }

    @Test
    void shouldRejectAddingNullQuestion() {
        QuizRevision revision = QuizRevision.createDraft(1);

        assertThrows(
                BusinessException.class,
                () -> revision.addQuestion(null)
        );
    }

    @Test
    void shouldRemoveExistingQuestion() {
        QuizRevision revision = QuizRevision.createDraft(1);
        Question question = Question.create(
                "What is the correct answer?",
                10,
                null
        );

        revision.addQuestion(question);

        revision.removeQuestion(question.getId());

        assertTrue(revision.getQuestions().isEmpty());
    }

    @Test
    void shouldRejectRemovingQuestionWithNullId() {
        QuizRevision revision = QuizRevision.createDraft(1);

        assertThrows(
                BusinessException.class,
                () -> revision.removeQuestion(null)
        );
    }

    @Test
    void shouldRejectRemovingNonExistingQuestion() {
        QuizRevision revision = QuizRevision.createDraft(1);

        assertThrows(
                BusinessException.class,
                () -> revision.removeQuestion(UUID.randomUUID())
        );
    }

    @Test
    void shouldCalculateMaxScoreFromQuestions() {
        QuizRevision revision = QuizRevision.createDraft(1);

        Question question1 = Question.create(
                "Question 1",
                10,
                null
        );

        Question question2 = Question.create(
                "Question 2",
                20,
                null
        );

        revision.addQuestion(question1);
        revision.addQuestion(question2);

        assertEquals(30, revision.getMaxScore());
    }

    @Test
    void shouldReturnZeroMaxScoreWhenThereAreNoQuestions() {
        QuizRevision revision = QuizRevision.createDraft(1);

        assertEquals(0, revision.getMaxScore());
    }

    @Test
    void shouldNotAllowDirectModificationOfQuestionsCollection() {
        QuizRevision revision = QuizRevision.createDraft(1);
        Question question = Question.create(
                "Question",
                10,
                null
        );

        revision.addQuestion(question);

        assertThrows(
                UnsupportedOperationException.class,
                () -> revision.getQuestions().clear()
        );

        assertEquals(1, revision.getQuestions().size());
    }
}