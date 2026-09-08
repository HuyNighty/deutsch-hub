# Quiz Business Rules

## 1. Purpose

This document defines the business rules governing Quiz definitions, Quiz Revisions, Questions, Answers, QuizAttempts, assessment results, retry policies, Quiz availability, learner eligibility, and learning completion requirements within the Learning Context.

The rules are based on:

- the current DeutschHub implementation;
- the target Learning domain model;
- the Quiz domain decisions established during domain discovery.

The document distinguishes confirmed business rules from decisions that remain open.

---

# 2. Quiz Definition Rules

## BR-QUIZ-01 — Quiz Is an Independent Assessment Definition

A Quiz is an independent assessment definition within the Learning Context.

A Quiz is not owned by a Course or Lesson Aggregate.

A Quiz may be placed inside a Lesson through a `LessonItem`, which references the Quiz by identifier.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
            └── quizId → Quiz
````

The Quiz Aggregate owns the Quiz identity, governance, and Revision lifecycle.

---

## BR-QUIZ-02 — Quiz Is the Stable Assessment Identity

The Quiz represents the stable identity of an assessment across multiple definitions.

The Quiz does not directly contain the mutable assessment content.

The concrete assessment definition belongs to a `QuizRevision`.

```text
Quiz
└── QuizRevision
    └── Question
        └── Answer
```

---

## BR-QUIZ-03 — Published Revisions Are Immutable

A Published Quiz Revision must not be modified in place.

Any modification to a Published assessment definition must be performed through a new Draft Revision.

The existing Published Revision remains unchanged.

This protects the integrity of existing QuizAttempts that depend on the previously published definition.

---

## BR-QUIZ-04 — Historical Revisions Are Preserved

When a new Revision becomes Published, the previous Published Revision becomes Historical.

Historical Revisions must be retained.

Historical Revisions are immutable.

They remain available as the assessment definition referenced by existing historical QuizAttempts.

---

## BR-QUIZ-05 — Quiz Visibility Is Separate from Availability

Quiz visibility and Quiz availability are independent concepts.

Visibility determines the high-level access mode of the Quiz.

Availability determines whether the Quiz currently accepts new Attempts.

Therefore:

```text
Visibility ≠ Availability
```

Changing Quiz availability does not terminate existing `IN_PROGRESS` QuizAttempts.

---

## BR-QUIZ-06 — Quiz Visibility

The supported Quiz visibility concepts are:

```text
PRIVATE
COURSE_ONLY
PUBLIC
```

The business meaning is:

* `PRIVATE`: ordinary learners cannot access the Quiz.
* `PUBLIC`: the learner may access the Quiz subject to eligibility checks.
* `COURSE_ONLY`: valid Course learning access is required.

Visibility is distinct from:

```text
Access Requirements
Learning Prerequisites
Availability
Current Level
```

The exact access-check implementation is outside the Quiz Aggregate.

---

## BR-QUIZ-07 — Quiz Availability

Quiz availability is represented by:

```text
ACTIVE
INACTIVE
```

An inactive Quiz cannot accept new Attempts.

Deactivating a Quiz does not automatically terminate existing `IN_PROGRESS` Attempts.

Availability therefore controls new Attempt creation rather than the lifecycle of existing Attempts.

---

# 3. Quiz Lifecycle Rules

## BR-LIFECYCLE-01 — Quiz Lifecycle

The Quiz lifecycle is:

```text
ACTIVE
   ↓
ARCHIVED
   ↓
DELETED
```

Quiz lifecycle is separate from:

```text
Revision Lifecycle
Availability
Attempt Lifecycle
```

---

## BR-LIFECYCLE-02 — Archive

Archiving a Quiz prevents new Attempts from being created.

Archiving does not automatically terminate existing `IN_PROGRESS` Attempts.

Existing Attempts remain bound to their original Revision.

---

## BR-LIFECYCLE-03 — Delete

A Quiz may be deleted only from the `ARCHIVED` state.

Deletion is a soft-delete operation.

A Quiz must not be deleted while it has an `IN_PROGRESS` QuizAttempt.

Deleting a Quiz does not delete:

* Historical Revisions;
* completed QuizAttempts;
* expired QuizAttempts;
* cancelled QuizAttempts;
* assessment evidence.

---

## BR-LIFECYCLE-04 — Restore

A deleted Quiz may be restored.

Restoring a Quiz returns it to:

```text
ARCHIVED
```

A restored Quiz must be explicitly activated before new Attempts may be created.

---

## BR-LIFECYCLE-05 — Deleted Quiz Is Management-Blocked

While a Quiz is `DELETED`, management operations are blocked.

This includes:

* creating a new Revision;
* editing a Draft Revision;
* publishing a Revision;
* changing the author;
* changing visibility;
* changing availability;
* creating new Attempts.

Restore must occur before further management is allowed.

---

# 4. Quiz Revision Rules

## BR-REV-01 — Revision Lifecycle

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

A Revision may move to Historical when a newer Revision becomes Published.

---

## BR-REV-02 — Revision Cardinality

A Quiz may have:

* at most one Draft Revision;
* at most one Revision in Review;
* at most one Published Revision;
* zero or more Historical Revisions.

A Draft and a Published Revision may coexist.

A Revision in Review may coexist with the current Published Revision.

---

## BR-REV-03 — Creating a New Revision

When a Published Quiz definition needs to be modified, the modification must be performed on a new Draft Revision.

The existing Published Revision remains unchanged.

A new Draft Revision must not be created while another Draft Revision already exists.

The existing Draft must instead be edited, submitted for review, published, or discarded.

---

## BR-REV-04 — Draft Revision Is Mutable

A Draft Revision may be edited.

A Draft Revision may temporarily be incomplete.

Temporary incomplete states do not prevent Draft editing.

---

## BR-REV-05 — Draft Revision May Be Discarded

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

## BR-REV-06 — Submit Draft Revision for Review

A Draft Revision may be submitted for review when the required review conditions are satisfied.

Submission moves the Revision to:

```text
IN_REVIEW
```

A Revision in Review is not yet available for new Attempts.

The exact review workflow and reviewer authorization are outside the core Revision definition rules.

---

## BR-REV-07 — Publish Revision

A Revision may become Published only when all required publication conditions are satisfied.

When a new Revision is published:

1. The Draft/Review Revision becomes the Published Revision.
2. The previous Published Revision becomes Historical.
3. Existing QuizAttempts remain associated with their original Revision.

---

## BR-REV-08 — Revision Contains Assessment Rules

Assessment configuration belongs to the applicable Quiz Revision.

This includes:

* Questions;
* Answer definitions;
* Question scores;
* maximum score;
* passing percentage;
* time limit;
* maximum attempts;
* completion policy;
* other Revision-specific assessment rules.

A historical QuizAttempt continues to use the rules of the Revision from which it started.

---

# 5. Question and Answer Rules

## BR-QUESTION-01 — Draft Question May Be Incomplete

A Draft Question may temporarily contain:

```text
0 Answers
```

and may temporarily contain no correct Answer.

These states are allowed during Draft editing.

A Question must satisfy all publication invariants before its Revision can be Published.

---

## BR-QUESTION-02 — Question Answer Count for Publication

A publishable Question must contain:

```text
2 to 6 Answers
```

---

## BR-QUESTION-03 — Correct Answer Requirement

A publishable Question must contain at least one correct Answer.

---

## BR-QUESTION-04 — Single Choice

A `SINGLE_CHOICE` Question must contain exactly one correct Answer before publication.

---

## BR-QUESTION-05 — Multiple Choice

A `MULTIPLE_CHOICE` Question must contain at least one correct Answer before publication.

---

## BR-QUESTION-06 — True/False

A `TRUE_FALSE` Question must contain exactly:

```text
2 Answers
1 correct Answer
```

before publication.

---

## BR-QUESTION-07 — Answer Uniqueness

Answer content must be unique within a Question.

The target normalization rule is:

```text
case-insensitive comparison
```

Therefore, two Answers whose normalized contents differ only by letter case are considered duplicates.

---

## BR-QUESTION-08 — Question Score

A publishable Question must have a positive score.

The Revision maximum score is derived from Question scores.

```text
maxScore
=
sum(Question.score)
```

`maxScore` is not an independent author-entered source of truth.

---

## BR-QUESTION-09 — Supported Question Types

The currently supported Question types are:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

Text-based Question types are not part of the current Quiz scope.

---

## BR-QUESTION-10 — Answer Order

Answer order is domain data.

Within a Question, Answer order must be:

* unique;
* contiguous;
* starting from 1.

Adding, removing, or reordering Answers may cause their order values to be normalized.

---

## BR-QUESTION-11 — Question Type Changes

A Draft Question may change its Question type.

Changing the type does not automatically modify:

* existing Answers;
* Answer identities;
* correctness states.

The resulting Question must satisfy the invariants of its final type before publication.

---

## BR-QUESTION-12 — Published Questions Are Immutable

Questions belonging to a Published or Historical Revision are immutable.

Changes require a new Draft Revision.

---

## BR-QUESTION-13 — Published Answers Are Immutable

Answers belonging to a Published or Historical Revision are immutable.

Changes require a new Draft Revision.

---

# 6. Publication Rules

## BR-PUBLISH-01 — Questions Are Required

A Quiz Revision cannot be Published without at least one Question.

---

## BR-PUBLISH-02 — Questions Must Be Valid

Every Question in a Quiz Revision must satisfy all required publication invariants.

This includes:

* valid Question type;
* valid Answer count;
* valid correctness configuration;
* valid Answer content;
* valid Answer order;
* positive Question score.

---

## BR-PUBLISH-03 — Maximum Score Is Derived

The Revision maximum score is:

```text
sum(Question.score)
```

A Published Revision therefore always has a positive maximum score because it contains at least one valid Question with a positive score.

---

## BR-PUBLISH-04 — Passing Percentage Must Be Valid

When `passingPercentage` is defined:

```text
0 < passingPercentage <= 100
```

---

## BR-PUBLISH-05 — Published Definition Cannot Be Modified

After publication, the Revision and its Questions and Answers are immutable.

Changes must be made through a new Draft Revision.

---

# 7. Passing and Assessment Result Rules

## BR-SCORE-01 — Passing Percentage Is Optional

A Quiz Revision does not have to define a passing percentage.

This supports both practice-oriented and assessment-oriented Quizzes.

```text
passingPercentage = null
→ AssessmentResult = NONE
```

The Attempt may still produce a numeric score.

---

## BR-SCORE-02 — Passing Percentage Belongs to the Revision

`passingPercentage` is part of the Quiz Revision.

An Attempt evaluates its result using the passing percentage of the exact Revision associated with that Attempt.

---

## BR-SCORE-03 — Assessment Result Uses Score Percentage

When a passing percentage is defined:

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

scorePercentage < passingPercentage
        ↓
     FAILED
```

The comparison uses the exact Revision associated with the Attempt.

No fixed `passingScore` is used as the source of truth.

---

## BR-SCORE-04 — No Passing Percentage Means No Pass/Fail Result

When a Quiz Revision has no passing percentage:

```text
AssessmentResult = NONE
```

The Attempt may still have a numeric `totalScore`.

---

## BR-SCORE-05 — Assessment Result Is Separate from Attempt Status

Attempt lifecycle statuses are:

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

Assessment Results are:

```text
PASSED
FAILED
NONE
```

Therefore:

```text
PASSED ≠ Attempt Status
FAILED  ≠ Attempt Status
```

---

## BR-SCORE-06 — Assessment Result Is Determined at Completion

Assessment Result is determined exactly once when an Attempt reaches an assessment-ending state:

```text
SUBMITTED
or
EXPIRED
```

A `CANCELLED` Attempt does not produce an Assessment Result.

---

## BR-SCORE-07 — Expired Does Not Mean Failed

An expired Attempt is evaluated using the answers recorded before expiration.

Therefore:

```text
EXPIRED
```

does not inherently mean:

```text
FAILED
```

An expired Attempt may be:

```text
PASSED
FAILED
NONE
```

depending on the Revision's passing percentage and final score.

---

# 8. QuizAttempt Creation Rules

## BR-ATTEMPT-01 — Attempt Starts from Published Revision

A QuizAttempt may only be started from a Published Quiz Revision.

A Draft, In-Review, or Historical Revision cannot be used to start a new Attempt.

---

## BR-ATTEMPT-02 — Exact Revision Binding

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

## BR-ATTEMPT-03 — Historical Assessment Integrity

Changes to the Quiz after an Attempt starts must not change the assessment definition used by that Attempt.

The Attempt's:

* Questions;
* Answers;
* Question scores;
* maximum score;
* passing percentage;
* time limit;
* completion policy;
* maximum attempt policy;
* other applicable assessment rules

remain determined by its associated Revision.

---

## BR-ATTEMPT-04 — Quiz Must Be Active for New Attempts

A Quiz must be in the appropriate active lifecycle state and have active availability before a new QuizAttempt may be created.

A Quiz that is:

```text
ARCHIVED
or
DELETED
```

cannot accept new Attempts.

An `INACTIVE` Quiz cannot accept new Attempts.

---

## BR-ATTEMPT-05 — Attempt Creation Requires Eligibility

Before creating a new QuizAttempt, the applicable eligibility conditions must be satisfied.

Conceptually:

```text
Quiz accessible
      ↓
Access Requirements satisfied
      ↓
Quiz active
      ↓
Availability allows Start
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

The exact prerequisite and unlock conditions remain outside the Quiz Aggregate.

---

## BR-ATTEMPT-06 — Eligibility and Attempt Creation Are One Logical Operation

Eligibility validation and Attempt creation must behave as one logical business operation.

A concurrent race must not allow the system to exceed:

* the one-IN_PROGRESS Attempt rule;
* the applicable maximum attempt quota.

---

# 9. Attempt Limit and Retry Rules

## BR-ATTEMPT-LIMIT-01 — Multiple Attempts

A Quiz Revision may allow multiple Attempts.

The maximum number of Attempts is defined by:

```text
maxAttempts
```

When:

```text
maxAttempts = null
```

the number of Attempts is unlimited.

---

## BR-ATTEMPT-LIMIT-02 — Attempt Quota Is Per User and Published Revision

The attempt quota applies to:

```text
User × Published Quiz Revision
```

A new Published Revision has its own applicable attempt quota.

---

## BR-ATTEMPT-LIMIT-03 — Attempt Quota Is Consumed at Creation

A successfully created QuizAttempt consumes one attempt quota immediately.

Quota consumption does not wait until the Attempt reaches a terminal state.

```text
Create Attempt
      ↓
quota consumed
      ↓
IN_PROGRESS
```

---

## BR-ATTEMPT-LIMIT-04 — Cancelled Attempts Do Not Return Quota

A `CANCELLED` Attempt does not return previously consumed quota.

---

## BR-ATTEMPT-LIMIT-05 — Passed Attempt Retry

A User may retry after `PASSED` when the applicable attempt policy and remaining quota permit it.

Passing does not universally prevent another Attempt.

---

## BR-ATTEMPT-LIMIT-06 — Failed Attempt Retry

A User may retry after `FAILED` when the applicable attempt policy and remaining quota permit it.

Any temporal retry restriction remains policy-specific.

---

## BR-ATTEMPT-LIMIT-07 — Retry Creates a New Attempt

Retrying a Quiz creates a new QuizAttempt.

A previous Attempt is never reopened or converted into a new Attempt.

Answers from the previous Attempt are not carried into the new Attempt.

---

# 10. Concurrent Attempt Rules

## BR-CONCURRENT-01 — One Active Attempt per User and Quiz

A User may have at most one `IN_PROGRESS` QuizAttempt for the same Quiz at a time.

This rule is scoped to the Quiz identity rather than only to one Revision.

Therefore:

```text
User × Quiz
    → maximum one IN_PROGRESS Attempt
```

---

## BR-CONCURRENT-02 — Concurrency Must Respect Attempt Quota

Concurrent Attempt creation must not allow a User to exceed the configured `maxAttempts` for the applicable Published Revision.

The quota check and successful Attempt creation must be treated as one logical operation.

---

# 11. Attempt Resume Rules

## BR-RESUME-01 — IN_PROGRESS Attempts May Be Resumed

An `IN_PROGRESS` QuizAttempt may be resumed after the User temporarily leaves the Quiz.

This includes:

* closing the browser;
* losing network connectivity;
* temporarily leaving the Quiz.

These events do not automatically terminate the Attempt.

---

## BR-RESUME-02 — Browser Closure Does Not Cancel an Attempt

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

## BR-RESUME-03 — Resume Uses the Same Attempt

Resuming an Attempt continues the existing QuizAttempt.

It:

* does not create a new Attempt;
* does not consume another quota;
* does not re-run Start Attempt eligibility;
* does not switch the Attempt to another Revision.

---

## BR-RESUME-04 — Resume Does Not Reset Time

Resuming an Attempt does not reset or extend its remaining time.

The original Attempt deadline remains authoritative.

---

# 12. Time and Expiration Rules

## BR-TIME-01 — Time Limit Is Revision-Specific

A Quiz Revision may define a time limit.

When configured:

```text
timeLimit > 0
```

When:

```text
timeLimit = null
```

the Attempt has unlimited duration.

---

## BR-TIME-02 — Time Limit Starts with the Attempt

When a timed QuizAttempt starts, the timer starts from the Attempt's `startedAt`.

The Attempt deadline is based on:

```text
startedAt + Revision.timeLimit
```

---

## BR-TIME-03 — Time Limit Uses Real Elapsed Time

The time limit continues to run in real time.

Leaving the Quiz, closing the browser, or losing network connectivity does not pause the timer.

---

## BR-TIME-04 — Server Time Is the Source of Truth

Server-side time is authoritative for determining whether the Attempt has reached its deadline.

If the server determines that the deadline has passed, the Attempt is expired.

---

## BR-TIME-05 — Deadline Terminates the Attempt

When the time limit is reached, the Attempt becomes:

```text
EXPIRED
```

Expiration is based on the Attempt deadline rather than User interaction.

---

## BR-TIME-06 — Expired Attempt Cannot Be Resumed

An `EXPIRED` Attempt is terminal.

It cannot be resumed.

If the User is eligible for another Attempt, a new QuizAttempt must be created.

---

## BR-TIME-07 — Expired Attempt Is Evaluated

When an Attempt expires:

1. the current valid responses are evaluated;
2. unanswered Questions receive zero score;
3. QuestionResults are produced for every Question;
4. totalScore is calculated;
5. Assessment Result is determined according to `passingPercentage`.

---

# 13. Completion Policy Rules

## BR-COMPLETION-01 — Completion Policy

A Quiz Revision defines how unanswered Questions affect manual submission.

The supported concepts are:

```text
REQUIRED_ALL
OPTIONAL
```

---

## BR-COMPLETION-02 — REQUIRED_ALL

Under `REQUIRED_ALL`:

* all Questions must be answered before manual submission is accepted;
* the User may leave Questions unanswered while the Attempt is `IN_PROGRESS`;
* skipping is allowed as navigation;
* unanswered Questions receive zero score if the Attempt expires.

---

## BR-COMPLETION-03 — OPTIONAL

Under `OPTIONAL`:

* manual submission is allowed even when Questions remain unanswered;
* unanswered Questions receive zero score;
* unanswered Questions produce `QuestionResult` records.

---

## BR-COMPLETION-04 — Expiration Is Independent of Completion Policy

Completion Policy controls manual submission.

It does not prevent time expiration.

Therefore, even under `REQUIRED_ALL`, a timed-out Attempt becomes:

```text
EXPIRED
```

and is evaluated.

---

# 14. Answer Rules

## BR-ANSWER-01 — One Current Answer per Question

A QuizAttempt maintains at most one current UserAnswer for each Question.

```text
One current UserAnswer
per Question
per Attempt
```

---

## BR-ANSWER-02 — Answers May Be Changed

While an Attempt is `IN_PROGRESS`, the User may change the current response to a Question.

The latest valid response becomes the current response.

---

## BR-ANSWER-03 — Answers May Be Cleared

A User may clear the current response.

Clearing a response returns the Question to an unanswered state.

---

## BR-ANSWER-04 — Skip Is Not an Answer State

Skipping a Question is navigation behavior.

The domain does not persist a separate:

```text
SKIPPED
```

answer state.

For assessment purposes, a Question is:

```text
ANSWERED
or
UNANSWERED
```

---

## BR-ANSWER-05 — No Answer History

The target model does not maintain an answer-change history.

Only the current response is required while the Attempt is `IN_PROGRESS`.

---

## BR-ANSWER-06 — No Correctness Evaluation During Attempt

Correctness is not evaluated while the Attempt remains `IN_PROGRESS`.

Evaluation occurs when the Attempt reaches:

```text
SUBMITTED
or
EXPIRED
```

---

## BR-ANSWER-07 — Answer Reference Integrity

A UserAnswer may only reference:

* a Question belonging to the Attempt's Revision;
* Answers belonging to that Question.

Cross-Revision or cross-Question Answer references are invalid.

---

# 15. Scoring Rules

## BR-SCORING-01 — Question Has Fixed Positive Score

Each Question has a fixed positive score defined by its Quiz Revision.

---

## BR-SCORING-02 — Single Choice Scoring

For `SINGLE_CHOICE`:

```text
correct answer
    → full Question score

incorrect answer
    → 0
```

---

## BR-SCORING-03 — True/False Scoring

For `TRUE_FALSE`:

```text
correct answer
    → full Question score

incorrect answer
    → 0
```

---

## BR-SCORING-04 — Multiple Choice Scoring

For `MULTIPLE_CHOICE`:

```text
selected answer set
        =
correct answer set
        ↓
full Question score
```

Otherwise:

```text
0
```

The comparison is based on the exact selected set.

---

## BR-SCORING-05 — No Partial Credit

A Question is scored using full-score or zero-score evaluation.

There is no partial credit.

---

## BR-SCORING-06 — No Negative Marking

Incorrect answers do not reduce the score below zero.

There is no negative marking.

---

## BR-SCORING-07 — Total Score

When an Attempt is evaluated:

```text
totalScore
=
sum(QuestionResult.earnedScore)
```

---

## BR-SCORING-08 — Total Score Is Historical

Once an Attempt has reached an assessment-ending state, its final `totalScore` is a historical fact.

A later Quiz Revision must not change that score.

---

## BR-SCORING-09 — Score Percentage Is Derived

Score percentage is derived from:

```text
(totalScore / QuizRevision.maxScore) × 100
```

using the exact Revision associated with the Attempt.

Score percentage is not an independent source-of-truth field.

---

# 16. Question Result Rules

## BR-RESULT-01 — QuestionResult Is Assessment Evidence

A `QuestionResult` represents the historical assessment outcome for one Question in one Attempt.

It is not:

* Learner Progress;
* Competency;
* Learner Current Level.

---

## BR-RESULT-02 — Every Question Produces a Result

When an Attempt is evaluated, every Question in the associated Revision produces exactly one `QuestionResult`.

This includes unanswered Questions.

---

## BR-RESULT-03 — QuestionResult Stores Evaluated Selection

For an answered Question, `QuestionResult` stores the selected Answer identities used during evaluation.

The stored selection is historical evidence of what was evaluated.

---

## BR-RESULT-04 — Answered Correctly

For a correctly answered Question:

```text
responseStatus = ANSWERED
isCorrect = true
earnedScore = full Question score
```

---

## BR-RESULT-05 — Answered Incorrectly

For an incorrectly answered Question:

```text
responseStatus = ANSWERED
isCorrect = false
earnedScore = 0
```

---

## BR-RESULT-06 — Unanswered

For an unanswered Question:

```text
responseStatus = UNANSWERED
selectedAnswerIds = empty
isCorrect = null
earnedScore = 0
```

---

## BR-RESULT-07 — QuestionResult Is Immutable

Once produced for an evaluated Attempt, a QuestionResult is historical assessment evidence and must not be recalculated against a later Revision.

---

# 17. Submission Rules

## BR-SUBMIT-01 — Manual Submission

A User may submit an `IN_PROGRESS` Attempt when the applicable Completion Policy permits submission.

For `REQUIRED_ALL`, all Questions must be answered.

For `OPTIONAL`, unanswered Questions are allowed.

---

## BR-SUBMIT-02 — Submission Ends the Attempt

A valid manual submission transitions the Attempt from:

```text
IN_PROGRESS
```

to:

```text
SUBMITTED
```

The Attempt is then terminal.

---

## BR-SUBMIT-03 — Submission Is Atomic

Final submission is one logical business operation:

```text
Evaluate Questions
      ↓
Create QuestionResults
      ↓
Calculate totalScore
      ↓
Determine AssessmentResult
      ↓
Complete Attempt
```

The Attempt must not become partially evaluated.

---

# 18. Cancellation Rules

## BR-CANCEL-01 — User May Cancel Own Attempt

A User may intentionally cancel their own `IN_PROGRESS` QuizAttempt.

Administrative or Instructor cancellation is outside the current scope.

---

## BR-CANCEL-02 — Cancellation Is Intentional

Cancellation is an explicit User action.

The User interface should confirm the User's intention before cancellation.

---

## BR-CANCEL-03 — Accidental Interruption Does Not Cancel

The following do not automatically cancel an Attempt:

* browser closure;
* network disconnection;
* leaving the Quiz temporarily.

---

## BR-CANCEL-04 — Cancelled Attempts Are Terminal

A `CANCELLED` Attempt cannot be resumed or submitted.

If the User is eligible for another Attempt, a new QuizAttempt must be created.

---

## BR-CANCEL-05 — Cancellation Does Not Produce Assessment Result

A cancelled Attempt does not produce:

```text
PASSED
FAILED
```

It produces no Assessment Result.

It also does not produce final QuestionResults.

---

## BR-CANCEL-06 — Cancellation Does Not Return Quota

The quota consumed when the Attempt was created remains consumed after cancellation.

---

# 19. Revision Changes During an Active Attempt

## BR-REV-ATTEMPT-01 — Publishing a New Revision Does Not Invalidate Active Attempts

Publishing a new Quiz Revision does not automatically terminate or invalidate existing `IN_PROGRESS` Attempts.

An existing Attempt may continue and be submitted against its original Revision.

---

## BR-REV-ATTEMPT-02 — Existing Attempt Remains Bound to Original Revision

An active Attempt continues to use:

* the original Questions;
* the original Answer definitions;
* the original Question scores;
* the original maximum score;
* the original passing percentage;
* the original time limit;
* the original completion policy;
* the original applicable Attempt Policy.

The new Revision applies to future Attempts.

---

## BR-REV-ATTEMPT-03 — User May Be Informed of Revision Change

When a newer Revision has been published while a User has an active Attempt, the User may be informed that the Quiz has been updated.

This notification does not change the existing Attempt.

---

# 20. Best Score Rules

## BR-BEST-SCORE-01 — Best Score Is Derived

Best Score is not stored as a field on an individual QuizAttempt.

It is derived across eligible historical Attempts for:

```text
User × Published Quiz Revision
```

---

## BR-BEST-SCORE-02 — Highest Score Wins

Best Score is:

```text
MAX(totalScore)
```

across eligible evaluated Attempts.

Example:

```text
Attempt 1 → 70
Attempt 2 → 85
Attempt 3 → 60

Best Score = 85
```

A later lower-scoring Attempt does not overwrite a previous higher score.

---

## BR-BEST-SCORE-03 — Cancelled Attempts Are Excluded

Cancelled Attempts do not contribute to Best Score because they do not produce an evaluated assessment result.

---

## BR-BEST-SCORE-04 — Historical Attempts Remain Preserved

All historical Attempts remain stored as individual assessment records.

Best Score is a derived view over those Attempts rather than a replacement for them.

---

# 21. Completion Requirement Rules

## BR-COMPLETE-01 — Quiz Is Not Inherently Mandatory

A Quiz may be used as:

```text
Practice
Diagnostic
Completion Requirement
```

A Quiz is not inherently mandatory simply because it exists within the Learning Context.

---

## BR-COMPLETE-02 — Completion Requirement May Belong to Lesson or Section

A Completion Requirement may be associated with:

```text
Lesson
or
Section
```

---

## BR-COMPLETE-03 — Completion Requirement References a Specific Revision

A Completion Requirement references a specific Published QuizRevision.

It does not reference only the stable Quiz identity.

```text
Completion Requirement
        ↓
Published QuizRevision
```

---

## BR-COMPLETE-04 — Passing Attempt Satisfies Requirement

A Completion Requirement is satisfied when there is at least one valid:

```text
PASSED QuizAttempt
```

for the required Published QuizRevision.

The latest Attempt does not need to be the passing Attempt.

---

## BR-COMPLETE-05 — Completion Is Historical

Once a Completion Requirement has been satisfied and the associated Lesson or Section completion is achieved, later QuizRevision publication does not automatically revoke or rewrite the completed state.

Historical completion remains a historical fact.

---

## BR-COMPLETE-06 — Multiple Completion Requirements

Multiple Completion Requirements are supported.

The default relationship is:

```text
AND
```

meaning all mandatory requirements must be satisfied.

A simple `OR` relationship may be supported when a concrete business need exists.

No nested generic expression engine is introduced.

---

## BR-COMPLETE-07 — Completion Requirement Is Not Learning Prerequisite

Completion Requirement and Learning Prerequisite are separate domain concepts.

```text
Completion Requirement
    → contributes to completing a Lesson or Section

Learning Prerequisite
    → blocks a learner from starting a new activity or assessment
```

One must not be treated as the other.

---

# 22. Learning Completion Impact Rules

## BR-IMPACT-01 — Revision Completion Impact

A QuizRevision may explicitly declare whether the change affects learning completion.

Supported concepts are:

```text
NO_IMPACT
AFFECTS_COMPLETION
```

---

## BR-IMPACT-02 — NO_IMPACT

When a Revision change is marked:

```text
NO_IMPACT
```

it does not alter the validity of existing completion state.

---

## BR-IMPACT-03 — AFFECTS_COMPLETION

When a Revision change is marked:

```text
AFFECTS_COMPLETION
```

it may affect completion evaluation for learners who have not yet completed the associated requirement.

It does not:

* delete historical evidence;
* rewrite historical Attempts;
* reset the entire Course;
* automatically invalidate already achieved completion.

---

## BR-IMPACT-04 — Historical Completion Is Preserved

A later QuizRevision does not revoke an already achieved completion solely because the assessment definition changed.

Material completion impact concerns learners who have not yet completed the applicable requirement.

---

# 23. Access and Eligibility Rules

## BR-ACCESS-01 — Visibility Is Not Eligibility

Visibility determines the high-level access mode of a Quiz.

It does not by itself determine whether the User may start an Attempt.

```text
Visibility
    ≠
Eligibility
```

---

## BR-ACCESS-02 — Access Requirements Are Separate

Access Requirements are distinct from:

```text
Visibility
Availability
Learning Prerequisites
Current Level
```

Multiple Access Requirements may apply.

The default relationship is:

```text
AND
```

A hard access failure blocks the Attempt.

There is no general "Continue Anyway" behavior for hard access failures.

---

## BR-ACCESS-03 — Course Access Is Distinct from Quiz Eligibility

Valid Course learning access does not automatically make a User eligible to start every Quiz.

Additional Access Requirements or Learning Prerequisites may still block the Attempt.

Course completion does not automatically revoke Course access.

---

## BR-ACCESS-04 — Current Level Is a Soft Recommendation

Current Level is not a hard Quiz prerequisite in the current target model.

When a learner's Current Level is below the recommended level, the system may provide a warning.

Conceptually:

```text
Recommended level not met
        ↓
Warning
   /        \
Continue    Go back
anyway
```

Only choosing to continue creates the Attempt and consumes quota.

The exact Current Level calculation and source remain outside the Quiz model.

---

# 24. Practice and Formal Assessment Rules

## BR-ASSESSMENT-POLICY-01 — Different Assessment Policies Are Allowed

Practice-oriented and formal assessment-oriented Quizzes may use different policies for:

* retry;
* maximum Attempts;
* feedback;
* access;
* eligibility;
* result visibility.

These differences are policy concerns.

---

## BR-ASSESSMENT-POLICY-02 — No Separate AssessmentType Model Yet

The current target model does not establish a separate:

```text
AssessmentType
PracticeQuiz
Exam
```

domain entity.

Such a model must not be introduced without a concrete business distinction requiring it.

---

## BR-ASSESSMENT-POLICY-03 — Feedback Is Separate from QuestionResult

QuestionResult represents assessment evidence.

Whether the User can view:

* correct Answers;
* detailed QuestionResults;
* explanations;
* feedback

is a separate feedback/access policy.

Therefore:

```text
QuestionResult
    ≠
Feedback Policy
```

---

# 25. Learning State Separation Rules

## BR-LEARNING-01 — Assessment Result Is Not Competency

`PASSED`, `FAILED`, and `NONE` are outcomes of one assessment execution.

They must not automatically be interpreted as a Competency state.

---

## BR-LEARNING-02 — Score Is Not Current Level

A Quiz score must not automatically be interpreted as the User's Current Level.

---

## BR-LEARNING-03 — QuestionResult Is Learning Evidence

QuestionResults are historical assessment evidence.

They may later be consumed by broader Learning State or analytics capabilities.

However, a QuestionResult does not directly define:

* Competency;
* Current Level;
* Learning Direction.

---

# 26. Rule Summary

The core Quiz business model can be summarized as:

```text
Quiz
│
├── Identity
├── Ownership
├── Governance
├── Visibility
├── Availability
├── Quiz Lifecycle
│
└── QuizRevision
      │
      ├── Revision Lifecycle
      │     DRAFT
      │       ↓
      │    IN_REVIEW
      │       ↓
      │    PUBLISHED
      │       ↓
      │    HISTORICAL
      │
      ├── Questions
      │     └── Answers
      │
      ├── maxScore
      │     → derived from Question scores
      │
      ├── passingPercentage?
      ├── timeLimit?
      ├── maxAttempts?
      └── completionPolicy
              │
              ▼
        Create Attempt
              │
        eligibility checks
              │
        quota consumed
              │
              ▼
         IN_PROGRESS
         /    |     \
        /     |      \
   SUBMIT   EXPIRE   CANCEL
      │       │        │
      ▼       ▼        ▼
   evaluate evaluate  no evaluation
      │       │
      └───┬───┘
          ▼
   QuestionResults
          │
          ▼
      totalScore
          │
   passingPercentage?
      /          \
    yes           no
     │             │
PASSED/FAILED    NONE
```

The fundamental historical rule is:

```text
Quiz
└── QuizRevision
        │
        │ exact binding
        ▼
   QuizAttempt
        │
        ├── UserAnswer
        ├── QuestionResult
        ├── totalScore
        └── AssessmentResult
```

A new Quiz Revision never changes the definition, evaluation, or historical result of an existing QuizAttempt.
