# Quiz Domain Decisions — V1

> Status: Baseline
>
> Scope: Quiz domain and its currently confirmed direct business dependencies.
>
> This document records business decisions that have been explicitly agreed upon.
> Open questions are intentionally not resolved here.

---

## 1. Quiz and Quiz Revision

### 1.1 Quiz

`Quiz` represents the stable identity and governance container of an assessment.

Quiz is responsible for:

- identity;
- ownership;
- governance;
- visibility;
- availability;
- revision lifecycle.

Quiz does not directly represent one concrete assessment definition.

### 1.2 QuizRevision

`QuizRevision` represents a concrete, version-specific assessment definition.

A Revision owns assessment-specific content and configuration, including:

- title;
- description;
- difficulty;
- time limit;
- passing percentage;
- maximum attempts;
- questions;
- answers;
- learning completion impact.

A Quiz may have multiple Revisions over its lifetime.

### 1.3 Revision independence

Questions and Answers belong to a specific Revision.

When a new Revision is created from an existing Revision, its Questions and Answers are independent instances.

Content may be copied from a previous Revision, but identity is not shared.

Therefore:

```text
Revision A
 └── Question A1
      └── Answer A1

Revision B
 └── Question B1
      └── Answer B1
````

`Question A1` and `Question B1` are different domain entities even when their content is initially identical.

---

## 2. Quiz Revision Lifecycle

A Quiz Revision follows:

```text
DRAFT → PUBLISHED → HISTORICAL
```

Rules:

* A Quiz may have at most one Draft Revision.
* A Quiz may have at most one Published Revision.
* Publishing a new Revision automatically transitions the previous Published Revision to Historical.
* Draft Revisions are editable.
* Published Revisions are immutable.
* Historical Revisions are immutable.
* Historical Revisions cannot return to Draft or Published.
* A new Revision does not modify an existing Attempt bound to an older Published Revision.

A Revision is therefore both an assessment definition and a historical boundary.

---

## 3. Question

`Question` is an Entity inside a `QuizRevision`.

It is not an Aggregate Root.

A Question has:

* identity;
* prompt;
* question type;
* score;
* ordered Answers.

### 3.1 Question completeness

A Draft Question may temporarily be incomplete.

Draft editing may therefore produce temporarily invalid content.

Completeness is validated when the Revision is submitted/published.

No `QuestionStatus` is introduced for incomplete Questions.

### 3.2 Question Type

The current Question Types are:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

`QuestionType` is a domain classification that determines structural and evaluation rules.

It is not an Entity.

### 3.3 Question type rules

For a publishable Question:

#### SINGLE_CHOICE

Exactly one Answer must be correct.

#### MULTIPLE_CHOICE

At least one Answer must be correct.

#### TRUE_FALSE

Exactly two Answers must exist and exactly one must be correct.

Draft Questions may temporarily violate these rules.

### 3.4 Question type editing

A Draft Question may change its Question Type.

Changing the Question Type does not automatically modify its Answers or correctness configuration.

The author is responsible for adjusting the Answers so that the Question becomes valid before publication.

---

## 4. Answer

`Answer` is an Entity inside a `Question`.

It is not an Aggregate Root.

An Answer has:

* identity;
* content;
* correctness;
* order.

### 4.1 Answer identity

Answer identity is stable and is not represented by presentation labels such as:

```text
A
B
C
D
```

Labels are presentation concerns derived from Answer ordering.

### 4.2 Correctness

`isCorrect` belongs to the Answer.

The Question is responsible for enforcing collection-level correctness invariants.

No separate `CorrectAnswerConfiguration` object is introduced.

### 4.3 Answer ordering

Answer order is domain data.

Rules:

* order starts at `1`;
* order is unique within a Question;
* order is contiguous;
* ordering is independent from Answer identity and correctness.

Presentation labels may be derived from the order.

### 4.4 Draft Answer editing

While the Question is Draft, the author may:

* add Answers;
* remove Answers;
* modify Answer content;
* reorder Answers.

A Draft Question may temporarily contain zero Answers.

Removing the current correct Answer is allowed.

The Question may therefore temporarily contain zero correct Answers while being edited.

### 4.5 Answer content

Answer content is required for a publishable Question.

Duplicate Answer content is not allowed within the same Question.

The exact normalization and case-sensitivity rules remain open.

---

## 5. Question Score and Quiz Score

Each Question has a fixed positive score.

```text
Question.score > 0
```

The maximum score of a Revision is derived:

```text
Revision.maxScore
    =
sum(Question.score)
```

`maxScore` is not independently authored or stored as a separate source of truth.

A Draft Revision with zero Questions may therefore have:

```text
maxScore = 0
```

A Published Revision must contain at least one valid Question, therefore its `maxScore` is positive.

No separate `Score` Value Object is introduced at this stage.

---

## 6. Passing Percentage

Passing is configured as a percentage rather than a fixed score.

The Revision stores:

```text
passingPercentage
```

The passing percentage is optional.

```text
null
```

means that the Revision does not produce a PASSED/FAILED classification.

When configured:

```text
0 < passingPercentage ≤ 100
```

The Attempt result is determined from:

```text
(totalScore / maxScore) × 100
```

An Attempt passes when the calculated percentage is greater than or equal to the configured `passingPercentage`.

`passingScore` is not stored as an independent source of truth.

---

## 7. Time Limit

`timeLimit` belongs to the Quiz Revision.

It is optional:

```text
null = unlimited
```

When configured, the value must be positive.

The effective expiration of an Attempt is determined from:

```text
Attempt.startedAt
+
PublishedRevision.timeLimit
```

`startedAt` is recorded using server time.

An Attempt therefore remains bound to the timing policy of the Published Revision under which it was created.

A later Revision does not modify the time limit of an existing Attempt.

---

## 8. Maximum Attempts

`maxAttempts` belongs to a Published Quiz Revision.

It is optional:

```text
null = unlimited
```

When configured, the value must be greater than zero.

The quota is scoped to:

```text
User × Published QuizRevision
```

Rules:

* A successful Attempt creation consumes one attempt immediately.
* `CANCELLED` Attempts consume quota.
* A learner may create a new Attempt after a previous Attempt reaches a terminal state.
* A new Attempt is allowed only while the Revision's quota remains available.
* The total number of successfully created Attempts must never exceed `maxAttempts`.
* The rule must remain true under concurrent Attempt creation.
* A learner may have at most one `IN_PROGRESS` Attempt for the same Quiz at a time.
* `maxAttempts = null` allows unlimited Attempts, subject to the one-active-Attempt rule.

A new Published Revision receives its own attempt quota.

Publishing a new Revision does not carry over the previous Revision's quota.

---

## 9. Quiz Lifecycle

Quiz lifecycle is separate from Revision lifecycle.

The Quiz lifecycle is:

```text
ACTIVE → ARCHIVED → DELETED
   ↑          │
   └─ Restore ┘
```

The Quiz may also be activated from Archived:

```text
ARCHIVED → ACTIVE
```

Rules:

* Archiving does not terminate an existing `IN_PROGRESS` Attempt.
* Archived Quizzes do not allow new Attempts.
* Deletion is allowed only from Archived.
* Deletion is a soft delete.
* A Quiz cannot be deleted while an `IN_PROGRESS` Attempt exists.
* Deletion does not business-delete Historical Revisions.
* Deletion does not business-delete completed or terminal Attempts.
* Restore returns the Quiz to `ARCHIVED`.
* A Deleted Quiz blocks management operations until it is restored.
* A Deleted Quiz cannot create a new Revision, edit a Draft, publish a Revision, change its author, change Visibility, change Availability, or create new Attempts.

Quiz lifecycle is independent from Revision lifecycle.

A Published Revision may remain Published even when the Quiz itself is Archived or Deleted.

---

## 10. Availability

Quiz Availability is distinct from Quiz lifecycle.

Availability has two states:

```text
ACTIVE
INACTIVE
```

Availability determines whether new Attempts may be started.

When Availability becomes `INACTIVE`:

* new Attempts cannot be created;
* existing `IN_PROGRESS` Attempts are not automatically terminated.

Availability is therefore different from:

* Visibility;
* Revision lifecycle;
* Attempt lifecycle.

A configured Availability Window also affects only the ability to start a new Attempt.

It does not automatically terminate an Attempt that is already running.

---

## 11. Ownership

Quiz ownership distinguishes between:

```text
createdBy
author
```

### createdBy

`createdBy` represents the historical creator of the Quiz.

It is immutable.

### author

`author` represents the currently responsible author.

It may change through a `ChangeAuthor` business operation.

Changing the author does not create a new Revision.

A new author receives the authority associated with managing the Quiz according to the applicable authorization policy.

A Deleted Quiz cannot change its author.

The exact role/authorization policy remains an open business decision.

---

## 12. Visibility

Quiz Visibility determines the learner-facing access scope.

Supported visibility concepts are:

```text
PRIVATE
PUBLIC
COURSE_ONLY
```

### PRIVATE

Ordinary learners cannot access the Quiz.

### PUBLIC

The Quiz may be accessed by learners, but Public visibility alone does not guarantee eligibility to start an Attempt.

### COURSE_ONLY

The Quiz requires valid Course learning access.

Visibility is therefore not equivalent to Attempt eligibility.

---

## 13. Access Requirements

Access Requirements are separate from Visibility.

A Quiz may define multiple Access Requirements.

The default composition is:

```text
Requirement A
    AND
Requirement B
    AND
Requirement C
```

Possible business requirements include:

* Course access;
* Premium/entitlement;
* Role.

Failure of a hard Access Requirement blocks Attempt creation.

A learner cannot bypass a hard Access Requirement through a "Continue anyway" option.

No generic nested access-rule engine is introduced at this stage.

---

## 14. Course Access

Course access is an Access Eligibility condition.

Course access is distinct from:

* Quiz Visibility;
* Quiz Completion;
* Learning Prerequisites.

A `COURSE_ONLY` Quiz requires valid Course learning access.

Course access alone does not guarantee eligibility to start a Quiz Attempt because additional Learning Prerequisites may still apply.

Course completion is also not equivalent to Quiz eligibility.

---

## 15. Current Level Recommendation

A Quiz may define a recommended minimum Current Level.

Current Level is a learner-state concept.

It is not a hard prerequisite for Quiz participation.

If the learner's Current Level is below the recommended level:

```text
Warning
   ├── Continue anyway
   └── Go back
```

Only when the learner chooses to continue is the Attempt created and its attempt quota consumed.

Current Level is therefore a soft recommendation/warning rather than an access block.

The exact Current Level model, calculation, and source remain open.

---

## 16. Learning Prerequisites

A Learning Prerequisite represents a learning condition that must be satisfied before a learner may start a Quiz Attempt.

Examples include:

* completion of a specific Lesson;
* completion of a set of Lessons;
* completion of learning content associated with a Section.

If a Learning Prerequisite is not satisfied:

```text
Start Attempt → rejected
Attempt       → not created
Quota         → not consumed
```

Learning Prerequisites are distinct from:

* Course Access;
* Access Requirements;
* Current Level recommendation.

Multiple Learning Prerequisites may be combined.

The default model supports:

```text
AND
OR
```

No nested generic prerequisite expression engine is introduced at this stage.

The exact ownership and reference model of Learning Prerequisites remains open.

---

## 17. Start Attempt

Starting a Quiz Attempt is one logical business operation.

A learner may create a new Attempt only when all required conditions are satisfied:

```text
Access
  ↓
Access Requirements
  ↓
Quiz Lifecycle / Availability
  ↓
Published Revision exists
  ↓
Learning Prerequisites
  ↓
No conflicting IN_PROGRESS Attempt
  ↓
Attempt quota available
  ↓
Create Attempt
```

If any hard condition fails:

```text
No Attempt
No quota consumption
```

Current Level may produce a warning rather than a hard failure.

Eligibility validation and Attempt creation must be treated as one logical business operation so that a successful creation cannot exceed the Revision's attempt quota.

---

## 18. Quiz Attempt

`QuizAttempt` is a separate Aggregate Root.

It represents one learner's execution of a specific Published Quiz Revision.

An Attempt is bound to the exact Published Revision under which it was created.

Its lifecycle is:

```text
IN_PROGRESS
    ├── SUBMITTED
    ├── EXPIRED
    └── CANCELLED
```

Once an Attempt reaches a terminal state, it cannot be resumed or modified.

A newer Quiz Revision never changes the Revision of an existing Attempt.

---

## 19. Resume

An `IN_PROGRESS` Attempt may be resumed.

Resume:

* does not create another Attempt;
* does not consume another quota;
* does not re-run Start Attempt eligibility;
* continues the same Attempt;
* continues using the same Published Revision;
* continues using the same timing rules.

Current question navigation is presentation/UI behavior.

The domain does not persist `currentQuestionId` merely to support frontend navigation.

---

## 20. Answering

While an Attempt is `IN_PROGRESS`, the learner may:

* select an Answer;
* change the selected Answer;
* clear the current Answer;
* select another Answer.

The Attempt stores the learner's current selection.

It does not store answer-change history.

A Question has at most one current UserAnswer in an Attempt.

Clearing an Answer results in:

```text
UNANSWERED
```

No Question-level `SKIPPED` domain state is introduced.

---

## 21. Skip

Skip is navigation behavior.

It is not a persisted domain state.

Skipping a Question:

* does not create a UserAnswer;
* does not create a QuestionResult;
* does not mark the Question as skipped.

The learner may return to the Question later.

If the Attempt ends while the Question has no current answer, the Question is evaluated as `UNANSWERED`.

---

## 22. Answer Evaluation Timing

Correctness is not evaluated while the learner is answering.

During `IN_PROGRESS`:

```text
UserAnswer
    ↓
current selection only
```

The domain does not classify the selection as correct or incorrect at this point.

When the Attempt reaches an evaluated terminal state:

```text
UserAnswer
    ↓
Evaluate
    ↓
QuestionResult
```

Only the final current selection is used for scoring.

---

## 23. Reference Integrity

An Attempt may accept only Questions and Answers belonging to the exact Published Revision bound to that Attempt.

The relationship is:

```text
Attempt
   ↓
Published Revision
   ↓
Question
   ↓
Answer
```

Therefore:

* a Question from another Revision cannot be answered;
* an Answer from another Question cannot be selected;
* an Answer from another Revision cannot be selected;
* invalid Answer identities are rejected.

Question and Answer identity are therefore part of the historical boundary of the Attempt.

---

## 24. Completion Policy

Completion Policy determines whether Manual Submit requires all Questions to have an answer.

### REQUIRED_ALL

All Questions must have a current answer before Manual Submit succeeds.

If at least one Question is `UNANSWERED`:

```text
Manual Submit → rejected
```

Skip remains allowed during the Attempt.

Expiration does not use the Manual Submit requirement.

If the Attempt expires, all Questions are evaluated and unanswered Questions receive zero points.

### OPTIONAL

Manual Submit may succeed even when Questions remain unanswered.

Unanswered Questions receive zero points.

Expiration evaluates the Attempt in the same way.

Completion Policy does not change:

* Answering;
* Skip;
* Expiration.

It only controls the success condition of Manual Submit.

---

## 25. Time Limit and Expiration

Time Limit and Attempt Expiration are related but distinct concepts.

`timeLimit` defines the maximum allowed answering time.

The Attempt's expiration is derived from:

```text
startedAt + PublishedRevision.timeLimit
```

Rules:

* server time is authoritative;
* closing the browser does not pause the Attempt;
* losing network connectivity does not pause the Attempt;
* Resume continues the existing countdown;
* once expiration is reached, new Answers are rejected;
* Answers recorded before expiration remain valid for evaluation.

If the time limit is reached:

```text
IN_PROGRESS
    ↓
EXPIRED
    ↓
Evaluate recorded answers
```

An expired Attempt is evaluated normally.

---

## 26. Cancel

An `IN_PROGRESS` Attempt may be cancelled when the applicable policy allows cancellation.

```text
IN_PROGRESS
    ↓
CANCELLED
```

Rules:

* cancellation requires no reason;
* UX may request confirmation;
* a cancelled Attempt cannot be resumed;
* a cancelled Attempt cannot be submitted;
* a cancelled Attempt is not scored;
* a cancelled Attempt does not create QuestionResults;
* cancellation consumes the Attempt quota.

Cancellation is a policy capability rather than a separate `AssessmentType`.

---

## 27. Final Submit

Final Submit is a complete business operation.

A successful Submit must evaluate the entire Attempt atomically from a business perspective:

```text
Evaluate all Questions
        ↓
QuestionResults
        ↓
Total Score
        ↓
Assessment Result
        ↓
Attempt becomes SUBMITTED
```

The business must not allow a partially evaluated submitted Attempt.

Every Question in the bound Published Revision is evaluated.

---

## 28. QuestionResult

When an Attempt is evaluated, every Question in the bound Published Revision receives exactly one `QuestionResult`.

A QuestionResult contains:

```text
QuestionResult
├── questionId
├── selectedAnswerIds
├── responseStatus
├── isCorrect
└── earnedScore
```

### ANSWERED

```text
selectedAnswerIds
isCorrect = true / false
earnedScore
```

### UNANSWERED

```text
selectedAnswerIds = []
isCorrect = null
earnedScore = 0
```

`selectedAnswerIds` represents the evaluated selection.

`isCorrect` is a historical evaluation fact.

`earnedScore` is the historical score earned for the Question.

A QuestionResult is immutable after creation.

---

## 29. Scoring

Scoring uses a fixed positive score per Question.

There is:

* no partial credit;
* no negative marking.

Rules:

```text
correct
    → Question.score

incorrect
    → 0

unanswered
    → 0
```

For `MULTIPLE_CHOICE`, correctness uses exact-set matching.

Example:

```text
Correct = {A, C}
```

Then:

```text
{A, C}     → correct → full score
{A}        → incorrect → 0
{A, B, C}  → incorrect → 0
```

Therefore:

```text
0 ≤ earnedScore ≤ Question.score
```

and under the current policy:

```text
earnedScore ∈ {0, Question.score}
```

---

## 30. Assessment Result

An Attempt receives its final assessment classification when it reaches an evaluated terminal state:

```text
SUBMITTED
EXPIRED
```

The result is determined exactly once.

Possible result classifications are:

```text
PASSED
FAILED
NONE
```

If `passingPercentage` is configured:

```text
scorePercentage >= passingPercentage
    → PASSED

scorePercentage < passingPercentage
    → FAILED
```

If `passingPercentage = null`:

```text
AssessmentResult = NONE
```

`scorePercentage` is derived and is not persisted as an independent source of truth.

Once determined, the result is historical assessment evidence.

It is not recalculated because of later Quiz Revision changes.

`CANCELLED` Attempts do not produce an Assessment Result.

---

## 31. Historical Assessment Evidence

An evaluated Attempt and its QuestionResults represent historical assessment evidence.

Once an Attempt reaches an evaluated terminal state:

* its result is fixed;
* its QuestionResults are immutable;
* later Quiz Revisions do not recalculate the Attempt;
* the Attempt continues to refer to its original Published Revision.

Therefore:

```text
Revision A
    ↓
Attempt A
    ↓
Result A
```

remains historically valid even after:

```text
Revision B
```

is published.

---

## 32. Best Score

Best Score is a derived concept across eligible evaluated Attempts.

It is not a separate Attempt state.

An evaluated Attempt may contribute its result/score to Best Score according to the applicable Quiz policy.

`CANCELLED` Attempts do not contribute an assessment result.

The exact Best Score policy remains to be finalized.

---

## 33. Quiz as Practice or Completion Requirement

A Quiz does not inherently mean that completion is mandatory.

A Quiz may be used as:

```text
Practice / Diagnostic
```

or:

```text
Completion Requirement
```

The mandatory meaning comes from the Completion Requirement, not from the Quiz itself.

A Practice/Diagnostic Quiz may generate Learning Evidence without blocking Lesson or Section completion.

A Quiz used as a Completion Requirement becomes a completion gate.

---

## 34. Completion Requirement

A Completion Requirement defines an assessment condition that must be satisfied for a Lesson or Section to be completed.

A Quiz Completion Requirement may apply to:

```text
Lesson
Section
```

A Completion Requirement references a specific:

```text
Published QuizRevision
```

rather than only the stable Quiz identity.

This preserves the historical meaning of which assessment definition satisfied the requirement.

---

## 35. Multiple Completion Requirements

A Lesson or Section may have multiple Completion Requirements.

The default composition is:

```text
Requirement A
    AND
Requirement B
    AND
Requirement C
```

Simple OR composition may be supported when a concrete business need exists.

A generic nested expression engine is not introduced at this stage.

---

## 36. Satisfying a Quiz Completion Requirement

A Quiz Completion Requirement is satisfied when the learner has at least one valid:

```text
PASSED QuizAttempt
```

for the Published QuizRevision referenced by the requirement.

The latest Attempt does not have to be the passing Attempt.

Example:

```text
Attempt 1 → FAILED
Attempt 2 → FAILED
Attempt 3 → PASSED
Attempt 4 → FAILED
```

The Completion Requirement remains satisfied because a valid passing Attempt exists.

---

## 37. Lesson and Section Completion

Quiz Completion Requirements do not replace learning-content completion.

Conceptually:

```text
Learning Content Completion
          AND
Completion Requirements
          ↓
Lesson / Section Completed
```

A Lesson may therefore:

* have no Quiz;
* contain a Practice/Diagnostic Quiz;
* contain one or more Required Quiz Completion Requirements.

A Section may similarly define Section-level Completion Requirements.

The exact learning-content completion criteria remain open.

---

## 38. Historical Completion

Once a Completion Requirement has been satisfied and the associated Lesson or Section has been completed, a later Quiz Revision does not automatically revoke that completion.

The system must not:

```text
COMPLETED → INCOMPLETE
```

merely because a newer Revision was published.

Historical completion evidence remains preserved.

This means:

```text
Historical Completion
    ≠
Current Learning State
```

---

## 39. Learning Completion Impact

A Quiz Revision may explicitly declare whether its changes affect Learning Completion.

The business concept is:

```text
NO_IMPACT
AFFECTS_COMPLETION
```

The Content Author/Manager explicitly determines this impact.

### NO_IMPACT

A new Revision does not materially change the learning completion requirements.

Existing completion remains valid.

### AFFECTS_COMPLETION

The Revision materially affects the learning requirements represented by the related completion logic.

This may require current/future completion evaluation to be reconsidered.

However:

* historical evidence is preserved;
* already-established completion is not deleted;
* the entire Course or Enrollment is not automatically reset.

Learning Completion Impact is therefore about current/future completion evaluation, not rewriting historical facts.

---

## 40. Learning Activity

`Learning Activity` is a learner-facing learning action.

It is not equivalent to:

* `LessonItem`;
* a content element;
* a UI component.

An Activity may produce Learning Evidence, but it is not required to do so.

Evidence should be created when the Activity produces meaningful, domain-relevant evidence of:

* learner performance;
* completion;
* outcome.

A Learning Activity may exist independently of:

* Course;
* Section;
* Lesson.

A Lesson or another Learning Flow may use a Learning Activity, but Lesson is not the defining boundary of Learning Activity.

---

## 41. Important Domain Distinctions

The following concepts must remain distinct:

```text
Quiz
    ≠ QuizRevision
    ≠ QuizAttempt

Question
    ≠ Answer

Visibility
    ≠ Access Requirement
    ≠ Learning Prerequisite
    ≠ Current Level Recommendation
    ≠ Availability

Quiz Score
    ≠ Assessment Result
    ≠ Learning Evidence
    ≠ Completion

Completion Requirement
    ≠ Learning Prerequisite

Historical Completion
    ≠ Current Learner State

Learning Activity
    ≠ LessonItem
    ≠ Learning Evidence
    ≠ Progress
    ≠ Competency
```

---

## 42. Explicitly Open

The following decisions are intentionally not resolved in V1:

* exact QuizRevision Aggregate boundary;
* Submit for Review workflow;
* exact Availability Window model;
* exact authorization model for ChangeAuthor;
* exact Access Requirement model;
* exact Learning Prerequisite ownership and reference model;
* detailed Current Level model, calculation, and source;
* exact learning-content completion criteria;
* detailed propagation of Learning Completion Impact;
* detailed Best Score policy;
* detailed remediation policy when a Completion Requirement is not satisfied;
* Learning Activity identity/versioning model;
* broader Competency and Learner State models.

These should be resolved only when their business need and boundaries are sufficiently understood.

