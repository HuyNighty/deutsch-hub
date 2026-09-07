# Quiz Domain Model

## 1. Purpose

This document defines the target domain model and aggregate boundaries for the Quiz domain within the Learning context.

The model is based on the current DeutschHub implementation and the business decisions established during domain discovery.

The target model separates:

- Quiz identity and governance from Quiz revision content.
- Current Quiz definitions from historical Quiz definitions.
- Assessment definition from learner-specific assessment execution.
- Attempt lifecycle from assessment result.
- Learner answers from assessment evidence.
- Quiz placement within a Lesson from the Quiz aggregate itself.

This document describes the domain structure and relationships. Detailed business rules and business actions are defined separately in:

- `quiz-business-rules.md`
- `quiz-business-actions.md`

---

## 2. Domain Position

Quiz belongs to the Learning domain.

A Quiz is an assessment concept that may be placed inside a Lesson through a `LessonItem`.

The `LessonItem` does not contain the Quiz aggregate. It references the Quiz by identifier.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
````

This keeps learning structure and assessment definition as separate domain concepts.

The Quiz aggregate does not own Course, Section, Lesson, or LessonItem.

---

## 3. Quiz Aggregate

### 3.1 Aggregate Root

`Quiz` is an independent Aggregate Root.

The Quiz aggregate owns the identity and governance of a Quiz and its versioned assessment definitions.

Target conceptual structure:

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── AnswerQuestion
```

A Quiz is not contained inside `Course`, `Lesson`, or `LessonItem`.

### 3.2 Quiz Responsibilities

The Quiz aggregate is responsible for concepts that belong to the Quiz itself, including:

* Quiz identity;
* visibility;
* availability;
* revision lifecycle;
* access-related configuration;
* the relationship between the Quiz and its revisions.

The Quiz aggregate is not responsible for learner-specific execution.

Learner-specific execution belongs to `QuizAttempt`.

---

## 4. Quiz Revision

### 4.1 Purpose

A `QuizRevision` represents a specific version of a Quiz definition.

A revision contains the assessment definition that is used when a QuizAttempt is started.

A published revision is immutable.

Any modification to a published Quiz definition must be performed through a new Draft Revision and explicitly published as a new definition.

### 4.2 Revision Lifecycle

A Quiz may have:

* at most one Draft Revision;
* at most one Published Revision;
* zero or more historical revisions.

A newly created Quiz starts with a Draft Revision.

```text
New Quiz
└── Draft Revision
```

After the first publication:

```text
Quiz
└── Published Revision
```

After editing a published Quiz:

```text
Quiz
├── Published Revision
└── Draft Revision
```

After publishing the new Draft Revision:

```text
Quiz
├── New Published Revision
└── Previous Published Revision → historical
```

Historical revisions are retained because existing QuizAttempts must remain associated with the exact assessment definition used when they started.

The exact representation and lifecycle status of historical revisions remains an implementation detail to be determined later.

### 4.3 Draft and Published Revisions

A Draft Revision may be incomplete.

A Draft Revision may be created and edited before publication.

Submission for review requires the required business information to be present, including a valid title.

A Published Revision represents an assessment definition that may be used to start new QuizAttempts.

A published revision must not be modified in place.

---

## 5. Quiz Governance

Quiz governance is separate from revision content.

### 5.1 Visibility

Visibility determines who may discover or access the Quiz at a high level.

Current supported visibility concepts are:

```text
PRIVATE
COURSE_ONLY
PUBLIC
```

Conceptually:

```text
Quiz
├── visibility
└── availability
```

Visibility is a property of the Quiz, not of an individual Revision.

The exact access rules associated with each visibility mode are business rules and are defined separately.

In particular:

* `PRIVATE` is not available to ordinary learners.
* `PUBLIC` may proceed to eligibility checks.
* `COURSE_ONLY` requires valid Course learning access.

The Quiz remains an independent aggregate and does not regain direct ownership of Course or Lesson.

### 5.2 Availability

Quiz availability determines whether the Quiz can currently accept new Attempts.

Availability is distinct from:

* visibility;
* Revision lifecycle;
* Attempt lifecycle.

Deactivating a Quiz prevents creation of new Attempts but does not automatically terminate existing `IN_PROGRESS` Attempts.

---

## 6. Quiz Revision Content

A Quiz Revision contains the configuration and questions required to define an assessment.

Conceptually:

```text
QuizRevision
├── title
├── description
├── timeLimit
├── maxScore
├── passingScore?
├── maxAttempts
├── completionPolicy
├── difficulty
└── questions
    └── Question
        └── AnswerQuestion
```

### 6.1 Time Limit

`timeLimit` defines the maximum real-world duration of an Attempt.

The timer starts when the Attempt starts and continues to run regardless of:

* temporary network disconnection;
* browser closure;
* leaving the Quiz temporarily.

An `IN_PROGRESS` Attempt may be resumed, but elapsed time is not paused.

The server-side time is the source of truth.

If the deadline has been reached when the server processes an operation, the Attempt is considered expired.

### 6.2 Maximum Score

`maxScore` defines the maximum score of the Revision.

The maximum score is determined by the sum of Question scores.

Before publication:

```text
sum(Question.score) = QuizRevision.maxScore
```

The maximum score of the Revision is preserved for historical assessment calculations.

### 6.3 Passing Score

`passingScore` is optional.

A Quiz may define a passing score when pass/fail evaluation is required.

When no passing score exists:

```text
AssessmentResult = none
```

The Attempt may still produce a numeric score.

When a passing score exists:

```text
totalScore >= passingScore → PASSED
totalScore < passingScore  → FAILED
```

The passing score belongs to the Quiz Revision so that historical Attempts continue to use the assessment rule that was effective when they started.

### 6.4 Maximum Attempts

`maxAttempts` defines the maximum number of Attempts that may be created for a User under the applicable Published Revision.

The quota is associated with:

```text
User × Published Quiz Revision
```

The quota is consumed immediately when a new Attempt is successfully created.

Terminal status does not determine whether quota is consumed.

Therefore:

```text
Create Attempt
      ↓
quota consumed
      ↓
IN_PROGRESS
      ↓
SUBMITTED / EXPIRED / CANCELLED
```

A cancelled Attempt does not return consumed quota.

### 6.5 Completion Policy

`completionPolicy` defines the rule for manual submission.

Supported concepts are:

```text
REQUIRED_ALL
OPTIONAL
```

#### REQUIRED_ALL

All Questions must have an answer before manual submission is accepted.

A learner may leave Questions unanswered temporarily while the Attempt remains `IN_PROGRESS`.

If the time limit expires, unanswered Questions remain unanswered and receive zero score.

The Attempt becomes `EXPIRED`.

#### OPTIONAL

Manual submission is allowed even when some Questions remain unanswered.

Unanswered Questions receive zero score.

Completion Policy belongs to the Revision because it is part of the assessment definition used by the Attempt.

---

## 7. Question Entity

`Question` is an entity within a Quiz Revision.

Target relationship:

```text
Quiz
└── QuizRevision
    └── Question
```

A Question contains:

* Question identifier;
* Question content;
* score;
* question type;
* answer options.

Supported Question types are currently:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

Text-based Question types are not part of the current target model.

### 7.1 Question Constraints

The current domain implementation establishes the following constraints:

* A Question must have at least two answers.
* A Question may have at most six answers.
* At least one answer must be correct.
* Single-choice Questions may have at most one correct answer.
* True/False Questions must contain exactly two answers.
* True/False Questions must contain exactly one correct answer.
* Question score must be greater than zero.
* Question content must not be blank.
* Answer content must not be blank.
* Duplicate answer content is not allowed within a Question.

These constraints form part of the Quiz definition and therefore belong to the Revision containing the Question.

---

## 8. AnswerQuestion Entity

`AnswerQuestion` is an entity owned by a Question.

```text
Quiz
└── QuizRevision
    └── Question
        └── AnswerQuestion
```

An answer contains:

* answer identifier;
* answer content;
* correctness state.

The correctness of an answer is part of the Quiz Revision's assessment definition.

---

## 9. QuizAttempt Aggregate

`QuizAttempt` is a separate Aggregate Root from `Quiz`.

It represents one learner-specific execution of a Quiz using one exact Published Revision.

Target conceptual structure:

```text
QuizAttempt Aggregate
└── QuizAttempt
    ├── quizId
    ├── revisionId
    ├── userId
    ├── status
    ├── answers
    │   └── UserAnswer
    ├── results
    │   └── QuestionResult
    ├── totalScore
    ├── assessmentResult?
    ├── startedAt
    └── submittedAt / endedAt
```

The Attempt is not contained inside the Quiz aggregate.

The Attempt owns learner-specific execution state and assessment evidence.

---

## 10. Attempt-to-Revision Relationship

When a User starts a QuizAttempt, the Attempt must be associated with the exact Published Quiz Revision used at the time of start.

```text
Published Revision 1
        │
        ▼
   QuizAttempt 1
```

If a new revision is subsequently published:

```text
Quiz
├── Revision 1 → historical
└── Revision 2 → current Published Revision

QuizAttempt 1
└── revisionId → Revision 1
```

The existing Attempt does not switch to Revision 2.

Its:

* Questions;
* Answer definitions;
* Question scores;
* Maximum score;
* Passing score;
* Completion policy;
* Attempt policy;
* Other assessment rules

remain determined by the Revision from which the Attempt started.

This guarantees assessment consistency and preserves historical assessment integrity.

An Attempt may continue and submit even if a newer Revision is published while it is `IN_PROGRESS`.

The User may be informed that the Quiz definition has changed.

---

## 11. QuizAttempt Lifecycle

The Attempt lifecycle is represented separately from its assessment result.

### 11.1 Attempt Status

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

### 11.2 IN_PROGRESS

The User is currently working on the Attempt.

An `IN_PROGRESS` Attempt may be resumed after:

* leaving the Quiz;
* closing the browser;
* losing network connectivity.

These events do not automatically terminate the Attempt.

Only one `IN_PROGRESS` Attempt is allowed for the same User and Quiz at a time.

### 11.3 SUBMITTED

The User has completed and submitted the Attempt.

When submission is accepted:

* the Attempt becomes `SUBMITTED`;
* Question Results are determined;
* `totalScore` is calculated;
* `AssessmentResult` is determined when `passingScore` exists.

### 11.4 EXPIRED

The Attempt has reached its time limit.

Expiration is based on real elapsed time from the Attempt's start.

An expired Attempt is terminal and cannot be resumed.

Unanswered Questions receive zero score.

If the Revision has a passing score:

```text
totalScore >= passingScore → PASSED
totalScore < passingScore  → FAILED
```

Therefore an expired Attempt may produce a `FAILED` Assessment Result.

If no passing score exists:

```text
AttemptStatus = EXPIRED
AssessmentResult = none
```

### 11.5 CANCELLED

The User may intentionally cancel their own `IN_PROGRESS` Attempt.

Cancellation is terminal.

A cancelled Attempt:

* cannot be resumed;
* does not produce a final assessment result;
* does not become a submitted or expired assessment;
* does not return previously consumed attempt quota.

Administrative or Instructor intervention is outside the current scope.

Accidental browser closure or temporary network loss does not automatically cause cancellation.

---

## 12. UserAnswer Entity

`UserAnswer` belongs to a QuizAttempt.

It represents the current response of the User to one Question.

```text
QuizAttempt
└── UserAnswer
```

A UserAnswer contains conceptually:

* Question identifier;
* selected answer identifiers;
* current response state.

For the current target model:

```text
One current UserAnswer
per Question
per Attempt
```

A User may change an answer while the Attempt is `IN_PROGRESS`.

A User may clear an answer and return the Question to an unanswered state.

Skipping a Question is a navigation action and is not represented as a separate persisted `SKIPPED` answer state.

There is no answer history in the current target model.

Answers from one Attempt are not carried over into another Attempt.

---

## 13. Question Result

`QuestionResult` represents the assessment evidence produced for one Question when an Attempt ends with an assessment outcome.

Target structure:

```text
QuizAttempt
└── QuestionResult
    ├── questionId
    ├── responseStatus
    ├── isCorrect
    └── earnedScore
```

### 13.1 Response Status

```text
ANSWERED
UNANSWERED
```

The result is created for every Question in the Revision, including unanswered Questions.

Conceptually:

```text
Answered + correct
→ ANSWERED
→ isCorrect = true
→ earnedScore = full Question score

Answered + incorrect
→ ANSWERED
→ isCorrect = false
→ earnedScore = 0

Unanswered
→ UNANSWERED
→ isCorrect = null
→ earnedScore = 0
```

`QuestionResult` is assessment evidence.

It is not:

* Competency;
* Learner Current Level;
* Learning Progress.

Detailed interpretation of results for learner analytics belongs to later Learner State capabilities.

---

## 14. Scoring

Each Question has a fixed score.

The target scoring model is:

```text
SINGLE_CHOICE
→ correct = full score
→ incorrect = 0

TRUE_FALSE
→ correct = full score
→ incorrect = 0

MULTIPLE_CHOICE
→ exact selected set matches correct set = full score
→ otherwise = 0
```

There is no partial credit.

There is no negative marking.

For an ended Attempt:

```text
totalScore
=
sum(QuestionResult.earnedScore)
```

`totalScore` is a historical fact of that Attempt and does not change after the Attempt has ended.

The target model does not store a separate score percentage as an independent fact.

Percentage may be derived from:

```text
totalScore / QuizRevision.maxScore
```

using the exact Revision associated with the Attempt.

---

## 15. Assessment Result

Assessment Result is a separate concept from Attempt Status.

```text
AttemptStatus
├── IN_PROGRESS
├── SUBMITTED
├── EXPIRED
└── CANCELLED

AssessmentResult
├── PASSED
└── FAILED
```

`AttemptStatus` answers:

> What is the lifecycle state of this Attempt?

`AssessmentResult` answers:

> Did the completed assessment satisfy its passing requirement?

Assessment Result is determined only when the Attempt reaches an assessment-ending state:

```text
SUBMITTED
or
EXPIRED
```

For a Revision with a passing score:

```text
totalScore >= passingScore → PASSED
totalScore < passingScore  → FAILED
```

For a Revision without a passing score:

```text
AssessmentResult = none
```

A `CANCELLED` Attempt does not produce an Assessment Result.

Assessment Result must not be interpreted as:

* Competency;
* Learner Current Level;
* Learning Progress.

---

## 16. Best Score

Best Score is a derived concept across historical Attempts.

It is not a field owned by an individual QuizAttempt.

For a User and a Published Quiz Revision:

```text
Best Score
=
MAX(totalScore)
across eligible historical Attempts
```

Conceptually:

```text
User
  +
Published Revision
  │
  ├── Attempt 1 → 70
  ├── Attempt 2 → 85
  └── Attempt 3 → 60
             │
             ▼
        Best Score = 85
```

A later lower-scoring Attempt does not overwrite the previous higher score.

All historical Attempts remain preserved.

The exact query/read-model mechanism for obtaining Best Score is an application or persistence concern rather than an additional field on `QuizAttempt`.

---

## 17. Attempt Limits and Retry

A Quiz Revision may allow multiple Attempts according to its `maxAttempts` policy.

The quota applies to:

```text
User × Published Quiz Revision
```

A new Attempt consumes one quota immediately after successful creation.

Example:

```text
maxAttempts = 3

Create Attempt 1
→ quota consumed: 1

Attempt 1 → CANCELLED

Create Attempt 2
→ quota consumed: 2

Attempt 2 → FAILED

Create Attempt 3
→ quota consumed: 3

Attempt 3 → PASSED
```

The cancelled Attempt does not return its consumed quota.

Retry creates a new QuizAttempt.

A previous Attempt is never reopened or converted into a new Attempt.

A User may have multiple historical Attempts for the same Quiz Revision, but only one Attempt may be `IN_PROGRESS` for the same User and Quiz at a time.

Whether a User may retry after `PASSED` or `FAILED` is governed by the applicable Attempt Policy and remaining quota.

---

## 18. Attempt Creation Eligibility

Creating a QuizAttempt is not only an object-construction operation.

Conceptually, a User may create an Attempt only when all required eligibility conditions are satisfied:

```text
Quiz accessible
      ↓
Published Revision available
      ↓
Quiz active
      ↓
Prerequisite / unlock satisfied
      ↓
No existing IN_PROGRESS Attempt
      ↓
Attempt quota available
      ↓
Create Attempt
      ↓
Consume quota
```

The exact prerequisite or unlock conditions have not yet been defined.

No separate prerequisite domain model is introduced here until those business conditions are established.

---

## 19. Resume Behavior

An `IN_PROGRESS` Attempt is persistent and may be resumed.

```text
Start
  ↓
IN_PROGRESS
  ↓
temporary interruption
  ↓
IN_PROGRESS
  ↓
resume
  ↓
continue
```

The time limit continues to run during the interruption.

Browser closure, leaving the page, or temporary network loss does not pause the timer.

If the deadline has passed, the Attempt becomes `EXPIRED` and cannot be resumed.

---

## 20. Quiz Revision and Active Attempts

A new Quiz Revision may be published while a User has an `IN_PROGRESS` Attempt.

The existing Attempt remains associated with the original Revision.

```text
Revision 1
    ↓
Attempt 1 → IN_PROGRESS
    │
    │ Revision 2 published
    ↓
Attempt 1 → still bound to Revision 1
```

The new Revision affects future Attempts, not the already-created Attempt.

The User may be informed that the Quiz has been updated.

If the Quiz is used as a prerequisite for progressing to a new learning stage, the business may require the User to satisfy the prerequisite using the applicable current Revision.

The exact mechanism for determining learning-stage eligibility is outside the Quiz aggregate and remains to be defined.

---

## 21. Relationship to Learning Structure

Quiz placement is handled by `LessonItem`.

Current domain relationship:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
```

`LessonItem` is responsible for the Quiz's position within Lesson structure.

The Quiz aggregate is responsible for:

* Quiz identity;
* visibility;
* availability;
* Revision lifecycle;
* assessment definitions;
* Questions;
* Answers.

The QuizAttempt aggregate is responsible for:

* learner-specific execution;
* User Answers;
* Question Results;
* Attempt lifecycle;
* score;
* Assessment Result.

This avoids placing the Quiz aggregate inside the Course or Lesson aggregate.

---

## 22. Aggregate Boundary Summary

| Concept          | Boundary              | Responsibility                                          |
| ---------------- | --------------------- | ------------------------------------------------------- |
| Course           | Course Aggregate      | Learning structure                                      |
| Lesson           | Course Aggregate      | Lesson structure                                        |
| LessonItem       | Course Aggregate      | Placement of learning content, including Quiz reference |
| Quiz             | Quiz Aggregate        | Quiz identity, governance, and revision lifecycle       |
| QuizRevision     | Quiz Aggregate        | Versioned assessment definition                         |
| Question         | Quiz Aggregate        | Question definition                                     |
| AnswerQuestion   | Quiz Aggregate        | Answer definition                                       |
| QuizAttempt      | QuizAttempt Aggregate | Learner-specific assessment execution                   |
| UserAnswer       | QuizAttempt Aggregate | Current learner response                                |
| QuestionResult   | QuizAttempt Aggregate | Per-question assessment evidence                        |
| AssessmentResult | QuizAttempt Aggregate | Pass/fail assessment outcome                            |
| Best Score       | Derived               | Highest historical score for User × Published Revision  |

The important distinction is:

```text
Aggregate ≠ Module ≠ Bounded Context ≠ Database Table
```

The Quiz and QuizAttempt aggregates are separate because their lifecycles and consistency boundaries are different.

---

## 23. Domain Invariants

The following invariants define the core target model:

1. A Quiz is an independent Aggregate Root.
2. A Quiz is placed in a Lesson through a `LessonItem` reference.
3. Quiz visibility is separate from Quiz availability.
4. Quiz visibility is not part of an individual Revision.
5. A published Quiz Revision cannot be modified in place.
6. Modifications to a published Quiz definition create a new Draft Revision.
7. At most one Draft Revision exists for a Quiz.
8. At most one Published Revision exists for a Quiz.
9. Historical revisions are retained for existing Attempts.
10. A new Quiz starts with a Draft Revision.
11. An Attempt must start from a Published Revision.
12. An Attempt remains bound to the exact Revision used at start.
13. A new Revision does not change an existing Attempt.
14. Only one Attempt may be `IN_PROGRESS` for a User and Quiz at a time.
15. An `IN_PROGRESS` Attempt may be resumed.
16. The Attempt time limit continues to run in real time.
17. Reaching the deadline terminates the Attempt as `EXPIRED`.
18. An expired Attempt cannot be resumed.
19. A User may intentionally cancel their own `IN_PROGRESS` Attempt.
20. A cancelled Attempt is terminal.
21. A cancelled Attempt does not return consumed quota.
22. Creating an Attempt consumes one attempt quota immediately after successful creation.
23. Retry creates a new Attempt.
24. `PASSED` and `FAILED` are Assessment Results, not Attempt Statuses.
25. `passingScore` is optional.
26. A Revision without `passingScore` has no pass/fail Assessment Result.
27. Assessment Result is determined only when an Attempt is submitted or expired.
28. A cancelled Attempt does not produce an Assessment Result.
29. Score is calculated according to the exact Revision associated with the Attempt.
30. `totalScore` equals the sum of QuestionResult earned scores.
31. Question scoring uses full-score or zero-score evaluation.
32. Multiple-choice Questions require an exact selected-answer set for full credit.
33. There is no partial credit.
34. There is no negative marking.
35. Every Question produces a QuestionResult when an Attempt reaches an assessment-ending state.
36. An unanswered Question receives zero earned score.
37. User Answers may be changed while an Attempt is `IN_PROGRESS`.
38. Clearing an answer returns the Question to an unanswered state.
39. Skipping is navigation behavior and is not a persisted answer state.
40. Answers from one Attempt are not carried over into another Attempt.
41. Best Score is derived across historical Attempts and is not stored as an individual Attempt field.
42. Assessment Result must not be treated as Competency or Learner Current Level.

---

## 24. Current Implementation vs Target Model

The current implementation provides part of this model but does not yet implement the complete revision-based design.

### 24.1 Confirmed Current Implementation

The current source contains:

* `Quiz` as an Aggregate Root:
  `src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java`
* `QuizAttempt` as a separate Aggregate Root:
  `src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java`
* `Question`:
  `src/main/java/com/deutschhub/domain/learning/model/entity/Question.java`
* `AnswerQuestion`:
  `src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java`
* `UserAnswer`:
  `src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java`
* Quiz placement through `LessonItem.quizId`:
  `src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java`

The current `Quiz` implementation already contains concepts corresponding to:

```text
Quiz
├── questions
├── maxScore
├── passingScore
├── timeLimit
├── status
└── visibility
```

The current `QuizAttempt` implementation contains:

```text
QuizAttempt
├── quizId
├── userId
├── answers
├── totalScore
├── status
└── startedAt / submittedAt
```

The current `AttemptStatus` contains:

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

in:

```text
src/main/java/com/deutschhub/domain/learning/model/enums/AttemptStatus.java
```

The current `QuestionType` contains:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

in:

```text
src/main/java/com/deutschhub/domain/learning/model/enums/QuestionType.java
```

### 24.2 Target Concepts Not Yet Present

The current source does not yet contain a `QuizRevision` entity/model.

The current `Quiz` implementation stores Questions directly:

```text
Quiz
└── Question
```

rather than the target:

```text
Quiz
└── QuizRevision
    └── Question
```

The current `QuizAttempt` does not yet store an explicit reference to a Quiz Revision.

The current source also does not yet contain:

* `QuestionResult`;
* a separate `AssessmentResult` concept;
* `CompletionPolicy`;
* the target attempt-eligibility workflow;
* the target immediate quota-consumption model;
* a persisted Best Score concept.

Therefore, these are target domain decisions rather than descriptions of the current implementation.

---

## 25. Known Current-Model Inconsistencies

The current source contains an implementation inconsistency that should be addressed when the Quiz model is evolved:

`Quiz.createDraft(UUID courseId, UUID createdBy)` passes `courseId` into the constructor field named `lessonId`.

Location:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

This conflicts with the agreed business relationship that Quiz is associated with a Lesson through `LessonItem`.

The target model therefore does not treat Quiz as directly owned by Lesson or Course.

The current implementation also does not yet implement the target Revision-based model described in this document.

Other implementation details are intentionally left for the later implementation/design stage rather than being solved in this domain model document.
