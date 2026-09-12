package com.deutschhub.domain.learning.quiz.model.aggregate;

import com.deutschhub.domain.learning.quiz.model.entity.QuizRevision;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;
import com.deutschhub.domain.learning.quiz.model.enums.QuizStatus;
import com.deutschhub.domain.learning.quiz.model.enums.QuizVisibility;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QuizTest {

    @Test
    void shouldCreateDraftQuizWithDefaultState() {
        UUID createdBy = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(createdBy);

        assertNotNull(quiz);
        assertEquals(createdBy, quiz.getCreatedBy());
        assertEquals(createdBy, quiz.getAuthor());
        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
        assertEquals(QuizVisibility.PRIVATE, quiz.getVisibility());

        assertEquals(1, quiz.getRevisions().size());

        QuizRevision revision = quiz.getRevisions().get(0);

        assertEquals(1, revision.getRevisionNumber());
        assertEquals(QuizRevisionStatus.DRAFT, revision.getStatus());
    }

    @Test
    void shouldActivateDraftQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());

        quiz.activate();

        assertEquals(QuizStatus.ACTIVE, quiz.getStatus());
    }

    @Test
    void shouldDeactivateActiveQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());
        quiz.activate();

        quiz.deactivate();

        assertEquals(QuizStatus.DRAFT, quiz.getStatus());
    }

    @Test
    void shouldArchiveActiveQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());
        quiz.activate();

        quiz.archive();

        assertEquals(QuizStatus.ARCHIVED, quiz.getStatus());
    }

    @Test
    void shouldReactivateArchivedQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());
        quiz.activate();
        quiz.archive();

        quiz.activate();

        assertEquals(QuizStatus.ACTIVE, quiz.getStatus());
    }

    @Test
    void shouldRejectActivatingActiveQuiz() {

        Quiz quiz = Quiz.createDraft(UUID.randomUUID());
        quiz.activate();

        assertThrows(
                RuntimeException.class,
                quiz::activate
        );
    }

    @Test
    void shouldRejectDeactivatingDraftQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());

        assertThrows(
                RuntimeException.class,
                quiz::deactivate
        );
    }

    @Test
    void shouldRejectArchivingDraftQuiz() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());

        assertThrows(
                RuntimeException.class,
                quiz::archive
        );
    }

    @Test
    void shouldDiscardDraftRevision() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());

        QuizRevision revision = quiz.getRevisions().get(0);

        quiz.discardDraftRevision(revision.getId());

        assertTrue(quiz.getRevisions().isEmpty());
    }
//
//    @Test
//    void shouldCreateNewRevisionWhenNoDraftRevisionExists() {
//        Quiz quiz = Quiz.createDraft(UUID.randomUUID());
//
//        QuizRevision firstRevision = quiz.getRevisions().get(0);
//        quiz.discardDraftRevision(firstRevision.getId());
//
//        QuizRevision secondRevision = quiz.createRevision();
//
//        assertEquals(2, secondRevision.getRevisionNumber());
//        assertEquals(QuizRevisionStatus.DRAFT, secondRevision.getStatus());
//        assertEquals(1, quiz.getRevisions().size());
//    }

    @Test
    void shouldRejectCreatingRevisionWhenDraftRevisionAlreadyExists() {
        Quiz quiz = Quiz.createDraft(UUID.randomUUID());

        assertThrows(
                RuntimeException.class,
                quiz::createRevision
        );
    }

    @Test
    void shouldChangeAuthorByCurrentAuthor() {
        UUID authorId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        quiz.changeAuthor(authorId, newAuthorId, false);

        assertEquals(newAuthorId, quiz.getAuthor());
    }

    @Test
    void shouldChangeAuthorByAdmin() {
        UUID authorId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        quiz.changeAuthor(adminId, newAuthorId, true);

        assertEquals(newAuthorId, quiz.getAuthor());
    }

    @Test
    void shouldRejectChangingAuthorByUnauthorizedUser() {
        UUID authorId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        assertThrows(
                RuntimeException.class,
                () -> quiz.changeAuthor(
                        unauthorizedUserId,
                        newAuthorId,
                        false
                )
        );
    }

    @Test
    void shouldChangeVisibilityByAuthorizedUser() {
        UUID authorId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        quiz.changeVisibility(
                QuizVisibility.PUBLIC,
                authorId,
                false
        );

        assertEquals(QuizVisibility.PUBLIC, quiz.getVisibility());
    }

    @Test
    void shouldChangeVisibilityByAdmin() {
        UUID authorId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        quiz.changeVisibility(
                QuizVisibility.COURSE_ONLY,
                adminId,
                true
        );

        assertEquals(
                QuizVisibility.COURSE_ONLY,
                quiz.getVisibility()
        );
    }

    @Test
    void shouldRejectChangingVisibilityByUnauthorizedUser() {
        UUID authorId = UUID.randomUUID();
        UUID unauthorizedUserId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(authorId);
        quiz.activate();

        assertThrows(
                RuntimeException.class,
                () -> quiz.changeVisibility(
                        QuizVisibility.PUBLIC,
                        unauthorizedUserId,
                        false
                )
        );
    }

    @Test
    void shouldRejectActivatingDeletedQuiz() {
        UUID createdBy = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(createdBy);
        quiz.softDelete();

        assertThrows(
                RuntimeException.class,
                quiz::activate
        );
    }

    @Test
    void shouldRejectChangingAuthorOfDeletedQuiz() {
        UUID createdBy = UUID.randomUUID();
        UUID newAuthorId = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(createdBy);
        quiz.softDelete();

        assertThrows(
                RuntimeException.class,
                () -> quiz.changeAuthor(
                        createdBy,
                        newAuthorId,
                        false
                )
        );
    }

    @Test
    void shouldRejectChangingVisibilityOfDeletedQuiz() {
        UUID createdBy = UUID.randomUUID();

        Quiz quiz = Quiz.createDraft(createdBy);
        quiz.softDelete();

        assertThrows(
                RuntimeException.class,
                () -> quiz.changeVisibility(
                        QuizVisibility.PUBLIC,
                        createdBy,
                        false
                )
        );
    }
}