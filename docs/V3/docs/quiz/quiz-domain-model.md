# Quiz Domain Model

## 1. Purpose

This document defines the target domain model and aggregate boundaries for the Quiz domain within the Learning context.

The model is based on the current DeutschHub implementation and the business decisions established during domain discovery.

The target model separates:

- Quiz definition from Quiz execution.
- Quiz revisions from historical Quiz definitions.
- Assessment configuration from learner answers.
- Attempt lifecycle from assessment result.
- Quiz placement within a Lesson from the Quiz aggregate itself.

---

## 2. Domain Position

Quiz belongs to the Learning domain.

A Quiz is used as an assessment or learning activity and may be placed inside a Lesson through a `LessonItem`.

The `LessonItem` does not contain the Quiz aggregate. It only references the Quiz by identifier.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
````

This keeps learning structure and assessment definition as separate domain concepts.

---

## 3. Quiz Aggregate

### 3.1 Aggregate Root

`Quiz` is an independent Aggregate Root.

The Quiz aggregate owns the definition and lifecycle of a Quiz, including its revisions and assessment configuration.

Target conceptual structure:

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── AnswerQuestion
```

A Quiz is not contained inside `Course`, `Lesson`, or `LessonItem`.

---

## 4. Quiz Revision

### 4.1 Purpose

A `QuizRevision` represents a specific immutable version of a Quiz definition.

A revision contains the assessment definition that is used when a QuizAttempt is started.

A published revision must not be modified in place.

Any modification to a published Quiz must be performed through a new Draft Revision and explicitly published as a new definition.

### 4.2 Revision Lifecycle

At most one Draft Revision and one Published Revision may exist for a Quiz at the same time.

A Quiz may therefore have the following states of revision availability:

```text
New Quiz
└── Draft Revision

After first publication
└── Published Revision

After editing
├── Published Revision
└── Draft Revision

After publishing the new draft
├── New Published Revision
└── Previous Published Revision → historical
```

Historical revisions are retained because existing QuizAttempts must remain associated with the exact assessment definition used when they started.

The exact representation of historical revision status remains an implementation detail to be determined later.

---

## 5. Quiz Revision Content

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
├── difficulty
├── visibility
└── questions
    └── Question
        └── AnswerQuestion
```

### 5.1 Assessment Configuration

#### Time Limit

`timeLimit` defines the maximum real-world duration of an Attempt.

The timer starts when the Attempt starts and continues to run regardless of:

* temporary network disconnection;
* browser closure;
* leaving the Quiz temporarily.

A user may resume an `IN_PROGRESS` Attempt, but the elapsed time is not paused.

#### Maximum Score

`maxScore` defines the maximum score defined for the Quiz Revision.

The total score of the Questions must match the Revision's maximum score before publication.

#### Passing Score

`passingScore` is optional.

A Quiz may define a passing score when the business requires pass/fail evaluation.

Practice or learning-oriented Quizzes may omit a passing score.

When no passing score exists:

```text
Assessment Result = none
```

The Attempt may still produce a numeric score.

When a passing score exists:

```text
totalScore >= passingScore → PASSED
totalScore < passingScore  → FAILED
```

The passing score belongs to the Quiz Revision so that historical Attempts continue to use the assessment rules that were effective when they started.

#### Maximum Attempts

`maxAttempts` defines the maximum number of Attempts a User may consume for the Quiz under the applicable Quiz Revision.

The value may differ between Quizzes according to their business purpose.

Examples:

```text
Practice Quiz
maxAttempts = 10

Formal Assessment
maxAttempts = 1

Other Assessment
maxAttempts = 3
```

A cancelled Attempt does not consume an attempt quota.

The exact authorization for managing Quiz configuration is not defined in this document.

---

## 6. Question Entity

`Question` is an entity within the Quiz Revision.

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

Supported question types are currently:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

### 6.1 Question Constraints

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

## 7. AnswerQuestion Entity

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

## 8. QuizAttempt Aggregate

`QuizAttempt` is a separate Aggregate Root from `Quiz`.

It represents one learner-specific execution of a Quiz.

Target relationship:

```text
Quiz
└── QuizRevision
        ↑
        │ exact revision
        │
QuizAttempt
├── UserAnswer
├── status
├── totalScore
└── assessmentResult?
```

The Attempt is not contained inside the Quiz aggregate.

---

## 9. Attempt-to-Revision Relationship

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
├── Revision 1
└── Revision 2 ← current Published Revision

QuizAttempt 1
└── → Revision 1
```

The existing Attempt does not switch to Revision 2.

Its:

* Questions;
* Answer definitions;
* Question scores;
* Maximum score;
* Passing score;
* Attempt policy;
* Other assessment rules

remain determined by the revision from which the Attempt started.

This guarantees assessment consistency and preserves historical assessment integrity.

---

## 10. QuizAttempt Lifecycle

The Attempt lifecycle is represented separately from its assessment result.

### 10.1 Attempt Status

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

### 10.2 Status Meaning

#### IN_PROGRESS

The User is currently working on the Attempt.

An `IN_PROGRESS` Attempt can be resumed after:

* leaving the Quiz;
* closing the browser;
* losing network connectivity.

These events do not automatically terminate the Attempt.

Only one `IN_PROGRESS` Attempt is allowed for the same User and Quiz at a time.

#### SUBMITTED

The User has completed and submitted the Attempt.

The score is calculated when the Attempt is submitted.

#### EXPIRED

The Attempt has reached its time limit.

The time limit is based on real elapsed time from the Attempt's start.

An expired Attempt is terminal and cannot be resumed.

If the Quiz has a passing score, an expired Attempt results in:

```text
EXPIRED
    +
passingScore exists
    ↓
FAILED
```

If the Quiz has no passing score, the Attempt is simply:

```text
EXPIRED
```

with no `PASSED` / `FAILED` assessment result.

#### CANCELLED

The Attempt is intentionally cancelled.

An Attempt may be cancelled by:

* the User;
* the System;
* an authorized Admin/Instructor.

Accidental browser closure or temporary network loss does not automatically cause cancellation.

A cancelled Attempt does not consume the Quiz's maximum attempt quota.

---

## 11. Assessment Result

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

Assessment Result is determined only when the Attempt reaches a terminal assessment outcome.

For a Quiz with a passing score:

```text
SUBMITTED
    ↓
calculate totalScore
    ↓
PASSED / FAILED
```

For a timed Quiz:

```text
EXPIRED
    ↓
FAILED
```

only when a passing score is defined.

A Quiz without a passing score does not produce a pass/fail result.

Assessment Result must not be interpreted as Competency or Learner Current Level.

---

## 12. UserAnswer Entity

`UserAnswer` belongs to a QuizAttempt.

It represents the User's selected answers for one Question.

```text
QuizAttempt
└── UserAnswer
```

A UserAnswer contains:

* answer identifier;
* Question identifier;
* selected answer identifiers;
* correctness information.

The exact persistence and lifecycle policy for answers after an expired Attempt is not defined here.

However, answers from an expired Attempt must not be carried over into a new Attempt.

A new Attempt starts with its own answer set.

---

## 13. Attempt Limits and Retry

A Quiz may allow multiple Attempts.

The maximum number of Attempts is determined by the Quiz's applicable assessment policy.

Example:

```text
maxAttempts = 3

Attempt 1 → SUBMITTED → FAILED
Attempt 2 → CANCELLED
Attempt 3 → SUBMITTED → FAILED
```

The cancelled Attempt does not consume a quota.

A User may have multiple historical Attempts for the same Quiz, but only one Attempt may be `IN_PROGRESS` for that Quiz at a time.

---

## 14. Resume Behavior

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

If the deadline is reached, the Attempt becomes expired and cannot be resumed.

---

## 15. Quiz Revision and Active Attempts

A Quiz Revision may change while a User has an `IN_PROGRESS` Attempt.

The existing Attempt remains associated with the original revision.

```text
Revision 1
    ↓
Attempt 1 → IN_PROGRESS
    │
    │ Revision 2 published
    ↓
Attempt 1 → still bound to Revision 1
```

The User may be informed that the Quiz has been updated.

For Quizzes that serve as prerequisites for progressing to a new learning stage, the business may require the User to retake the Quiz using the new Revision.

This does not change the historical identity of the existing Attempt.

The exact mechanism for determining learning-stage eligibility is outside the Quiz aggregate and remains to be defined.

---

## 16. Relationship to Learning Structure

Quiz placement is handled by `LessonItem`.

Current domain structure:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
```

`LessonItem` is responsible for the Quiz's position within Lesson structure.

The Quiz aggregate remains responsible for:

* assessment definition;
* Quiz revisions;
* Questions;
* Answers;
* assessment configuration.

This avoids placing the Quiz aggregate inside the Course or Lesson aggregate.

---

## 17. Aggregate Boundary Summary

| Concept          | Boundary              | Responsibility                                          |
| ---------------- | --------------------- | ------------------------------------------------------- |
| Course           | Course Aggregate      | Learning structure                                      |
| Lesson           | Course Aggregate      | Lesson structure                                        |
| LessonItem       | Course Aggregate      | Placement of learning content, including Quiz reference |
| Quiz             | Quiz Aggregate        | Assessment definition                                   |
| QuizRevision     | Quiz Aggregate        | Versioned assessment definition                         |
| Question         | Quiz Aggregate        | Question definition                                     |
| AnswerQuestion   | Quiz Aggregate        | Answer definition                                       |
| QuizAttempt      | QuizAttempt Aggregate | Learner-specific assessment execution                   |
| UserAnswer       | QuizAttempt Aggregate | Learner response                                        |
| AssessmentResult | QuizAttempt           | Outcome of the assessment                               |

The important distinction is:

```text
Aggregate ≠ Module ≠ Bounded Context ≠ Database Table
```

The Quiz and QuizAttempt aggregates are separate because their lifecycles and consistency boundaries are different.

---

## 18. Domain Invariants

The following invariants define the core target model:

1. A Quiz is an independent Aggregate Root.
2. A Quiz is placed in a Lesson through a `LessonItem` reference.
3. A published Quiz definition cannot be modified in place.
4. Modifications to a published Quiz create a new Draft Revision.
5. At most one Draft Revision exists for a Quiz.
6. At most one Published Revision exists for a Quiz.
7. Historical revisions are retained for existing Attempts.
8. An Attempt must start from a Published Revision.
9. An Attempt remains bound to the exact Revision used at start.
10. A Quiz may allow multiple Attempts according to its `maxAttempts` policy.
11. Only one Attempt may be `IN_PROGRESS` for a User and Quiz at a time.
12. An `IN_PROGRESS` Attempt may be resumed.
13. The Attempt time limit continues to run in real time.
14. Reaching the deadline terminates the Attempt as `EXPIRED`.
15. An intentionally cancelled Attempt becomes `CANCELLED`.
16. A `CANCELLED` Attempt does not consume attempt quota.
17. `PASSED` and `FAILED` are assessment results, not Attempt statuses.
18. `passingScore` is optional.
19. A Quiz without `passingScore` has no pass/fail assessment result.
20. Score and assessment result are calculated according to the exact Revision associated with the Attempt.
21. An expired Attempt cannot be resumed.
22. Answers from an expired Attempt are not carried over into a new Attempt.
23. Assessment Result must not be used as a direct representation of Competency or Learner Current Level.

---

## 19. Current Implementation vs Target Model

The current implementation provides part of this model but does not yet implement the complete target revision-based design.

### Confirmed Current Implementation

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

The current implementation also already contains the basic concepts of:

```text
Quiz
├── Questions
├── maxScore
├── passingScore
├── timeLimit
├── status
└── visibility

QuizAttempt
├── answers
├── totalScore
├── status
└── startedAt / submittedAt
```

### Target Concepts Not Yet Present in the Current Model

The current source does not yet contain a `QuizRevision` entity/model.

The current `Quiz` implementation stores Questions directly and currently exposes:

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

The current `QuizAttempt` stores `quizId` but does not yet store an explicit reference to an exact Quiz Revision.

The current implementation also does not yet contain a separate `AssessmentResult` concept.

Therefore, these are target domain decisions rather than descriptions of the current implementation.

---

## 20. Known Current-Model Inconsistencies

The current source contains an implementation inconsistency that should be addressed when the Quiz revision model is implemented:

`Quiz.createDraft(UUID courseId, UUID createdBy)` passes `courseId` into the constructor field named `lessonId`.

Location:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

This conflicts with the agreed business relationship that Quiz is associated with a Lesson through `LessonItem`.

The target model therefore does not treat Quiz as directly owned by Lesson or Course.

Other implementation details are intentionally left for the later implementation/design stage rather than being solved in this domain model document.
