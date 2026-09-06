# Quiz Business Rules

## 1. Purpose

This document defines the business rules governing Quiz definitions, Quiz Revisions, QuizAttempts, assessment results, retry policies, and Quiz availability within the Learning context.

The rules are based on the current DeutschHub implementation and the target domain decisions established during Quiz domain discovery.

The document distinguishes confirmed business rules from decisions that remain open.

---

## 2. Quiz Definition Rules

### BR-QUIZ-01 — Quiz is an Independent Assessment Definition

A Quiz is an independent assessment definition within the Learning context.

A Quiz is not owned by a Course or Lesson aggregate.

A Quiz may be placed inside a Lesson through a `LessonItem`, which references the Quiz by identifier.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
````

---

### BR-QUIZ-02 — Published Quiz Definitions Are Immutable

A Published Quiz definition must not be modified in place.

Any modification to a Published Quiz must be performed through a new Draft Revision.

This protects the integrity of existing QuizAttempts that depend on the previously published definition.

---

## 3. Quiz Revision Rules

### BR-REV-01 — Draft and Published Revisions

A Quiz may have:

* at most one Draft Revision;
* at most one Published Revision.

A Draft Revision and a Published Revision may coexist.

```text
Quiz
├── Published Revision
└── Draft Revision
```

---

### BR-REV-02 — Creating a New Revision

When a Published Quiz needs to be modified, the modification must be performed on a new Draft Revision.

The existing Published Revision remains unchanged.

A new Draft Revision must not be created while another active Draft Revision already exists.

The existing Draft must instead be edited, published, or discarded.

---

### BR-REV-03 — Draft Revision May Be Discarded

A Draft Revision may be discarded before publication.

Discarding a Draft does not modify the current Published Revision.

```text
Published Revision 1
        +
Draft Revision 2
        │
        └── discard
               ↓
Published Revision 1
```

---

### BR-REV-04 — Publishing a New Revision

When a Draft Revision is published:

1. The Draft becomes the new Published Revision.
2. The previous Published Revision becomes historical.
3. Existing QuizAttempts remain associated with their original Revision.

Historical Revisions must be retained because existing QuizAttempts may depend on their assessment definitions.

---

### BR-REV-05 — Revision Contains Assessment Rules

Assessment configuration belongs to the applicable Quiz Revision.

This includes, where applicable:

* maximum score;
* passing score;
* time limit;
* maximum attempts;
* questions;
* answer definitions;
* other assessment rules defined for that Revision.

A historical QuizAttempt must continue to use the rules of the Revision from which it started.

---

## 4. Question and Answer Rules

### BR-QUESTION-01 — Question Answer Count

A Question must contain at least two answers and no more than six answers.

---

### BR-QUESTION-02 — Correct Answer Requirement

A Question must contain at least one correct answer.

---

### BR-QUESTION-03 — Single Choice

A `SINGLE_CHOICE` Question may have at most one correct answer.

---

### BR-QUESTION-04 — True/False

A `TRUE_FALSE` Question must contain exactly two answers and exactly one correct answer.

---

### BR-QUESTION-05 — Answer Uniqueness

Answer content must be unique within a Question, ignoring case.

---

### BR-QUESTION-06 — Question Score

A Question must have a positive score.

The total score of all Questions must match the Quiz Revision's `maxScore` before that Revision can be published.

---

## 5. Quiz Publication Rules

### BR-PUBLISH-01 — Questions Are Required

A Quiz Revision cannot be published without Questions.

---

### BR-PUBLISH-02 — Questions Must Be Valid

Every Question in a Quiz Revision must satisfy its domain validation rules before publication.

---

### BR-PUBLISH-03 — Maximum Score Consistency

The sum of Question scores must equal the Revision's configured `maxScore` before publication.

---

### BR-PUBLISH-04 — Published Definition Cannot Be Modified

After publication, the Quiz definition is immutable.

Changes must be made through a new Draft Revision.

---

## 6. Passing Score Rules

### BR-SCORE-01 — Passing Score Is Optional

A Quiz does not have to define a passing score.

This supports both assessment-oriented and practice-oriented Quizzes.

```text
Practice Quiz
passingScore = none

Assessment Quiz
passingScore = defined
```

---

### BR-SCORE-02 — Passing Score Must Be Valid

When a passing score is defined:

```text
0 <= passingScore <= maxScore
```

The passing score belongs to the Quiz Revision.

---

### BR-SCORE-03 — Assessment Result

If a Quiz Revision defines a passing score, the final score of a QuizAttempt determines its Assessment Result.

```text
totalScore >= passingScore
        ↓
     PASSED

totalScore < passingScore
        ↓
     FAILED
```

---

### BR-SCORE-04 — No Passing Score Means No Pass/Fail Result

When a Quiz Revision has no passing score, the QuizAttempt may have a numeric score but does not produce a `PASSED` or `FAILED` Assessment Result.

---

### BR-SCORE-05 — Assessment Result Is Separate from Attempt Status

`PASSED` and `FAILED` are Assessment Results.

They are not QuizAttempt lifecycle statuses.

```text
Attempt Status
├── IN_PROGRESS
├── SUBMITTED
├── EXPIRED
└── CANCELLED

Assessment Result
├── PASSED
└── FAILED
```

---

## 7. QuizAttempt Creation Rules

### BR-ATTEMPT-01 — Attempt Starts from Published Revision

A QuizAttempt may only be started from a Published Quiz Revision.

A Draft Revision cannot be used to start an Attempt.

---

### BR-ATTEMPT-02 — Exact Revision Binding

When a QuizAttempt starts, it becomes permanently associated with the exact Published Quiz Revision used at that time.

The Attempt must not switch to another Revision later.

```text
Published Revision 1
        │
        ▼
QuizAttempt
        │
        └── Revision 1
```

---

### BR-ATTEMPT-03 — Historical Assessment Integrity

Changes to the Quiz after an Attempt starts must not change the assessment definition used by that Attempt.

The Attempt's:

* Questions;
* Answers;
* Question scores;
* maximum score;
* passing score;
* time limit;
* attempt policy;
* other applicable assessment rules

remain determined by its associated Revision.

---

## 8. Attempt Limit Rules

### BR-ATTEMPT-LIMIT-01 — Multiple Attempts

A Quiz may allow multiple Attempts.

The maximum number of Attempts depends on the Quiz's assessment policy.

Examples include:

```text
Practice Quiz
maxAttempts = 10

Formal Assessment
maxAttempts = 1

Other Assessment
maxAttempts = 3
```

---

### BR-ATTEMPT-LIMIT-02 — Attempt Policy Is Business-Specific

Retry eligibility is not globally fixed.

The business policy of a Quiz may determine:

* maximum number of Attempts;
* whether retrying after a pass is allowed;
* whether retrying after a failure is allowed;
* temporal restrictions on retrying.

---

### BR-ATTEMPT-LIMIT-03 — Cancelled Attempts Do Not Consume Quota

A `CANCELLED` Attempt does not consume the Quiz's attempt quota.

For example:

```text
maxAttempts = 3

Attempt 1 → SUBMITTED → consumes 1
Attempt 2 → CANCELLED → consumes 0
Attempt 3 → SUBMITTED → consumes 1
```

---

### BR-ATTEMPT-LIMIT-04 — Passed Attempt Retry

Whether a User may create another Attempt after receiving `PASSED` depends on the Quiz's assessment policy.

Practice-oriented Quizzes may allow further Attempts.

High-stakes or limited-frequency assessments may prohibit another Attempt during the applicable eligibility period.

---

### BR-ATTEMPT-LIMIT-05 — Failed Attempt Retry

Whether and when a User may retry after `FAILED` depends on the Quiz's assessment policy.

A Quiz may allow:

```text
FAILED
  ↓
retry immediately
```

or impose a temporal restriction such as a cooldown or later eligibility period.

The exact temporal policy model remains open.

---

## 9. Concurrent Attempt Rules

### BR-CONCURRENT-01 — One Active Attempt

A User may have at most one `IN_PROGRESS` QuizAttempt for the same Quiz at a time.

```text
Quiz A

User
├── Attempt 1 → SUBMITTED
├── Attempt 2 → SUBMITTED
└── Attempt 3 → IN_PROGRESS
```

The following is not allowed:

```text
Quiz A

User
├── Attempt 3 → IN_PROGRESS
└── Attempt 4 → IN_PROGRESS
```

---

## 10. Attempt Resume Rules

### BR-RESUME-01 — IN_PROGRESS Attempts May Be Resumed

An `IN_PROGRESS` QuizAttempt may be resumed after the User temporarily leaves the Quiz.

This includes:

* closing the browser;
* losing network connectivity;
* temporarily leaving the Quiz.

These events do not automatically terminate the Attempt.

---

### BR-RESUME-02 — Browser Closure Does Not Cancel an Attempt

Closing the browser or leaving the Quiz does not automatically change the Attempt status.

```text
IN_PROGRESS
    ↓
browser closed
    ↓
IN_PROGRESS
    ↓
resume
```

---

## 11. Time Limit Rules

### BR-TIME-01 — Time Limit Starts with the Attempt

When a Quiz has a time limit, the timer starts when the QuizAttempt starts.

---

### BR-TIME-02 — Time Limit Uses Real Elapsed Time

The time limit continues to run in real time.

Leaving the Quiz, closing the browser, or losing network connectivity does not pause the timer.

```text
10:00 → Attempt starts
10:10 → network lost
10:20 → User returns
10:30 → 30-minute limit reached
```

---

### BR-TIME-03 — Deadline Terminates the Attempt

When the time limit is reached, the Attempt is considered expired.

The Attempt becomes:

```text
EXPIRED
```

The expiration is based on the Attempt's deadline rather than on User interaction.

---

### BR-TIME-04 — Expired Attempt Cannot Be Resumed

An `EXPIRED` Attempt is terminal.

It cannot be resumed.

If the User is eligible for another Attempt, a new QuizAttempt must be created.

---

### BR-TIME-05 — Expired Assessment Result

If the Quiz Revision defines a passing score, an expired Attempt produces:

```text
EXPIRED
+
passingScore exists
    ↓
FAILED
```

If no passing score exists:

```text
EXPIRED
+
no passingScore
    ↓
no Assessment Result
```

---

## 12. Cancellation Rules

### BR-CANCEL-01 — Intentional Cancellation

A QuizAttempt may be cancelled intentionally by:

* the User;
* the System;
* an authorized Admin/Instructor.

---

### BR-CANCEL-02 — Accidental Interruption Does Not Cancel

The following do not automatically cancel an Attempt:

* browser closure;
* network disconnection;
* leaving the Quiz temporarily.

The Attempt remains `IN_PROGRESS` until another valid terminal condition occurs.

---

### BR-CANCEL-03 — Cancelled Attempts Are Terminal

A `CANCELLED` Attempt cannot be resumed.

If the User is eligible for another Attempt, a new Attempt must be created.

---

## 13. Submission and Assessment Rules

### BR-SUBMIT-01 — Score Is Calculated on Attempt Completion

The final score is calculated when the Attempt reaches its assessment completion outcome.

For a normal submission:

```text
IN_PROGRESS
    ↓
SUBMIT
    ↓
calculate totalScore
    ↓
SUBMITTED
```

---

### BR-SUBMIT-02 — Assessment Result Is Determined at Attempt Completion

Assessment Result is not determined while the Attempt remains `IN_PROGRESS`.

It is determined only when the Attempt reaches a terminal assessment outcome.

---

### BR-SUBMIT-03 — Score Uses the Exact Attempt Revision

A QuizAttempt must be scored using the exact Quiz Revision associated with the Attempt.

A newer Published Revision must never be used to score an older Attempt.

---

## 14. Revision Changes During an Active Attempt

### BR-REV-ATTEMPT-01 — Publishing a New Revision Does Not Invalidate Active Attempts

Publishing a new Quiz Revision does not automatically terminate or invalidate existing `IN_PROGRESS` Attempts.

An existing Attempt may continue and be submitted.

```text
Revision 1 → Published
     │
     └── Attempt A → IN_PROGRESS

Revision 2 → Published

Attempt A
└── continues using Revision 1
```

---

### BR-REV-ATTEMPT-02 — User Notification

When a newer Revision has been published while a User has an active Attempt, the User may be informed that the Quiz has been updated.

The existing Attempt remains bound to its original Revision.

---

### BR-REV-ATTEMPT-03 — Prerequisite Assessment Exception

If a Quiz is used as a prerequisite for progressing to a new learning stage, the business may require the User to complete a new Attempt using the newer Revision.

This does not change the historical identity or Revision association of the existing Attempt.

The rule determining whether an Assessment Result is sufficient to unlock a learning stage belongs outside the Quiz aggregate.

---

## 15. Quiz Availability Rules

### BR-AVAIL-01 — Quiz Availability

A Quiz may be Active or Inactive independently from its Revision lifecycle.

Availability answers whether a User may start a new Attempt.

```text
Quiz Lifecycle
├── Draft
├── Published
└── Historical

Quiz Availability
├── Active
└── Inactive
```

---

### BR-AVAIL-02 — Active Quiz

An Active Quiz may be used to start new Attempts, provided all other Attempt eligibility rules are satisfied.

---

### BR-AVAIL-03 — Inactive Quiz

An Inactive Quiz does not allow new QuizAttempts to be started.

---

### BR-AVAIL-04 — Deactivation Does Not Terminate Active Attempts

Deactivating a Quiz does not automatically terminate existing `IN_PROGRESS` Attempts.

Existing Attempts may continue and be submitted according to the Revision from which they started.

```text
Quiz → INACTIVE

New Attempt
    ✗

Existing IN_PROGRESS Attempt
    ✓ continue
```

---

## 16. Historical Attempt Rules

### BR-HISTORY-01 — Historical Revisions Are Retained

A Quiz Revision used by an existing QuizAttempt must remain available as historical assessment data.

A Revision must not be removed merely because a newer Revision has been published.

---

### BR-HISTORY-02 — Attempts Preserve Historical Assessment Context

A historical QuizAttempt must remain interpretable according to the Quiz Revision from which it started.

This ensures that its score and Assessment Result can be understood in the context of the assessment definition that was actually presented to the User.

---

## 17. Assessment and Learning Progression

### BR-PROGRESSION-01 — Assessment Result Does Not Represent Competency

`PASSED` or `FAILED` is an assessment outcome.

It does not directly represent:

* Competency;
* Learner Current Level;
* Certification Level.

```text
QuizAttempt
    ↓
Assessment Result
    ↓
Learning Progression may evaluate eligibility
```

---

### BR-PROGRESSION-02 — Quiz Does Not Own Learning Stage Unlock Rules

A Quiz may provide an Assessment Result that is used by Learning Progression.

The Quiz itself does not own the rule determining whether a learner may unlock a subsequent learning stage.

The exact prerequisite and progression model remains open.

---

## 18. Current Implementation Notes

The current implementation already contains several concepts represented by these rules.

### Confirmed Current Code

The following source files contain the current Quiz domain implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
src/main/java/com/deutschhub/domain/learning/model/entity/Question.java
src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java
src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The current implementation already contains:

* Quiz questions;
* Question validation;
* answer validation;
* maximum score;
* passing score;
* time limit;
* QuizAttempt;
* UserAnswer;
* Attempt status;
* total score;
* Quiz placement through `LessonItem.quizId`.

---

## 19. Current Implementation Gaps

The target rules above are not all implemented in the current model.

### 19.1 QuizRevision

A `QuizRevision` model has not been found in the current source.

The current model is effectively:

```text
Quiz
└── Question
```

rather than:

```text
Quiz
└── QuizRevision
    └── Question
```

---

### 19.2 Revision-bound QuizAttempt

The current `QuizAttempt` stores `quizId`.

It does not currently contain an explicit Quiz Revision reference.

The target model requires the Attempt to remain bound to the exact Published Revision from which it started.

---

### 19.3 AssessmentResult

A separate `AssessmentResult` concept has not been found in the current implementation.

The current `QuizAttempt` calculates `totalScore`, but there is no separate `PASSED` / `FAILED` result model.

---

### 19.4 Complete Attempt Application Flow

No complete Application/Infrastructure flow for QuizAttempt execution was found during the current analysis.

The current `QuizAttempt.submit(...)` domain operation accepts Questions externally and performs scoring inside the aggregate.

The target model will require the scoring operation to use the exact Revision associated with the Attempt.

---

### 19.5 Quiz Availability

The current `QuizStatus` contains:

```text
DRAFT
PUBLISHED
ARCHIVED
DELETED
```

A separate Active/Inactive availability concept has not been found in the current implementation.

The Active/Inactive availability model in this document is therefore a target business decision, not a description of the current code.

---

## 20. Open Decisions

The following decisions remain intentionally open and should not be inferred from the rules above.

### OPEN-01 — Temporal Retry Policy

The business may require cooldowns or eligibility windows for subsequent Attempts.

Examples:

```text
retry immediately
retry after 24 hours
one attempt per month
```

The exact policy model has not yet been defined.

---

### OPEN-02 — Attempt Eligibility Period

The relationship between `maxAttempts` and time-based eligibility windows has not yet been modeled.

A monthly assessment may require more than a simple `maxAttempts = 1` rule.

---

### OPEN-03 — Assessment Result Representation

The business meaning of `PASSED` and `FAILED` is confirmed.

The exact domain representation of `AssessmentResult` remains open.

---

### OPEN-04 — Expired Attempt Answer Retention

It is confirmed that answers from an expired Attempt must not be carried over into a new Attempt.

Whether those historical answers should be retained for audit/history is not yet decided.

---

### OPEN-05 — Learning Stage Prerequisites

The business has identified scenarios where a Quiz may be required to unlock a subsequent learning stage.

The exact ownership and model of prerequisite rules remain open.

---

### OPEN-06 — Quiz Management Authorization

The exact roles authorized to create, edit, publish, discard, activate, and deactivate Quiz definitions have not yet been finalized.

The business actor must be authorized to manage the applicable Quiz Revision.

---

## 21. Summary

The target Quiz business model is based on three separate concerns:

```text
Quiz Definition
└── QuizRevision
    └── Questions / Answers

Quiz Execution
└── QuizAttempt
    └── UserAnswers

Assessment Outcome
└── AssessmentResult
```

The central integrity rule is:

```text
QuizAttempt
    ↓
exact Published QuizRevision
    ↓
score
    ↓
AssessmentResult
```

Quiz Revision changes, Quiz availability changes, and Attempt lifecycle changes must not silently alter the assessment definition of an Attempt that has already started.

This separation allows DeutschHub to support both:

* repeatable practice Quizzes;
* controlled formal assessments;

without requiring a different core Quiz model for each business use case.
