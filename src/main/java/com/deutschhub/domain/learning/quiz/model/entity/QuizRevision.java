package com.deutschhub.domain.learning.quiz.model.entity;

import com.deutschhub.common.exception.BusinessException;
import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.domain.learning.quiz.model.enums.CompletionPolicy;
import com.deutschhub.domain.learning.quiz.model.enums.DifficultyLevel;
import com.deutschhub.domain.learning.quiz.model.enums.QuizRevisionStatus;
import com.deutschhub.domain.learning.quiz.model.valueobject.AvailabilityWindow;
import com.deutschhub.domain.learning.quiz.model.valueobject.ReviewFeedback;
import com.deutschhub.domain.shared.valueobject.UserId;

import java.time.Instant;
import java.util.*;

public class QuizRevision {

    private final UUID id;
    private final int revisionNumber;

    private QuizRevisionStatus status;

    private String title;
    private String description;
    private DifficultyLevel difficulty;
    private Availability availability;
    private CompletionPolicy completionPolicy;

    private Integer timeLimitMinutes;
    private Integer passingPercentage;
    private Integer maxAttempts;

    private final List<Question> questions = new ArrayList<>();
    private final List<ReviewCycle> reviewCycles = new ArrayList<>();
    private final List<LearningPrerequisite> learningPrerequisites = new ArrayList<>();

    private QuizRevision(UUID id, int revisionNumber, String title, String description, DifficultyLevel difficulty,
                         Integer timeLimitMinutes, Integer passingPercentage, Integer maxAttempts) {
        this.id = Objects.requireNonNull(id);
        this.revisionNumber = revisionNumber;

        this.status = QuizRevisionStatus.DRAFT;

        this.title = title;
        this.description = description;
        this.difficulty = difficulty;

        this.timeLimitMinutes = timeLimitMinutes;
        this.passingPercentage = passingPercentage;
        this.maxAttempts = maxAttempts;
        this.availability = Availability.create();
    }

    public static QuizRevision createDraft(int revisionNumber) {
        return new QuizRevision(UUID.randomUUID(), revisionNumber, null, null, null,
                null, null, null);
    }

    public int getMaxScore() {
        return questions.stream().mapToInt(Question::getScore).sum();
    }

    public void addQuestion(Question question) {
        ensureEditable();

        if (question == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        questions.add(question);
    }

    public void removeQuestion(UUID questionId) {
        ensureEditable();

        if (questionId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUESTION);
        }

        boolean removed = questions.removeIf(question -> question.getId().equals(questionId));

        if (!removed) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
    }

    public void submitForReview(UserId submittedBy, Instant submittedAt) {
        if (status != QuizRevisionStatus.DRAFT) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        validate();

        if (submittedBy == null || submittedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_REVIEW_CYCLE_DATA);
        }

        reviewCycles.add(new ReviewCycle(UUID.randomUUID(), submittedBy, submittedAt));

        this.status = QuizRevisionStatus.IN_REVIEW;
    }

    public void withdrawSubmission(UserId withdrawnBy, Instant withdrawnAt) {
        if (status != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        getCurrentReviewCycle().markWithdrawn(withdrawnBy, withdrawnAt);

        this.status = QuizRevisionStatus.DRAFT;
    }

    public void publish(UserId reviewer, Instant reviewedAt) {
        if (status != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        validate();

        getCurrentReviewCycle().markApproved(reviewer, reviewedAt);

        this.status = QuizRevisionStatus.PUBLISHED;
    }

    public void markHistorical() {
        if (status != QuizRevisionStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        this.status = QuizRevisionStatus.HISTORICAL;
    }

    public void requestChanges(UserId reviewer, ReviewFeedback feedback, Instant reviewedAt) {
        if (status != QuizRevisionStatus.IN_REVIEW) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }

        getCurrentReviewCycle().markChangesRequested(reviewer, feedback, reviewedAt);

        this.status = QuizRevisionStatus.DRAFT;
    }

    public void updateTitle(String title) {
        ensureEditable();
        this.title = validateTitle(title);
    }

    public void updateDescription(String description) {
        ensureEditable();
        this.description = description != null ? description.trim() : "";
    }

    public void updateDifficulty(DifficultyLevel difficulty) {
        ensureEditable();

        if (difficulty == null) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_DIFFICULTY);
        }

        this.difficulty = difficulty;
    }

    public void updateTimeLimit(int minutes) {
        ensureEditable();
        this.timeLimitMinutes = validateTimeLimit(minutes);
    }

    public void updatePassingPercentage(int percentage) {
        ensureEditable();
        this.passingPercentage = validatePassingPercentage(percentage);
    }

    public void updateMaxAttempts(int maxAttempts) {
        ensureEditable();
        this.maxAttempts = validateMaxAttempts(maxAttempts);
    }

    public void activateAvailability() {
        ensureEditable();
        availability.activate();
    }

    public void deactivateAvailability() {
        ensureEditable();
        availability.deactivate();
    }

    public void setAvailabilityWindow(AvailabilityWindow window) {
        ensureEditable();
        availability.setWindow(window);
    }

    public void clearAvailabilityWindow() {
        ensureEditable();
        availability.clearWindow();
    }

    public boolean isAvailableAt(Instant now) {
        return availability.isAvailableAt(now);
    }

    public void updateCompletionPolicy(CompletionPolicy completionPolicy) {
        ensureEditable();

        if (completionPolicy == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_COMPLETION_POLICY);
        }

        this.completionPolicy = completionPolicy;
    }

    public void addLearningPrerequisite(LearningPrerequisite prerequisite) {
        ensureEditable();

        if (prerequisite == null) {throw new BusinessException(
                ErrorCode.INVALID_QUIZ_LEARNING_PREREQUISITE_DATA);
        }

        learningPrerequisites.add(prerequisite);
    }

    public void removeLearningPrerequisite(UUID prerequisiteId) {
        ensureEditable();

        if (prerequisiteId == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_LEARNING_PREREQUISITE_DATA);
        }

        boolean removed = learningPrerequisites.removeIf(prerequisite -> prerequisite.getId().equals(prerequisiteId));

        if (!removed) {
            throw new BusinessException(ErrorCode.QUIZ_LEARNING_PREREQUISITE_NOT_FOUND);
        }
    }

    public boolean isPublished() {
        return status == QuizRevisionStatus.PUBLISHED;
    }

    private void ensureEditable() {
        if (status != QuizRevisionStatus.DRAFT) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_INVALID_STATUS);
        }
    }

    private ReviewCycle getCurrentReviewCycle() {
        if (reviewCycles.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_REVIEW_INVALID_STATE);
        }

        return reviewCycles.get(reviewCycles.size() - 1);
    }

    private void validate() {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_TITLE);
        }

        if (difficulty == null) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_DIFFICULTY);
        }

        if (passingPercentage == null) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_PASSING_SCORE);
        }

        if (maxAttempts == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_MAX_ATTEMPTS);
        }

        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_REVISION_HAS_NO_QUESTIONS);
        }

        if (completionPolicy == null) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_COMPLETION_POLICY);
        }

        if (timeLimitMinutes != null) {
            validateTimeLimit(timeLimitMinutes);
        }

        validatePassingPercentage(passingPercentage);
        validateMaxAttempts(maxAttempts);

        questions.forEach(Question::validate);
    }

    private String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_TITLE);
        }

        return title.trim();
    }

    private int validateTimeLimit(int minutes) {
        if (minutes <= 0) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_TIME_LIMIT);
        }

        return minutes;
    }

    private int validatePassingPercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new BusinessException(ErrorCode.QUIZ_INVALID_PASSING_SCORE);
        }

        return percentage;
    }

    private int validateMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUIZ_MAX_ATTEMPTS);
        }

        return maxAttempts;
    }

    public UUID getId() {
        return id;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public QuizRevisionStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public Integer getPassingPercentage() {
        return passingPercentage;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public List<ReviewCycle> getReviewCycles() {
        return Collections.unmodifiableList(reviewCycles);
    }

    public Availability getAvailability() {
        return availability;
    }

    public CompletionPolicy getCompletionPolicy() {
        return completionPolicy;
    }

    public List<LearningPrerequisite> getLearningPrerequisites() {
        return Collections.unmodifiableList(learningPrerequisites);
    }
}