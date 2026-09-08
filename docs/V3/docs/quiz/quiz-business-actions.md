# Quiz Business Actions

## 1. Purpose

This document defines the business actions available within the Quiz domain.

A Business Action represents a meaningful business operation performed by an actor or triggered by a business condition.

This document focuses on:

- the actor performing or triggering the action;
- the business intent;
- the required conditions;
- the main business flow;
- the resulting business state.

This document does not define:

- REST endpoints;
- Application Service classes;
- persistence operations;
- database procedures;
- UI interactions;
- infrastructure implementation details.

---

# 2. Business Actors

## 2.1 Quiz Manager

A Quiz Manager is responsible for managing Quiz definitions and their Revisions.

The exact authorization mapping between system roles and Quiz Manager is an application authorization concern.

The current source defines the following roles in:

```text
src/main/java/com/deutschhub/domain/identity/enums/RoleType.java
````

```text
USER
ADMIN
CONTENT_EDITOR
```

No `INSTRUCTOR` role currently exists in the source.

Therefore, this document does not assume a new role.

---

## 2.2 Learner

A Learner is a User who may:

* access a Quiz;
* check Attempt eligibility;
* start a QuizAttempt;
* resume an existing QuizAttempt;
* answer Questions;
* change or clear answers;
* navigate between Questions;
* submit an Attempt;
* cancel an Attempt;
* retry a Quiz when eligible.

---

## 2.3 System

The System may trigger business behavior based on domain conditions.

Examples include:

* expiring an Attempt when its deadline is reached;
* evaluating an Attempt after it reaches an evaluated terminal state;
* determining derived assessment information.

The System is not treated as a human actor.

---

# 3. Quiz Definition Actions

## 3.1 Create Quiz

### Actor

Quiz Manager.

### Intent

Create a new Quiz identity and its initial Draft Revision.

### Preconditions

The actor must be authorized to create a Quiz.

### Main Flow

```text
Create Quiz
    ↓
Create Quiz identity
    ↓
Create initial Draft Revision
```

### Result

A new Quiz exists with:

* a unique Quiz identifier;
* an initial Draft Revision;
* no Published Revision yet.

The Quiz is not automatically available for learner assessment.

---

## 3.2 Edit Draft Revision

### Actor

Quiz Manager.

### Intent

Modify the assessment definition before publication.

### Preconditions

* The Quiz is not DELETED.
* A Draft Revision exists.
* The actor is authorized.
* The Revision is editable.

### Editable Definition

The Quiz Manager may modify the Draft Revision, including:

* title;
* description;
* time limit;
* passing percentage;
* maximum attempts;
* completion policy;
* difficulty;
* Questions;
* Answers.

`maxScore` is not directly edited.

It is derived from the total score of the Questions:

```text
maxScore
=
sum(Question.score)
```

### Result

The Draft Revision is updated.

The current Published Revision, if any, remains unchanged.

---

# 4. Question Actions

## 4.1 Add Question

### Actor

Quiz Manager.

### Intent

Add a Question to a Draft Revision.

### Preconditions

* The Revision is Draft.
* The actor is authorized.
* The Question belongs to that Revision.
* The Question may temporarily be incomplete while in Draft.

### Result

The Question becomes part of the Draft Revision.

A Question cannot be added directly to a Published Revision.

---

## 4.2 Update Question

### Actor

Quiz Manager.

### Intent

Modify a Question within a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The actor is authorized.

The Question does not need to be publishable during every Draft edit.

It must satisfy the required publication rules before the Revision can be published.

### Result

The Question definition is updated.

The Published Revision remains unchanged.

---

## 4.3 Remove Question

### Actor

Quiz Manager.

### Intent

Remove a Question from a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The actor is authorized.

### Result

The Question is removed from the Draft Revision.

The Revision must still satisfy all publication requirements before it can be published.

---

## 4.4 Reorder Question

### Actor

Quiz Manager.

### Intent

Change the presentation order of Questions within a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The resulting ordering remains valid.

### Result

The Question order is updated.

Question order is part of the Revision definition.

Published Revision ordering is immutable.

---

# 5. Answer Actions

## 5.1 Add Answer

### Actor

Quiz Manager.

### Intent

Add an Answer to a Question within a Draft Revision.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The actor is authorized.

A Draft Question may temporarily contain zero or more Answers.

### Result

A new Answer is added to the Question.

The Answer receives a stable identity and an order within the Question.

---

## 5.2 Update Answer

### Actor

Quiz Manager.

### Intent

Modify an Answer within a Draft Question.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The Answer exists within that Question.

### Result

The Answer definition is updated.

The resulting Question must satisfy the required publication rules before publication.

---

## 5.3 Remove Answer

### Actor

Quiz Manager.

### Intent

Remove an Answer from a Draft Question.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The Answer exists within that Question.

### Result

The Answer is removed.

Answer ordering is normalized after the removal.

A Question may temporarily become incomplete after an Answer is removed.

---

## 5.4 Reorder Answer

### Actor

Quiz Manager.

### Intent

Change the presentation order of Answers within a Question.

### Preconditions

* The Revision is Draft.
* The Question exists.
* The Answer exists.

### Result

The Answer order is updated and normalized.

The resulting order remains contiguous and starts from `1`.

Published Revision ordering is immutable.

---

## 5.5 Change Question Type

### Actor

Quiz Manager.

### Intent

Change the type of a Draft Question.

### Supported Types

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

### Preconditions

* The Revision is Draft.
* The Question exists.

A Draft Question may temporarily contain an Answer configuration that does not satisfy the new Question Type.

The Question must satisfy the corresponding structural rules before publication.

### Result

The Question Type changes.

Existing Answers are not automatically rewritten solely because the Question Type changes.

---

# 6. Revision Lifecycle Actions

## 6.1 Submit Draft Revision for Review

### Actor

Quiz Manager.

### Intent

Indicate that a Draft Revision is ready to enter the applicable review/publication process.

### Preconditions

The Draft Revision must satisfy the required submission conditions.

At minimum:

* required assessment information is present;
* the Revision is in Draft state.

The exact review workflow is not currently defined.

### Result

The Revision becomes eligible for the applicable review/publication process.

No specific external review workflow is assumed by this document.

---

## 6.2 Publish Revision

### Actor

Quiz Manager.

### Intent

Make a valid Draft Revision the current Published Revision.

### Preconditions

The Revision must satisfy all publication rules.

At minimum:

* the Revision contains at least one Question;
* every Question is valid;
* every Question has a valid Answer configuration;
* every Question has a positive score;
* `maxScore` can be derived from the Question scores;
* passing percentage, when configured, is valid;
* all required Revision configuration is valid.

### Score Rule

The Revision's maximum score is derived:

```text
maxScore
=
sum(Question.score)
```

There is no separate author-entered maximum score.

### Main Flow

```text
Draft Revision
      ↓
Validate publication rules
      ↓
Publish Revision
      ↓
Current Published Revision
```

If another Published Revision already exists:

```text
Previous Published Revision
          ↓
       Historical

New Draft Revision
          ↓
        Published
```

### Result

The new Revision becomes the current Published Revision.

Existing QuizAttempts remain permanently associated with their original Published Revision.

A new Revision does not modify existing Attempts.

---

## 6.3 Discard Draft Revision

### Actor

Quiz Manager.

### Intent

Discard an unpublished Draft Revision.

### Preconditions

* The Draft Revision exists.
* The Revision has not been published.
* The actor is authorized.

### Result

The Draft Revision is discarded.

The current Published Revision, if any, remains unchanged.

---

# 7. Quiz Governance Actions

## 7.1 Change Author

### Actor

Quiz Manager.

### Intent

Change the current responsible author of a Quiz.

### Preconditions

* The Quiz is not DELETED.
* The actor is authorized.

### Result

The current `author` changes.

The historical `createdBy` value remains unchanged.

Changing the author does not create a new Revision.

---

## 7.2 Change Quiz Visibility

### Actor

Quiz Manager.

### Intent

Change the visibility policy of a Quiz.

### Supported Values

```text
PRIVATE
PUBLIC
COURSE_ONLY
```

### Result

The Quiz visibility changes.

Changing visibility does not modify:

* Quiz Revision content;
* existing QuizAttempts;
* historical assessment results.

Visibility is distinct from:

* Access Requirements;
* Availability;
* Learning Prerequisites.

---

## 7.3 Change Quiz Availability

### Actor

Quiz Manager.

### Intent

Control whether new QuizAttempts may be created.

### Supported Values

```text
ACTIVE
INACTIVE
```

### Preconditions

The Quiz must not be DELETED.

### Result

The availability state changes.

`INACTIVE` prevents new Attempts from being started.

Existing `IN_PROGRESS` Attempts are not automatically terminated.

Availability does not change Revision lifecycle.

---

# 8. Quiz Lifecycle Actions

The Quiz lifecycle is:

```text
ACTIVE
   ↓
ARCHIVED
   ↓
DELETED
```

With restoration:

```text
DELETED
   ↓
ARCHIVED
```

And reactivation:

```text
ARCHIVED
   ↓
ACTIVE
```

Quiz lifecycle is independent from QuizRevision lifecycle.

A Published Revision may remain Published while the Quiz itself is Archived or Deleted.

---

## 8.1 Archive Quiz

### Actor

Quiz Manager.

### Intent

Archive the Quiz so that new Attempts cannot be created.

### Preconditions

* The Quiz is `ACTIVE`.
* The actor is authorized.

### Result

The Quiz becomes:

```text
ARCHIVED
```

Existing `IN_PROGRESS` Attempts are not automatically cancelled or expired.

---

## 8.2 Activate Quiz

### Actor

Quiz Manager.

### Intent

Make an Archived Quiz active again.

### Preconditions

* The Quiz is `ARCHIVED`.
* The actor is authorized.

### Result

The Quiz becomes:

```text
ACTIVE
```

The Quiz may again allow new Attempts when all other eligibility conditions are satisfied.

---

## 8.3 Delete Quiz

### Actor

Quiz Manager.

### Intent

Soft-delete an Archived Quiz.

### Preconditions

* The Quiz is `ARCHIVED`.
* The Quiz has no `IN_PROGRESS` QuizAttempt.
* The actor is authorized.

### Result

The Quiz becomes:

```text
DELETED
```

Deletion does not physically remove historical assessment information.

The following historical information is preserved:

* Quiz identity;
* historical Revisions;
* completed QuizAttempts;
* terminal Attempt results.

No business-delete cascade is performed on historical assessment evidence.

A DELETED Quiz blocks further Quiz management until restored.

---

## 8.4 Restore Quiz

### Actor

Quiz Manager.

### Intent

Restore a previously deleted Quiz.

### Preconditions

* The Quiz is `DELETED`.
* The actor is authorized.

### Result

The Quiz becomes:

```text
ARCHIVED
```

Restoration does not automatically activate the Quiz.

---

# 9. Learner Access Actions

## 9.1 Access Quiz

### Actor

Learner.

### Intent

Access a Quiz that the User is allowed to view.

### Preconditions

The applicable access rules must be satisfied.

Conceptually:

```text
Visibility
    ↓
Access Requirements
    ↓
Quiz accessible
```

For `COURSE_ONLY`:

```text
Valid Course learning access
```

is required.

For `PRIVATE`:

```text
Ordinary learner access
    ↓
Rejected
```

Visibility does not by itself make the learner eligible to start an Attempt.

---

## 9.2 Check Attempt Eligibility

### Actor

Learner / System.

### Intent

Determine whether a new QuizAttempt may be created.

### Evaluation

Eligibility evaluates the applicable conditions in business order:

```text
Quiz access
    ↓
Access Requirements
    ↓
Quiz lifecycle
    ↓
Quiz availability
    ↓
Published Revision
    ↓
Learning Prerequisites
    ↓
No conflicting IN_PROGRESS Attempt
    ↓
Attempt quota available
```

### Result

The User is either:

```text
Eligible
```

or:

```text
Not Eligible
```

with the applicable business reason.

A failed eligibility check does not create an Attempt and does not consume quota.

---

# 10. QuizAttempt Actions

## 10.1 Start QuizAttempt

### Actor

Learner.

### Intent

Create a new learner-specific execution of a Published Quiz Revision.

### Preconditions

All required eligibility conditions must be satisfied:

1. The Quiz is accessible.
2. The Quiz is in an eligible lifecycle state.
3. The Quiz is available for new Attempts.
4. A Published Revision exists.
5. Required Access Requirements are satisfied.
6. Required Learning Prerequisites are satisfied.
7. The User has no existing `IN_PROGRESS` Attempt for the Quiz.
8. Attempt quota is available.

### Main Flow

```text
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

The Attempt is permanently bound to the exact Published Revision used at creation.

One Attempt quota is consumed immediately upon successful creation.

The quota check and Attempt creation must behave as one logical business operation so that concurrent requests cannot exceed the configured limit.

---

## 10.2 Resume QuizAttempt

### Actor

Learner.

### Intent

Continue an existing active Attempt.

### Preconditions

* The Attempt exists.
* The Attempt belongs to the User.
* The Attempt is `IN_PROGRESS`.
* The Attempt has not expired.

### Result

The User continues the same QuizAttempt.

No new Attempt is created.

No additional quota is consumed.

Start eligibility is not evaluated again.

The Attempt remains bound to the same Published Revision.

The Attempt deadline continues from its original start time.

---

## 10.3 Answer Question

### Actor

Learner.

### Intent

Record the current response to a Question.

### Preconditions

* The Attempt belongs to the User.
* The Attempt is `IN_PROGRESS`.
* The Question belongs to the Attempt's bound Revision.
* Every selected Answer belongs to that Question.
* The selected response satisfies the structural rules of the Question Type.

### Result

The current response is stored as the User's current answer.

Only one current `UserAnswer` exists for a Question within an Attempt.

Correctness is not evaluated while the Attempt is `IN_PROGRESS`.

---

## 10.4 Change Answer

### Actor

Learner.

### Intent

Change an existing current response.

### Preconditions

* The Attempt is `IN_PROGRESS`.
* The Question belongs to the Attempt's Revision.
* The new response is valid for that Question.

### Result

The current response is replaced.

No answer history is created.

No evaluation is performed during the Attempt.

---

## 10.5 Clear Answer

### Actor

Learner.

### Intent

Remove the current response from a Question.

### Preconditions

* The Attempt is `IN_PROGRESS`.
* The Question belongs to the Attempt's Revision.

### Result

The Question becomes:

```text
UNANSWERED
```

No `SKIPPED` assessment state is created.

---

## 10.6 Navigate / Skip Question

### Actor

Learner.

### Intent

Move between Questions without answering the current Question.

### Result

Navigation occurs without creating an assessment answer.

The Question remains unanswered.

Skipping is navigation behavior, not a persisted domain state.

The learner may return to the Question later.

---

# 11. Attempt Completion Actions

## 11.1 Submit QuizAttempt

### Actor

Learner.

### Intent

Finish an active QuizAttempt manually.

### Preconditions

The Attempt must be:

```text
IN_PROGRESS
```

The Completion Policy determines whether unanswered Questions are allowed.

### REQUIRED_ALL

Manual submission requires every Question to be answered.

```text
All Questions answered
        ↓
Submission allowed
```

If at least one Question is unanswered:

```text
Submission rejected
```

### OPTIONAL

Unanswered Questions are allowed.

```text
Some Questions unanswered
        ↓
Submission allowed
```

### Main Flow

```text
IN_PROGRESS
    ↓
Validate Completion Policy
    ↓
Evaluate all Questions
    ↓
Create QuestionResults
    ↓
Calculate totalScore
    ↓
Determine AssessmentResult
    ↓
SUBMITTED
```

### Result

The Attempt becomes:

```text
SUBMITTED
```

Every Question in the bound Published Revision receives exactly one QuestionResult.

The final `totalScore` is calculated from the QuestionResults.

If `passingPercentage` is configured, the final AssessmentResult becomes either:

```text
PASSED
```

or:

```text
FAILED
```

If `passingPercentage` is not configured:

```text
AssessmentResult = NONE
```

---

## 11.2 Cancel QuizAttempt

### Actor

Learner.

### Intent

Intentionally terminate an active Attempt without submitting it for assessment.

### Preconditions

* The Attempt belongs to the User.
* The Attempt is `IN_PROGRESS`.

### Main Flow

```text
IN_PROGRESS
      ↓
Cancel
      ↓
CANCELLED
```

### Result

The Attempt becomes:

```text
CANCELLED
```

A cancelled Attempt:

* cannot be resumed;
* cannot be submitted;
* does not produce QuestionResults;
* does not produce an AssessmentResult;
* does not return consumed Attempt quota.

Cancellation consumes the Attempt quota.

The learner does not need to provide a cancellation reason.

---

## 11.3 Expire QuizAttempt

### Actor

System.

### Intent

Terminate an active Attempt when its deadline is reached.

### Preconditions

* The Attempt is `IN_PROGRESS`.
* The server time has reached or passed the Attempt expiration time.

### Main Flow

```text
IN_PROGRESS
      ↓
Deadline reached
      ↓
Evaluate all Questions
      ↓
Unanswered Questions → 0 score
      ↓
Create QuestionResults
      ↓
Calculate totalScore
      ↓
Determine AssessmentResult
      ↓
EXPIRED
```

### Result

The Attempt becomes:

```text
EXPIRED
```

The Attempt cannot be resumed.

`EXPIRED` does not inherently mean `FAILED`.

If `passingPercentage` is configured, the final result is determined from the evaluated score.

If no `passingPercentage` is configured:

```text
AssessmentResult = NONE
```

---

## 11.4 Retry Quiz

### Actor

Learner.

### Intent

Create another QuizAttempt after a previous Attempt has reached a terminal state.

### Preconditions

The User must:

* be allowed to access the Quiz;
* satisfy the applicable Access Requirements;
* satisfy the applicable Learning Prerequisites;
* have an eligible Published Revision;
* have no existing `IN_PROGRESS` Attempt;
* have remaining Attempt quota.

### Result

A new QuizAttempt is created.

The new Attempt receives its own:

* Attempt identifier;
* exact Published Revision binding;
* current answers;
* lifecycle;
* QuestionResults;
* score.

A previous Attempt is never reopened.

A new Attempt consumes one quota.

---

# 12. Attempt Evaluation Actions

## 12.1 Evaluate Attempt

### Actor

System / Domain.

### Intent

Evaluate an Attempt exactly once when it reaches an assessment-ending state.

### Trigger

Evaluation occurs when the Attempt reaches:

```text
SUBMITTED
```

or:

```text
EXPIRED
```

### Evaluation Rule

Every Question in the bound Published Revision is evaluated.

For an answered Question:

```text
Selected Answers
       ↓
Evaluate against Revision definition
       ↓
QuestionResult
```

For an unanswered Question:

```text
UNANSWERED
isCorrect = null
earnedScore = 0
```

### Question Result

Each QuestionResult records:

* `questionId`;
* selected Answer identifiers;
* response status;
* correctness when applicable;
* earned score.

QuestionResults are historical evaluation evidence.

They do not modify the historical Question or Answer definitions.

### Score Calculation

```text
totalScore
=
sum(QuestionResult.earnedScore)
```

For every Question:

```text
0 ≤ earnedScore ≤ Question.score
```

No partial credit is applied.

No negative marking is applied.

### Assessment Result

When `passingPercentage` is configured:

```text
scorePercentage
=
(totalScore / maxScore) × 100
```

Then:

```text
scorePercentage >= passingPercentage
        ↓
PASSED
```

Otherwise:

```text
FAILED
```

When `passingPercentage` is not configured:

```text
AssessmentResult = NONE
```

The final assessment result is determined once for the Attempt.

---

## 12.2 Calculate Best Score

### Actor

System / Query capability.

### Intent

Determine the highest eligible score achieved by a User for a Published Quiz Revision.

### Input

Historical evaluated QuizAttempts within:

```text
User × QuizRevision
```

### Included Attempts

Eligible evaluated Attempts may include:

```text
SUBMITTED
EXPIRED
```

Cancelled Attempts are excluded.

### Calculation

```text
Best Score
=
MAX(totalScore)
```

### Result

Best Score is returned as a derived value.

Best Score is not stored as the source of truth inside an individual QuizAttempt.

---

# 13. Revision Change Actions During Active Attempts

## 13.1 Publish New Revision While Attempt Is Active

### Actor

Quiz Manager.

### Intent

Publish a newer Revision while learners may still have active Attempts against an older Revision.

### Preconditions

The new Draft Revision satisfies all publication rules.

### Result

A new Published Revision becomes current.

Existing `IN_PROGRESS` Attempts remain bound to their original Revision.

Example:

```text
Revision A
    ↓
Attempt A → IN_PROGRESS

Revision B published

Attempt A
    ↓
continues using Revision A
```

The active Attempt is never migrated to Revision B.

---

## 13.2 Inform Learner of Revision Change

### Actor

System.

### Intent

Inform a learner that a newer Revision has been published while the learner has an active Attempt.

### Result

The learner may be informed of the Revision change.

The notification does not modify:

* the existing Attempt;
* its Revision binding;
* its answers;
* its deadline;
* its results.

The exact notification mechanism is an application/presentation concern.

---

# 14. Completion Requirement Actions

## 14.1 Satisfy Completion Requirement

### Actor

System / Learning domain.

### Intent

Determine whether a Quiz-based Completion Requirement has been satisfied.

### Preconditions

The Completion Requirement references a specific Published Quiz Revision.

The requirement is satisfied when the learner has at least one valid:

```text
PASSED
```

QuizAttempt for that required Revision.

The latest Attempt does not need to be the passing Attempt.

### Result

The Completion Requirement becomes satisfied.

A later Quiz Revision does not invalidate an already satisfied historical Completion Requirement.

---

## 14.2 Evaluate Learning Completion

### Actor

System / Learning domain.

### Intent

Determine whether the applicable Lesson or Section completion conditions have been satisfied.

### Rule

A mandatory Quiz Completion Requirement may be one of the conditions required for Lesson or Section completion.

Conceptually:

```text
Learning Content Completion
          +
Mandatory Completion Requirements
          ↓
Lesson / Section Completion
```

A Practice or Diagnostic Quiz does not block learning completion unless it has explicitly been declared as a Completion Requirement.

The exact learning-content completion criteria are outside the scope of this document.

---

# 15. Business Action Summary

The Quiz business actions are grouped as follows.

## 15.1 Quiz Definition

```text
Create Quiz
Edit Draft Revision

Add Question
Update Question
Remove Question
Reorder Question

Add Answer
Update Answer
Remove Answer
Reorder Answer

Change Question Type

Submit Draft Revision for Review
Publish Revision
Discard Draft Revision
```

## 15.2 Quiz Governance

```text
Change Author
Change Quiz Visibility
Change Quiz Availability

Archive Quiz
Activate Quiz
Delete Quiz
Restore Quiz
```

## 15.3 Learner Access

```text
Access Quiz
Check Attempt Eligibility
```

## 15.4 QuizAttempt

```text
Start QuizAttempt
Resume QuizAttempt

Answer Question
Change Answer
Clear Answer
Navigate / Skip Question

Submit QuizAttempt
Cancel QuizAttempt
Expire QuizAttempt
Retry Quiz
```

## 15.5 Assessment Processing

```text
Evaluate Attempt
Calculate Best Score
Inform Learner of Revision Change
```

## 15.6 Learning Completion

```text
Satisfy Completion Requirement
Evaluate Learning Completion
```

---

# 16. Normal Quiz Assessment Flow

The normal assessment lifecycle is:

```text
                    Create Quiz
                         │
                         ▼
                  Draft Revision
                         │
                  Edit Definition
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
                   /     |      \
                  /      |       \
             Answer   Cancel    Deadline
                │        │          │
                │        ▼          ▼
                │    CANCELLED    EXPIRED
                │                    │
                ▼                    │
             Submit                 │
                │                    │
                ▼                    │
            SUBMITTED ◄─────────────┘
                │
                ▼
          Evaluate Attempt
                │
                ▼
         QuestionResults
                │
                ▼
            totalScore
                │
                ▼
      passingPercentage configured?
             /          \
           yes           no
            │             │
            ▼             ▼
       PASSED/FAILED      NONE
```

---

# 17. Eligibility and Attempt Creation Flow

Starting a new Attempt follows this logical sequence:

```text
Access Quiz
     ↓
Access Requirements
     ↓
Quiz Lifecycle
     ↓
Quiz Availability
     ↓
Published Revision
     ↓
Learning Prerequisites
     ↓
Existing IN_PROGRESS Attempt?
     │
   yes ──────────────→ Resume existing Attempt
     │
    no
     ↓
Attempt Quota Available?
     │
   no ───────────────→ Reject Start
     │
   yes
     ↓
Create QuizAttempt
     ↓
Bind Published Revision
     ↓
Consume Quota
     ↓
IN_PROGRESS
```

Eligibility and Attempt creation form one logical business operation.

The business rule must prevent concurrent Start requests from exceeding the configured Attempt quota.

---

# 18. Attempt Lifecycle

The Attempt lifecycle is:

```text
                 ┌─────────────┐
                 │ IN_PROGRESS │
                 └──────┬──────┘
                        │
             ┌──────────┼──────────┐
             │          │          │
             ▼          ▼          ▼
         SUBMIT      CANCEL     EXPIRE
             │          │          │
             ▼          ▼          ▼
        SUBMITTED   CANCELLED   EXPIRED
             │
             │
             └─────── evaluation
```

Terminal states are:

```text
SUBMITTED
EXPIRED
CANCELLED
```

A terminal Attempt cannot be resumed.

A Retry creates a new Attempt rather than reopening a previous Attempt.

---

# 19. Revision and Attempt Relationship

A QuizAttempt is permanently bound to the exact Published Revision used when it starts.

```text
Quiz
 │
 ├── Revision A
 │      └── Published
 │
 ├── Revision B
 │      └── Published
 │
 └── Revision C
        └── Draft

Attempt 1 ─────────→ Revision A
Attempt 2 ─────────→ Revision B
```

Publishing a newer Revision does not migrate existing Attempts.

Each Attempt therefore preserves the assessment definition against which it was evaluated.

---

# 20. Important Business Boundaries

The following distinctions must be preserved:

```text
Quiz
    ≠
QuizAttempt

QuizRevision
    ≠
QuizAttempt

Question
    ≠
QuestionResult

Answer
    ≠
UserAnswer

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

Access Requirement
    ≠
Learning Prerequisite

Completion Requirement
    ≠
Learning Prerequisite

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

A Quiz defines the stable identity and governance of an assessment.

A QuizRevision defines one concrete version of the assessment.

A QuizAttempt records one learner's execution of a specific Published Revision.

A QuestionResult records historical evaluation evidence for one Question within an Attempt.

An AssessmentResult records the final pass/fail outcome when a `passingPercentage` is configured.

---

# 21. Current Implementation Status

The current source contains the following Quiz-related domain objects:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java

src/main/java/com/deutschhub/domain/learning/model/entity/Question.java
src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java
src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java

src/main/java/com/deutschhub/domain/learning/model/enums/AttemptStatus.java
src/main/java/com/deutschhub/domain/learning/model/enums/QuestionType.java
src/main/java/com/deutschhub/domain/learning/model/enums/QuizStatus.java
```

The current implementation confirms the existence of basic Quiz, Question, Answer, UserAnswer, and QuizAttempt concepts.

However, the complete target business-action workflow is not yet implemented.

In particular, the current source does not yet provide a complete application/domain flow for:

* Quiz Revision management;
* exact Published Revision binding;
* Quiz eligibility evaluation;
* Access Requirements;
* Learning Prerequisites;
* Attempt quota enforcement;
* Completion Policy;
* QuestionResult persistence;
* AssessmentResult;
* Best Score;
* Completion Requirement evaluation;
* Learning Completion integration.

The current `QuizAttempt.create(...)` operation in:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

currently performs basic object creation and initial lifecycle setup.

It does not represent the complete eligibility and Attempt-creation workflow defined by the target business model.

Similarly, the current Quiz implementation does not yet provide the target `QuizRevision` model.

Therefore:

```text
Current Implementation
        ≠
Target Business Actions
```

The Business Actions defined in this document describe the agreed target business behavior.

They do not claim that the current implementation already supports all of these actions.
