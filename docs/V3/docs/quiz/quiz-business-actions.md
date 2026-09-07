# Quiz Business Actions

## 1. Purpose

This document defines the business actions available within the Quiz domain.

A Business Action describes a meaningful operation performed by an actor or triggered by a business condition.

This document focuses on:

- who performs the action;
- what business intent the action represents;
- the required conditions;
- the main business outcome.

It does not define REST endpoints, application service classes, persistence operations, or other implementation details.

---

## 2. Business Actors

The current Quiz business actions involve the following actors:

### 2.1 Quiz Manager

A Quiz Manager is responsible for managing Quiz definitions and their Revisions.

The exact authorization mapping between system roles and Quiz Manager remains an application authorization concern.

The current source defines:

```text
src/main/java/com/deutschhub/domain/identity/model/enums/RoleType.java
````

with:

```text
USER
ADMIN
CONTENT_EDITOR
```

No `INSTRUCTOR` role currently exists in the source.

Therefore, this document does not assume a new `INSTRUCTOR` role.

---

### 2.2 Learner

A Learner is the User who:

* accesses an available Quiz;
* creates a QuizAttempt;
* answers Questions;
* changes or clears answers;
* resumes an active Attempt;
* submits an Attempt;
* cancels an Attempt;
* retries a Quiz when eligible.

---

### 2.3 System

The System may trigger business behavior based on time or other domain conditions.

The most important example is Attempt expiration.

The System is not treated as a human actor.

---

# 3. Quiz Management Actions

## 3.1 Create Quiz

### Actor

Quiz Manager.

### Intent

Create a new Quiz identity with an initial Draft Revision.

### Preconditions

The actor must be authorized to create a Quiz.

### Main Flow

```text id="qba-create-quiz"
Create Quiz
    ↓
create Quiz identity
    ↓
create initial Draft Revision
```

### Result

A new Quiz exists with:

* a unique Quiz identifier;
* a Draft Revision;
* no Published Revision yet.

A newly created Quiz is not immediately available for learner assessment.

---

## 3.2 Edit Draft Revision

### Actor

Quiz Manager.

### Intent

Modify the assessment definition before publication.

### Preconditions

* A Draft Revision exists.
* The actor is authorized.
* The Revision is still editable.

### Main Flow

The Quiz Manager may modify the Draft Revision's assessment definition, including:

* title;
* description;
* time limit;
* passing score;
* maximum attempts;
* completion policy;
* difficulty;
* Questions;
* Answer definitions.

### Result

The Draft Revision is updated.

The current Published Revision, if any, remains unchanged.

---

## 3.3 Add Question

### Actor

Quiz Manager.

### Intent

Add a Question to a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question satisfies the Question domain constraints.

### Result

The Question becomes part of the Draft Revision.

A Question cannot be added directly to a Published Revision.

---

## 3.4 Update Question

### Actor

Quiz Manager.

### Intent

Modify a Question within a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The resulting Question remains valid.

### Result

The Question definition is updated.

A Published Revision remains unchanged.

---

## 3.5 Remove Question

### Actor

Quiz Manager.

### Intent

Remove a Question from a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.

### Result

The Question is removed from the Draft Revision.

The Revision must still satisfy all publication requirements before it can be published.

---

## 3.6 Submit Draft Revision for Review

### Actor

Quiz Manager.

### Intent

Indicate that a Draft Revision is complete enough to be considered for publication.

### Preconditions

The Draft Revision must satisfy the required submission conditions.

At minimum:

* title must be provided;
* required assessment information must be valid.

A Draft Revision may still be incomplete before submission.

### Result

The Revision becomes eligible for the applicable review/publication process.

The exact review workflow is not currently defined.

---

## 3.7 Publish Revision

### Actor

Quiz Manager.

### Intent

Make a valid Draft Revision the current Published Revision.

### Preconditions

The Draft Revision must:

* contain at least one Question;
* contain only valid Questions;
* satisfy score consistency;
* satisfy all required publication conditions.

```text id="qba-publish-revision"
sum(Question.score)
=
QuizRevision.maxScore
```

### Main Flow

```text
Draft Revision
      ↓
validate
      ↓
publish
      ↓
Current Published Revision
```

If another Published Revision exists:

```text
Previous Published Revision
          ↓
      historical

New Draft Revision
          ↓
      Published
```

### Result

The new Revision becomes the current Published Revision.

Existing QuizAttempts remain associated with their original Revision.

---

## 3.8 Discard Draft Revision

### Actor

Quiz Manager.

### Intent

Remove an unpublished Draft Revision without affecting the Published Revision.

### Preconditions

* A Draft Revision exists.
* The actor is authorized.

### Result

The Draft Revision is discarded.

The current Published Revision remains unchanged.

---

## 3.9 Change Quiz Visibility

### Actor

Quiz Manager.

### Intent

Change who may access or discover the Quiz.

### Supported Values

```text id="qba-visibility"
PRIVATE
COURSE_ONLY
PUBLIC
```

### Result

The Quiz visibility changes.

Visibility does not modify:

* Revision lifecycle;
* existing Attempts;
* historical results.

---

## 3.10 Activate Quiz

### Actor

Quiz Manager.

### Intent

Allow eligible learners to create new Attempts.

### Preconditions

The Quiz must satisfy the conditions required for activation.

### Result

The Quiz becomes active for new Attempt creation.

---

## 3.11 Deactivate Quiz

### Actor

Quiz Manager.

### Intent

Prevent new Attempts from being created.

### Result

The Quiz becomes unavailable for new Attempt creation.

Existing `IN_PROGRESS` Attempts are not automatically cancelled or expired.

---

# 4. Learner Access Actions

## 4.1 Access Quiz

### Actor

Learner.

### Intent

Open a Quiz for which the User may have access.

### Preconditions

The applicable access rules must be satisfied.

Conceptually:

```text id="qba-access"
Visibility
    ↓
Access eligibility
    ↓
Quiz accessible
```

For `COURSE_ONLY`, valid Course learning access is required.

For `PRIVATE`, ordinary learners do not have access.

The exact authorization implementation is outside the Quiz aggregate.

---

## 4.2 Check Attempt Eligibility

### Actor

Learner / System.

### Intent

Determine whether a new QuizAttempt may be created.

### Preconditions

The applicable eligibility rules are evaluated.

Conceptually:

```text id="qba-eligibility"
Quiz accessible
      ↓
Published Revision exists
      ↓
Quiz active
      ↓
Prerequisite / unlock satisfied
      ↓
No IN_PROGRESS Attempt
      ↓
Attempt quota available
```

The exact prerequisite or unlock rules are not yet defined.

### Result

The User is either:

* eligible to create an Attempt; or
* rejected with the applicable business reason.

---

# 5. QuizAttempt Actions

## 5.1 Start QuizAttempt

### Actor

Learner.

### Intent

Create a new learner-specific execution of a Quiz.

### Preconditions

All required eligibility conditions must be satisfied:

1. Quiz is accessible.
2. A Published Revision exists.
3. Quiz is active.
4. Required prerequisite/unlock conditions are satisfied.
5. User has no existing `IN_PROGRESS` Attempt for the Quiz.
6. Attempt quota is available.

### Main Flow

```text id="qba-start-attempt"
Check eligibility
      ↓
Create QuizAttempt
      ↓
Bind exact Published Revision
      ↓
Consume attempt quota
      ↓
IN_PROGRESS
```

### Result

A new QuizAttempt is created.

The Attempt is permanently associated with the exact Published Revision used at creation.

One attempt quota is consumed immediately after successful creation.

---

## 5.2 Resume QuizAttempt

### Actor

Learner.

### Intent

Continue an existing active Attempt.

### Preconditions

* Attempt exists.
* Attempt belongs to the User.
* Attempt status is `IN_PROGRESS`.
* Attempt has not reached its deadline.

### Result

The User continues the same QuizAttempt.

No new Attempt is created.

No additional quota is consumed.

---

## 5.3 Answer Question

### Actor

Learner.

### Intent

Record the current answer to a Question.

### Preconditions

* Attempt belongs to the User.
* Attempt is `IN_PROGRESS`.
* Question belongs to the Attempt's Revision.
* Submitted answer is valid for the Question.

### Result

The current `UserAnswer` for that Question is created or replaced.

Only the current answer is retained.

---

## 5.4 Change Answer

### Actor

Learner.

### Intent

Change an existing answer before the Attempt ends.

### Preconditions

* Attempt is `IN_PROGRESS`.
* Question belongs to the Attempt's Revision.

### Result

The current `UserAnswer` is replaced by the new valid response.

No answer history is created.

---

## 5.5 Clear Answer

### Actor

Learner.

### Intent

Remove the current answer from a Question.

### Preconditions

* Attempt is `IN_PROGRESS`.
* Question belongs to the Attempt's Revision.

### Result

The Question returns to an unanswered state.

No `SKIPPED` state is created.

---

## 5.6 Navigate / Skip Question

### Actor

Learner.

### Intent

Move between Questions without answering the current Question.

### Result

No assessment answer state is created.

The Question remains unanswered.

Skipping is navigation behavior rather than a persisted domain state.

---

## 5.7 Submit QuizAttempt

### Actor

Learner.

### Intent

Finish the QuizAttempt manually.

### Preconditions

The Attempt must be `IN_PROGRESS`.

The Completion Policy determines whether unanswered Questions are allowed.

### REQUIRED_ALL

All Questions must be answered.

```text
All Questions answered
        ↓
submission allowed
```

### OPTIONAL

Unanswered Questions are allowed.

```text
Some Questions unanswered
        ↓
submission still allowed
```

### Main Flow

```text id="qba-submit"
IN_PROGRESS
    ↓
validate Completion Policy
    ↓
evaluate Questions
    ↓
create QuestionResults
    ↓
calculate totalScore
    ↓
calculate AssessmentResult when applicable
    ↓
SUBMITTED
```

### Result

The Attempt becomes `SUBMITTED`.

Question Results are finalized.

The final `totalScore` is recorded.

If a passing score exists, the Assessment Result becomes either:

```text
PASSED
```

or:

```text
FAILED
```

---

## 5.8 Cancel QuizAttempt

### Actor

Learner.

### Intent

Intentionally stop an active Attempt without submitting it.

### Preconditions

* Attempt belongs to the User.
* Attempt is `IN_PROGRESS`.

### Main Flow

```text id="qba-cancel"
IN_PROGRESS
      ↓
CANCEL
      ↓
CANCELLED
```

### Result

The Attempt becomes `CANCELLED`.

The Attempt:

* cannot be resumed;
* does not produce an Assessment Result;
* does not become a completed assessment;
* does not return consumed quota.

The User does not need to provide a cancellation reason.

The frontend should confirm the cancellation intent before executing the action.

---

## 5.9 Expire QuizAttempt

### Actor

System.

### Intent

Terminate an Attempt whose time limit has elapsed.

### Preconditions

* Attempt is `IN_PROGRESS`.
* Current server time has reached or passed the Attempt deadline.

### Main Flow

```text id="qba-expire"
IN_PROGRESS
      ↓
deadline reached
      ↓
evaluate submitted answers
      ↓
unanswered Questions → 0
      ↓
create QuestionResults
      ↓
calculate totalScore
      ↓
calculate AssessmentResult when applicable
      ↓
EXPIRED
```

### Result

The Attempt becomes `EXPIRED`.

The Attempt cannot be resumed.

If a passing score exists, the final Assessment Result is calculated.

If no passing score exists, no Assessment Result is produced.

---

## 5.10 Retry Quiz

### Actor

Learner.

### Intent

Start another Attempt after a previous Attempt has ended.

### Preconditions

The User must:

* be eligible to access the Quiz;
* have a Published Revision available;
* satisfy the applicable retry policy;
* have remaining attempt quota;
* have no existing `IN_PROGRESS` Attempt.

### Result

A new QuizAttempt is created.

The new Attempt receives its own:

* Attempt identifier;
* Revision binding;
* answers;
* lifecycle;
* results;
* score.

A previous Attempt is never reopened.

A new quota is consumed when the new Attempt is created.

---

# 6. Assessment Result Actions

## 6.1 Evaluate Attempt

### Actor

System / Domain.

### Intent

Determine the assessment evidence and final score when an Attempt reaches an assessment-ending state.

### Trigger

The Attempt reaches:

```text
SUBMITTED
```

or:

```text
EXPIRED
```

### Main Flow

For every Question:

```text id="qba-evaluate"
Current UserAnswer
       ↓
evaluate against Revision definition
       ↓
QuestionResult
```

For unanswered Questions:

```text
UNANSWERED
+
earnedScore = 0
```

Then:

```text
QuestionResults
      ↓
sum earnedScore
      ↓
totalScore
```

If a passing score exists:

```text
totalScore >= passingScore
        ↓
PASSED

totalScore < passingScore
        ↓
FAILED
```

### Result

The Attempt contains its historical assessment outcome.

---

## 6.2 Calculate Best Score

### Actor

System / Query capability.

### Intent

Determine the highest historical score for a User and Published Quiz Revision.

### Input

Historical QuizAttempts for:

```text id="qba-best-score"
User × Published Quiz Revision
```

### Calculation

```text
Best Score
=
MAX(totalScore)
```

### Result

The highest score is returned as a derived value.

Best Score is not written back into an individual QuizAttempt.

---

# 7. Revision Change Actions During Active Attempts

## 7.1 Publish New Revision While Attempt Is Active

### Actor

Quiz Manager.

### Preconditions

A valid Draft Revision is available.

### Result

A new Published Revision becomes current.

Existing `IN_PROGRESS` Attempts continue using their original Revision.

```text id="qba-revision-change"
Revision 1
    ↓
Attempt A → IN_PROGRESS

Revision 2 published

Attempt A
    ↓
still uses Revision 1
```

The existing Attempt is not migrated to Revision 2.

---

## 7.2 Inform Learner of Revision Change

### Actor

System.

### Intent

Inform a learner that a newer Quiz Revision has been published while they have an active Attempt.

### Result

The User may be informed of the change.

The notification does not modify the existing Attempt.

The exact notification mechanism is an application/presentation concern.

---

# 8. Business Action Summary

The Quiz domain business actions can be grouped as follows.

### Quiz Definition

```text id="qba-summary-definition"
Create Quiz
Edit Draft Revision
Add Question
Update Question
Remove Question
Submit Draft Revision for Review
Publish Revision
Discard Draft Revision
Change Quiz Visibility
Activate Quiz
Deactivate Quiz
```

### Learner Assessment

```text id="qba-summary-learner"
Access Quiz
Check Attempt Eligibility
Start QuizAttempt
Resume QuizAttempt
Answer Question
Change Answer
Clear Answer
Navigate / Skip Question
Submit QuizAttempt
Cancel QuizAttempt
Retry Quiz
```

### System / Assessment Processing

```text id="qba-summary-system"
Expire QuizAttempt
Evaluate Attempt
Calculate Best Score
Inform Learner of Revision Change
```

---

# 9. Business Action Flow

The normal assessment lifecycle is:

```text id="qba-final-flow"
                 Create Quiz
                     │
                     ▼
               Draft Revision
                     │
              edit Questions
                     │
                     ▼
             Submit for Review
                     │
                     ▼
              Publish Revision
                     │
                     ▼
               Quiz Available
                     │
                     ▼
            Check Eligibility
                     │
                     ▼
              Start Attempt
                     │
              quota consumed
                     │
                     ▼
                IN_PROGRESS
                 /    |    \
                /     |     \
           Answer   Cancel   Deadline
              │       │        │
              │       ▼        ▼
              │   CANCELLED  EXPIRED
              │                 │
              ▼                 │
           Submit               │
              │                 │
              ▼                 │
          SUBMITTED ◄───────────┘
              │
              ▼
       Question Results
              │
              ▼
          totalScore
              │
       passingScore?
          /       \
        yes        no
         │          │
   PASSED/FAILED   score only
```

---

# 10. Important Business Boundaries

The following distinctions must be preserved:

```text id="qba-boundaries"
Quiz
    ≠
QuizAttempt

Quiz Revision
    ≠
QuizAttempt

Attempt Status
    ≠
Assessment Result

QuestionResult
    ≠
Competency

Quiz Score
    ≠
Learner Current Level

Visibility
    ≠
Availability

Retry
    ≠
Resume

Skip
    ≠
Answer State

Best Score
    ≠
QuizAttempt.totalScore
```

A Quiz defines what the assessment is.

A QuizAttempt records one learner's execution of a specific Published Revision.

A QuestionResult records assessment evidence for one Question within that Attempt.

An AssessmentResult records the final pass/fail outcome when a passing score exists.

---

# 11. Current Implementation Status

The current source contains the following related domain objects:

```text id="qba-current"
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java

src/main/java/com/deutschhub/domain/learning/model/entity/Question.java
src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java
src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java

src/main/java/com/deutschhub/domain/learning/model/enums/AttemptStatus.java
src/main/java/com/deutschhub/domain/learning/model/enums/QuestionType.java
```

The current source confirms that `QuizAttempt` currently supports the basic lifecycle and answer/scoring concepts.

However, the complete business-action workflow described above is not yet implemented.

In particular, the current source does not yet provide a complete application flow for:

* Quiz Revision management;
* exact Revision binding;
* Attempt eligibility;
* attempt quota consumption;
* Completion Policy;
* QuestionResult persistence;
* AssessmentResult;
* Best Score;
* prerequisite/unlock evaluation.

The current `QuizAttempt.create(...)` operation in:

```text id="qba-current-create"
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

currently performs object creation and initial state setup but does not implement the complete eligibility workflow defined by the target business model.

The target actions therefore represent agreed business behavior, not a claim that the current implementation already supports all of them.

