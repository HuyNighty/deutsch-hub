# Quiz Domain Model

## 1. Purpose

This document defines the target domain model and aggregate boundaries for the Quiz domain within the Learning Context.

The model is based on:

- the current DeutschHub implementation;
- the target Learning domain model;
- the Quiz business decisions established during domain discovery.

The target model separates:

- Quiz identity and governance from Quiz Revision content;
- current assessment definitions from historical assessment definitions;
- assessment definition from learner-specific assessment execution;
- Attempt lifecycle from Assessment Result;
- learner answers from assessment evidence;
- Quiz placement within a Lesson from the Quiz Aggregate itself.

This document describes the target domain structure and relationships.

Detailed business rules and business actions are defined separately in:

- `quiz-business-rules.md`
- `quiz-business-actions.md`

---

## 2. Domain Position

Quiz belongs to the Learning Context.

A Quiz is an assessment domain concept that may be placed within a Lesson through a `LessonItem`.

The `LessonItem` references the Quiz by identity rather than containing the Quiz Aggregate.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
````

This keeps Learning Structure and Assessment Definition as separate domain concepts.

The Quiz Aggregate does not own:

* Course;
* Section;
* Lesson;
* LessonItem.

---

# 3. Quiz Aggregate

## 3.1 Aggregate Root

`Quiz` is an Aggregate Root.

The Quiz Aggregate owns the stable identity, ownership, governance, and Revision lifecycle of an assessment.

Target conceptual structure:

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── Answer
```

`QuizRevision` is an Entity inside the Quiz Aggregate.

`Question` is an Entity inside a QuizRevision.

`Answer` is an Entity inside a Question.

The Quiz Aggregate is therefore the consistency boundary for the assessment definition and its revisions.

---

## 3.2 Quiz Responsibilities

The Quiz Aggregate is responsible for concepts that belong to the Quiz itself, including:

* Quiz identity;
* ownership;
* governance;
* visibility;
* availability;
* Revision lifecycle;
* the relationship between the Quiz and its Revisions.

The Quiz Aggregate does not own learner-specific execution.

Learner-specific execution belongs to `QuizAttempt`.

Conceptually:

```text
Quiz
├── Identity
├── Ownership
├── Governance
├── Visibility
├── Availability
└── Revision Lifecycle
     └── QuizRevision
          ├── Definition
          ├── Questions
          │    └── Answers
          └── Assessment Rules
```

---

# 4. Quiz Revision

## 4.1 Purpose

`QuizRevision` represents a concrete version of a Quiz assessment definition.

A Revision contains the assessment definition used when a QuizAttempt is started.

Revision-specific concepts include:

* title;
* description;
* difficulty;
* time limit;
* maximum score;
* passing percentage;
* maximum attempts;
* completion policy;
* Questions;
* Answers.

A Published Revision is immutable.

A Historical Revision is immutable.

Any modification to an already Published assessment definition is performed through a new Draft Revision.

---

## 4.2 Revision Lifecycle

The target Revision lifecycle is:

```text
DRAFT
   ↓
IN_REVIEW
   ↓
PUBLISHED
   ↓
HISTORICAL
```

A Quiz may have:

* at most one Draft Revision;
* at most one Revision in review;
* at most one Published Revision;
* zero or more Historical Revisions.

The exact persistence representation of Revision lifecycle states is outside the scope of this document.

Conceptually:

```text
New Quiz
└── Draft Revision
```

After publication:

```text
Quiz
└── Published Revision
```

While a new definition is being prepared:

```text
Quiz
├── Published Revision
└── Draft Revision
```

During review:

```text
Quiz
├── Published Revision
└── Revision in Review
```

After the new Revision is published:

```text
Quiz
├── New Published Revision
└── Previous Published Revision → Historical
```

Historical Revisions are retained because existing QuizAttempts remain bound to the exact Revision used when they started.

---

## 4.3 Draft Revision

A Draft Revision is mutable.

It may be incomplete while being edited.

A Draft Revision may contain:

* incomplete Questions;
* incomplete Answers;
* temporary assessment configuration.

Completeness and publishability are validated before the Revision can become Published.

A Draft Revision may be submitted for review when the required review conditions are satisfied.

The detailed validation rules are defined in `quiz-business-rules.md`.

---

## 4.4 Published Revision

A Published Revision represents a concrete assessment definition that may be used for new QuizAttempts when the Quiz is otherwise available and the learner is eligible.

A Published Revision is immutable.

It defines the exact assessment configuration used by an Attempt, including:

```text
Questions
Answers
Question scores
Maximum score
Passing percentage
Time limit
Maximum attempts
Completion policy
```

A newer Revision does not modify an existing Published or Historical Revision.

---

## 4.5 Historical Revision

When a newer Revision becomes Published, the previous Published Revision becomes Historical.

Historical Revisions are retained.

They preserve the assessment definition against which existing Attempts were executed.

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED
```

Historical Revisions are not edited or republished in place.

---

# 5. Quiz Governance

Quiz governance is separate from Revision content.

## 5.1 Ownership

Quiz ownership contains two distinct concepts:

```text
createdBy
author
```

`createdBy` represents the historical creator of the Quiz and is immutable.

`author` represents the current responsible author and may be changed through the appropriate business operation.

Changing the current author does not create a new Revision.

The exact authorization policy is defined separately from the domain structure.

---

## 5.2 Visibility

Visibility determines the high-level access mode of the Quiz.

The supported visibility concepts are:

```text
PRIVATE
PUBLIC
COURSE_ONLY
```

Visibility is a property of the Quiz rather than an individual Revision.

Conceptually:

```text
Quiz
├── Visibility
└── Availability
```

Visibility is distinct from:

* Access Requirements;
* Availability;
* Learning Prerequisites;
* Current Level.

For example:

```text
PRIVATE
→ ordinary learners cannot access the Quiz

PUBLIC
→ learner may access the Quiz, subject to eligibility

COURSE_ONLY
→ valid Course learning access is required
```

The Quiz Aggregate does not directly own Course access state.

---

## 5.3 Availability

Availability determines whether the Quiz can currently accept new Attempts.

Availability is distinct from:

* Quiz lifecycle;
* Revision lifecycle;
* Visibility;
* Attempt lifecycle.

The target availability concept supports:

```text
ACTIVE
INACTIVE
```

Deactivating a Quiz prevents creation of new Attempts but does not automatically terminate existing `IN_PROGRESS` Attempts.

Availability therefore controls whether a new Attempt may be started rather than whether an existing Attempt may continue.

---

## 5.4 Quiz Lifecycle

The Quiz lifecycle is:

```text
ACTIVE
   ↓
ARCHIVED
   ↓
DELETED
```

The lifecycle is separate from Revision lifecycle and Availability.

Conceptually:

```text
Quiz Lifecycle
    ACTIVE
      ↓
   ARCHIVED
      ↓
    DELETED

Revision Lifecycle
    DRAFT
      ↓
  IN_REVIEW
      ↓
  PUBLISHED
      ↓
  HISTORICAL

Availability
    ACTIVE / INACTIVE
```

Archiving a Quiz prevents new Attempts but does not terminate existing `IN_PROGRESS` Attempts.

Deleting a Quiz is a soft-delete operation.

Deletion preserves Historical Revisions and terminal QuizAttempts.

---

# 6. Quiz Revision Content

A QuizRevision contains the concrete assessment definition.

Conceptually:

```text
QuizRevision
├── title
├── description
├── difficulty
├── timeLimit
├── maxScore
├── passingPercentage?
├── maxAttempts?
├── completionPolicy
└── questions
    └── Question
        └── Answer
```

---

## 6.1 Time Limit

`timeLimit` defines the maximum real-world duration of an Attempt.

It is optional:

```text
timeLimit = null
→ unlimited duration
```

When configured, it must be positive.

The Attempt expiration point is determined from the Attempt start time and the time limit of the exact Published Revision used by that Attempt.

The timer continues to run regardless of:

* browser closure;
* temporary network disconnection;
* leaving the Quiz;
* temporary interruption.

An `IN_PROGRESS` Attempt may be resumed, but elapsed time is not paused.

Server-side time is authoritative.

If the deadline has been reached when the server processes an operation, the Attempt is considered expired.

---

## 6.2 Maximum Score

`maxScore` is derived from the Question scores in the Revision.

```text
maxScore
=
sum(Question.score)
```

It is not an independent author-entered source of truth.

A Draft Revision may temporarily contain no Questions and therefore have a maximum score of zero.

A Published Revision must contain at least one valid Question and therefore has a positive maximum score.

The maximum score is preserved through the exact Revision associated with an Attempt.

---

## 6.3 Passing Percentage

`passingPercentage` is optional.

```text
passingPercentage = null
→ no PASSED / FAILED Assessment Result
```

When configured:

```text
0 < passingPercentage ≤ 100
```

The Assessment Result is determined from the actual score percentage:

```text
scorePercentage
=
(totalScore / maxScore) × 100
```

Then:

```text
scorePercentage >= passingPercentage
→ PASSED

scorePercentage < passingPercentage
→ FAILED
```

Passing is therefore percentage-based rather than based on a fixed stored passing score.

The exact calculation and comparison rule are defined in `quiz-business-rules.md`.

---

## 6.4 Maximum Attempts

`maxAttempts` defines the maximum number of Attempts that may be created by a User for a Published QuizRevision.

The quota is scoped to:

```text
User × Published QuizRevision
```

When `maxAttempts` is `null`, the number of Attempts is unlimited.

A quota is consumed immediately when a new Attempt is successfully created.

Therefore:

```text
Create Attempt
      ↓
Quota consumed
      ↓
IN_PROGRESS
      ↓
SUBMITTED / EXPIRED / CANCELLED
```

A cancelled Attempt does not return consumed quota.

---

## 6.5 Completion Policy

`completionPolicy` defines the rule for manual submission.

Supported concepts are:

```text
REQUIRED_ALL
OPTIONAL
```

### REQUIRED_ALL

All Questions must have an answer before manual submission is accepted.

A learner may temporarily leave Questions unanswered while the Attempt remains `IN_PROGRESS`.

If the time limit expires, unanswered Questions remain unanswered and receive zero score.

The Attempt becomes `EXPIRED` and is evaluated.

### OPTIONAL

Manual submission is allowed even when some Questions remain unanswered.

Unanswered Questions receive zero score.

Completion Policy is part of the Revision because it is an assessment rule used by the Attempt.

---

# 7. Question Entity

`Question` is an Entity within a QuizRevision.

Target relationship:

```text
Quiz
└── QuizRevision
    └── Question
```

A Question contains:

* Question identity;
* learner-facing prompt;
* score;
* Question type;
* ordered Answers.

Supported Question types are:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

No additional Question types are introduced by the current target model.

---

## 7.1 Question Lifecycle and Mutability

A Draft Question is mutable.

A Draft Question may temporarily be incomplete.

For example, during editing it may temporarily have:

```text
0 Answers
0 correct Answers
```

These temporary states do not make the Draft invalid as an editable object.

Before a Revision becomes Published, each Question must satisfy the required structural and evaluation invariants.

Published and Historical Questions are immutable because their definition contributes to the historical meaning of the Revision.

---

## 7.2 Question Constraints

For a publishable Question, the target model requires:

* Question score is greater than zero.
* Answer content is required.
* Duplicate Answer content is not allowed within the same Question.
* Answer order is unique and contiguous, starting from 1.
* `SINGLE_CHOICE` has exactly one correct Answer.
* `MULTIPLE_CHOICE` has at least one correct Answer.
* `TRUE_FALSE` has exactly two Answers.
* `TRUE_FALSE` has exactly one correct Answer.

A Question may change type while its Revision is Draft.

Changing the Question type does not automatically modify its existing Answers or correctness configuration.

The Question must satisfy the invariants of its final type before publication.

The exact content length, formatting, normalization, and case-sensitivity rules remain separate business-rule concerns where not yet explicitly defined.

---

# 8. Answer Entity

`Answer` is an Entity owned by a Question.

Target relationship:

```text
Quiz
└── QuizRevision
    └── Question
        └── Answer
```

An Answer contains conceptually:

* Answer identity;
* Answer content;
* correctness state;
* presentation order.

---

## 8.1 Answer Identity

Answer identity is stable within the domain.

Presentation labels such as:

```text
A
B
C
D
```

are presentation concerns rather than Answer identity.

An Answer remains the same domain entity when its presentation position changes.

---

## 8.2 Answer Order

Answer order is domain data.

Within a Question:

```text
1
2
3
4
```

must remain:

* unique;
* contiguous;
* ordered from 1.

Adding, removing, or reordering Answers may cause the order values to be normalized while the Revision remains Draft.

---

## 8.3 Correctness

`isCorrect` belongs to the Answer.

The Question enforces the collection-level correctness invariants according to its Question type.

No separate `CorrectAnswerConfiguration` abstraction is introduced in the target model.

---

# 9. QuizAttempt Aggregate

`QuizAttempt` is a separate Aggregate Root from `Quiz`.

It represents one learner-specific execution of one exact Published QuizRevision.

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

The Attempt is not contained inside the Quiz Aggregate.

The Attempt owns learner-specific execution state and the assessment evidence produced from that execution.

---

# 10. Attempt-to-Revision Relationship

When a User starts a QuizAttempt, the Attempt is bound to the exact Published QuizRevision used at the time of start.

Conceptually:

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED

Attempt 1
└── revisionId → Revision A

Attempt 2
└── revisionId → Revision B
```

An Attempt does not dynamically follow the latest Published Revision.

The Attempt therefore evaluates against the Revision that was effective when the Attempt started.

This includes:

* Questions;
* Answers;
* Question scores;
* maximum score;
* passing percentage;
* completion policy;
* attempt policy;
* time limit;
* other Revision-specific assessment rules.

Publishing a newer Revision does not alter an existing Attempt.

An `IN_PROGRESS` Attempt may continue and submit against its original Revision.

---

# 11. QuizAttempt Lifecycle

The Attempt lifecycle is represented separately from Assessment Result.

## 11.1 Attempt Status

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

---

## 11.2 IN_PROGRESS

The User is currently working on the Attempt.

An `IN_PROGRESS` Attempt may be resumed after:

* leaving the Quiz;
* closing the browser;
* losing network connectivity;
* other temporary interruptions.

These events do not automatically terminate the Attempt.

Only one `IN_PROGRESS` Attempt is allowed for the same User and Quiz at a time.

---

## 11.3 SUBMITTED

The User has completed and submitted the Attempt.

When submission is accepted:

* the Attempt becomes `SUBMITTED`;
* all Questions are evaluated;
* QuestionResults are produced;
* `totalScore` is calculated;
* Assessment Result is determined when `passingPercentage` is configured.

---

## 11.4 EXPIRED

The Attempt has reached its time limit.

Expiration is based on real elapsed time from the Attempt's start.

An expired Attempt is terminal and cannot be resumed.

All Questions are evaluated.

Unanswered Questions receive:

```text
isCorrect = null
earnedScore = 0
```

If the Revision has a passing percentage, the final Assessment Result is determined from the calculated score percentage.

Therefore:

```text
AttemptStatus = EXPIRED
```

does not inherently mean:

```text
AssessmentResult = FAILED
```

An expired Attempt may pass if its score satisfies the passing percentage.

---

## 11.5 CANCELLED

The User may intentionally cancel their own `IN_PROGRESS` Attempt.

Cancellation is terminal.

A cancelled Attempt:

* cannot be resumed;
* cannot be submitted;
* does not produce QuestionResults;
* does not produce an Assessment Result;
* does not return consumed attempt quota.

Browser closure or temporary network loss does not automatically cause cancellation.

---

# 12. UserAnswer Entity

`UserAnswer` belongs to a QuizAttempt.

It represents the current response of the User to one Question.

Conceptually:

```text
QuizAttempt
└── UserAnswer
```

A UserAnswer contains:

* Question identity;
* selected Answer identities;
* current response state.

The target model allows:

```text
One current UserAnswer
per Question
per Attempt
```

A User may change an answer while the Attempt is `IN_PROGRESS`.

A User may clear an answer and return the Question to an unanswered state.

Clearing the current answer means:

```text
selectedAnswerIds = empty
```

Skipping a Question is a navigation action.

It is not represented as:

```text
QuestionStatus.SKIPPED
```

and does not create a special persisted skipped-answer state.

There is no answer history in the target model.

Answers from one Attempt are not carried over into another Attempt.

Correctness is not evaluated while the Attempt remains `IN_PROGRESS`.

---

# 13. Question Result

`QuestionResult` represents the historical assessment evidence produced for one Question when an Attempt reaches an assessment-ending state.

Target structure:

```text
QuizAttempt
└── QuestionResult
    ├── questionId
    ├── selectedAnswerIds
    ├── responseStatus
    ├── isCorrect
    └── earnedScore
```

A QuestionResult is produced for every Question in the bound Published Revision, including unanswered Questions.

---

## 13.1 Response Status

The target response statuses are:

```text
ANSWERED
UNANSWERED
```

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
→ selectedAnswerIds = empty
→ isCorrect = null
→ earnedScore = 0
```

`QuestionResult` is historical assessment evidence.

It is not:

* Competency;
* Learner Current Level;
* Learning Progress.

---

# 14. Scoring

Each Question has a fixed positive score.

The target scoring model is:

```text
SINGLE_CHOICE
→ correct = full Question score
→ incorrect = 0

TRUE_FALSE
→ correct = full Question score
→ incorrect = 0

MULTIPLE_CHOICE
→ exact selected set matches correct set = full Question score
→ otherwise = 0
```

There is:

* no partial credit;
* no negative marking.

For an evaluated Attempt:

```text
totalScore
=
sum(QuestionResult.earnedScore)
```

Each Question therefore contributes either:

```text
0
```

or:

```text
Question.score
```

The target model does not persist a separate score percentage as an independent historical fact.

Score percentage is derived from:

```text
(totalScore / QuizRevision.maxScore) × 100
```

using the exact Revision associated with the Attempt.

---

# 15. Assessment Result

Assessment Result is distinct from Attempt Status.

```text
Attempt Status
├── IN_PROGRESS
├── SUBMITTED
├── EXPIRED
└── CANCELLED

Assessment Result
├── PASSED
├── FAILED
└── NONE
```

`AttemptStatus` answers:

> What is the lifecycle state of this Attempt?

`AssessmentResult` answers:

> Did the evaluated assessment satisfy its passing requirement?

---

## 15.1 Result Determination

Assessment Result is determined exactly once when an Attempt reaches an assessment-ending state:

```text
SUBMITTED
or
EXPIRED
```

When `passingPercentage` is configured:

```text
scorePercentage >= passingPercentage
→ PASSED

scorePercentage < passingPercentage
→ FAILED
```

When `passingPercentage` is `null`:

```text
AssessmentResult = NONE
```

A `CANCELLED` Attempt does not produce an Assessment Result.

The Assessment Result is historical and immutable.

It is not recalculated against a later QuizRevision.

---

## 15.2 Assessment Result Is Not Learner State

Assessment Result must not be interpreted directly as:

```text
Competency
Current Level
Learning Progress
```

It is the outcome of one specific assessment execution against one specific Published Revision.

---

# 16. Best Score

Best Score is a derived concept across historical Attempts.

It is not a field owned by an individual QuizAttempt.

For a User and a Published QuizRevision:

```text
Best Score
=
MAX(totalScore)
across eligible evaluated Attempts
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

Cancelled Attempts are not evaluated Attempts and therefore do not contribute to Best Score.

All historical Attempts remain preserved.

The exact query or read-model mechanism for obtaining Best Score is outside this domain model.

---

# 17. Attempt Limits and Retry

A QuizRevision may define a maximum number of Attempts through `maxAttempts`.

The quota applies to:

```text
User × Published QuizRevision
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

A User may have multiple historical Attempts for the same Revision, but only one `IN_PROGRESS` Attempt is allowed for the same User and Quiz at a time.

Whether a User may retry after a particular Assessment Result is governed by the applicable attempt policy and remaining quota.

---

# 18. Attempt Creation Eligibility

Creating a QuizAttempt is a business operation rather than simple object construction.

Conceptually:

```text
Quiz accessible
      ↓
Access Requirements satisfied
      ↓
Quiz ACTIVE
      ↓
Quiz Availability allows Start
      ↓
Published Revision available
      ↓
Learning Prerequisites satisfied
      ↓
No existing IN_PROGRESS Attempt
      ↓
Attempt quota available
      ↓
Create Attempt
      ↓
Consume quota
```

The following concepts are intentionally distinct:

```text
Visibility
Access Requirements
Availability
Learning Prerequisites
Current Level
Attempt Quota
```

Current Level is a soft recommendation or warning rather than a hard prerequisite.

A learner who receives a Current Level warning may continue, but only continuing creates the Attempt and consumes quota.

The exact prerequisite ownership and evaluation mechanism remain outside this domain model.

---

# 19. Resume Behavior

An `IN_PROGRESS` Attempt is persistent and may be resumed.

Conceptually:

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

Resuming an existing Attempt:

* does not create a new Attempt;
* does not consume another quota;
* does not re-run Start Attempt eligibility;
* does not switch the Attempt to another Revision.

The time limit continues to run during the interruption.

If the deadline has passed, the Attempt becomes `EXPIRED` and cannot be resumed.

The current Question being displayed is UI navigation state and is not a required domain state of the Attempt.

---

# 20. Quiz Revision and Active Attempts

A new QuizRevision may be published while a User has an `IN_PROGRESS` Attempt.

The existing Attempt remains associated with its original Revision.

```text
Revision 1
    ↓
Attempt 1 → IN_PROGRESS
    │
    │ Revision 2 published
    ↓
Attempt 1 → still bound to Revision 1
```

The new Revision affects future Attempts, not an already-created Attempt.

The User may be informed that the Quiz definition has changed.

Historical assessment evidence remains associated with the Revision under which the Attempt was executed.

---

# 21. Completion Requirement Relationship

A Quiz may be used as a Practice, Diagnostic, or Completion Requirement.

A Quiz is not inherently mandatory simply because it is part of the Learning Context.

A Completion Requirement may be associated with:

```text
Lesson
or
Section
```

A Completion Requirement references a specific Published QuizRevision.

The requirement is satisfied by at least one valid `PASSED` Attempt for that Revision.

Conceptually:

```text
Completion Requirement
        ↓
Published QuizRevision
        ↓
PASSED QuizAttempt
        ↓
Requirement satisfied
```

The latest Attempt does not need to be the passing Attempt.

A previously satisfied Completion Requirement is a historical completion fact.

Publishing a later QuizRevision does not automatically revoke or rewrite an already achieved completion.

Completion Requirements and Learning Prerequisites are separate domain concepts.

---

# 22. Learning Completion Impact

A QuizRevision may declare whether changes to its assessment definition affect learning completion.

The target concepts are:

```text
NO_IMPACT
AFFECTS_COMPLETION
```

The decision is explicitly made by the Content Author or Manager according to the applicable business operation.

`NO_IMPACT` means the Revision does not alter the validity of existing or future completion evaluation.

`AFFECTS_COMPLETION` means the Revision may affect completion evaluation for learners who have not yet completed the associated requirement.

A new Revision does not:

* delete historical evidence;
* rewrite historical Attempts;
* reset the entire Course;
* automatically invalidate an already achieved completion.

Historical completion remains a historical fact.

---

# 23. Relationship to Learning Structure

Quiz placement is handled by `LessonItem`.

The domain relationship is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
```

`LessonItem` is responsible for the Quiz's placement within Lesson structure.

The Quiz Aggregate is responsible for:

* Quiz identity;
* ownership;
* governance;
* visibility;
* availability;
* Quiz lifecycle;
* Revision lifecycle;
* assessment definitions;
* Questions;
* Answers.

The QuizAttempt Aggregate is responsible for:

* learner-specific execution;
* User Answers;
* Question Results;
* Attempt lifecycle;
* score;
* Assessment Result.

This keeps Learning Structure separate from Assessment Definition and Assessment Execution.

---

# 24. Aggregate Boundary Summary

| Concept          | Boundary              | Responsibility                                                  |
| ---------------- | --------------------- | --------------------------------------------------------------- |
| Quiz             | Quiz Aggregate        | Identity, ownership, governance, lifecycle                      |
| QuizRevision     | Quiz Aggregate        | Concrete versioned assessment definition                        |
| Question         | Quiz Aggregate        | Question definition and invariants                              |
| Answer           | Quiz Aggregate        | Answer definition and correctness                               |
| QuizAttempt      | QuizAttempt Aggregate | Learner-specific assessment execution                           |
| UserAnswer       | QuizAttempt Aggregate | Current learner response                                        |
| QuestionResult   | QuizAttempt Aggregate | Per-question assessment evidence                                |
| AssessmentResult | QuizAttempt Aggregate | Assessment outcome                                              |
| Best Score       | Derived               | Highest eligible historical score for User × Published Revision |

The important distinction is:

```text
Aggregate ≠ Module ≠ Bounded Context ≠ Database Table
```

The Quiz and QuizAttempt Aggregates are separate because their identities, lifecycles, responsibilities, and consistency boundaries are different.

---

# 25. Domain Invariants

The following invariants summarize the target model:

1. A Quiz is an independent Aggregate Root.
2. A Quiz is placed in a Lesson through a `LessonItem` reference.
3. A Quiz owns its QuizRevisions.
4. A QuizRevision is not an independent Aggregate Root.
5. Questions belong to a QuizRevision.
6. Answers belong to a Question.
7. Quiz visibility is separate from Quiz availability.
8. Quiz visibility is separate from Access Requirements.
9. Quiz availability is separate from Quiz lifecycle.
10. Quiz lifecycle and Revision lifecycle are independent.
11. A Published Revision cannot be modified in place.
12. A Historical Revision cannot be modified.
13. Modifying a Published assessment definition requires a new Draft Revision.
14. At most one Draft Revision exists for a Quiz.
15. At most one Revision is in review for a Quiz.
16. At most one Published Revision exists for a Quiz.
17. Historical Revisions are retained.
18. `maxScore` is derived from the sum of Question scores.
19. `passingPercentage` is optional.
20. `passingPercentage`, when configured, is greater than 0 and at most 100.
21. A Revision without `passingPercentage` produces Assessment Result `NONE`.
22. A Question has a positive score before publication.
23. A publishable `SINGLE_CHOICE` Question has exactly one correct Answer.
24. A publishable `MULTIPLE_CHOICE` Question has at least one correct Answer.
25. A publishable `TRUE_FALSE` Question has exactly two Answers and exactly one correct Answer.
26. Published and Historical Questions are immutable.
27. Published and Historical Answers are immutable.
28. Answer order is unique and contiguous within a Question.
29. Duplicate Answer content is not allowed within a Question.
30. An Attempt is bound to one exact Published QuizRevision.
31. An Attempt does not switch to a newer Revision after it starts.
32. Only one Attempt may be `IN_PROGRESS` for a User and Quiz at a time.
33. `maxAttempts` applies to User × Published QuizRevision.
34. Creating a new Attempt consumes quota immediately after successful creation.
35. A cancelled Attempt does not return consumed quota.
36. Retry creates a new Attempt.
37. An `IN_PROGRESS` Attempt may be resumed.
38. Resuming an Attempt does not consume another quota.
39. Attempt time continues to run during temporary interruption.
40. Server time is authoritative for expiration.
41. Expiration produces a terminal `EXPIRED` Attempt.
42. An expired Attempt is evaluated.
43. A cancelled Attempt is terminal.
44. A cancelled Attempt does not produce QuestionResults.
45. A cancelled Attempt does not produce an Assessment Result.
46. Manual submission with `REQUIRED_ALL` requires every Question to be answered.
47. Manual submission with `OPTIONAL` allows unanswered Questions.
48. Unanswered Questions receive zero earned score.
49. Every Question produces exactly one QuestionResult when an Attempt is evaluated.
50. An unanswered Question has `isCorrect = null`.
51. Multiple-choice full credit requires an exact selected-answer set.
52. There is no partial credit.
53. There is no negative marking.
54. `totalScore` equals the sum of QuestionResult earned scores.
55. Assessment Result is determined exactly once for evaluated Attempts.
56. Assessment Result is based on the exact Revision associated with the Attempt.
57. Assessment Result is not an Attempt Status.
58. Best Score is derived across eligible historical Attempts.
59. Best Score is not an individual Attempt field.
60. Assessment Result is not Competency, Current Level, or Learning Progress.
61. A Completion Requirement may reference a specific Published QuizRevision.
62. A Completion Requirement is satisfied by at least one valid PASSED Attempt for that Revision.
63. An already achieved completion is not automatically revoked by a later QuizRevision.
64. `NO_IMPACT` and `AFFECTS_COMPLETION` describe the completion impact of a Revision change.
65. Historical Attempts and historical evidence are preserved across later Revision publication.
66. QuizAttempt remains a separate Aggregate Root from Quiz.
67. Learning Structure does not own the Quiz Aggregate.
68. LessonItem references Quiz identity rather than containing the Quiz Aggregate.

---

# 26. Current Implementation vs Target Model

The current implementation provides part of this model but does not yet implement the complete Revision-based design.

## 26.1 Confirmed Current Implementation

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

The current `Quiz` implementation contains concepts corresponding to:

```text
Quiz
├── questions
├── maxScore
├── passingScore
├── timeLimit
├── status
└── visibility
```

The current `QuizAttempt` implementation contains concepts corresponding to:

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

---

## 26.2 Target Concepts Not Yet Present

The current source does not yet contain a `QuizRevision` domain model.

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
        └── Answer
```

The current `QuizAttempt` does not yet explicitly bind to a QuizRevision.

The current source also does not yet contain the complete target concepts for:

* `QuestionResult`;
* `AssessmentResult`;
* `CompletionPolicy`;
* Revision review lifecycle;
* target Attempt eligibility workflow;
* target immediate quota-consumption model;
* Best Score as a derived historical concept.

Therefore, these are target domain decisions rather than descriptions of the current implementation.

---

# 27. Known Current-Model Inconsistencies

The current source contains an implementation inconsistency that should be addressed when the Quiz model is evolved.

`Quiz.createDraft(UUID courseId, UUID createdBy)` passes `courseId` into the constructor field named `lessonId`.

Location:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

This conflicts with the agreed domain relationship that Quiz is referenced from a Lesson through `LessonItem`.

The target model therefore does not treat Quiz as directly owned by Course or Lesson.

The current implementation also does not yet implement the target Revision-based model.

The current implementation uses `passingScore`, while the target model uses `passingPercentage`.

The current implementation also treats the Quiz lifecycle through the existing `QuizStatus` model, while the target model separates:

```text
Quiz lifecycle
    ACTIVE
    ARCHIVED
    DELETED

Revision lifecycle
    DRAFT
    IN_REVIEW
    PUBLISHED
    HISTORICAL

Availability
    ACTIVE / INACTIVE
```

These differences are implementation gaps against the target domain model and should be addressed during the later implementation stage.

Other implementation details are intentionally left for the implementation/design stage rather than being solved in this domain model document.

