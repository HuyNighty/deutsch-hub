# Quiz Business Rules

## 1. Purpose

This document defines the business rules governing Quiz definitions, Quiz Revisions, Questions, QuizAttempts, assessment results, retry policies, Quiz availability, and learner access within the Learning context.

The rules are based on the current DeutschHub implementation and the target domain decisions established during Quiz domain discovery.

The document distinguishes confirmed business rules from decisions that remain open.

---

## 2. Quiz Definition Rules

### BR-QUIZ-01 — Quiz Is an Independent Assessment Definition

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

Any modification to a Published Quiz definition must be performed through a new Draft Revision.

The existing Published Revision remains unchanged.

This protects the integrity of existing QuizAttempts that depend on the previously published definition.

---

### BR-QUIZ-03 — Quiz Visibility Is Separate from Availability

Quiz visibility and Quiz availability are independent concepts.

Visibility determines the high-level audience or discoverability of the Quiz.

Availability determines whether the Quiz currently accepts new Attempts.

Therefore:

```text
Visibility ≠ Availability
```

Changing Quiz availability does not change the lifecycle of existing QuizAttempts.

---

### BR-QUIZ-04 — Quiz Visibility

The supported Quiz visibility concepts are:

```text
PRIVATE
COURSE_ONLY
PUBLIC
```

The business meaning is:

* `PRIVATE`: ordinary learners cannot access the Quiz.
* `PUBLIC`: the Quiz may proceed to the applicable eligibility checks.
* `COURSE_ONLY`: the User must have valid Course learning access.

The exact access-check implementation is outside the Quiz aggregate.

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

This includes:

* maximum score;
* passing score, when defined;
* time limit;
* maximum attempts;
* completion policy;
* Questions;
* Answer definitions;
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

### BR-QUESTION-07 — Supported Question Types

The currently supported Question types are:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

Text-based Questions are not part of the current Quiz scope.

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

```text
sum(Question.score) = QuizRevision.maxScore
```

---

### BR-PUBLISH-04 — Published Definition Cannot Be Modified

After publication, the Quiz Revision is immutable.

Changes must be made through a new Draft Revision.

---

## 6. Passing Score Rules

### BR-SCORE-01 — Passing Score Is Optional

A Quiz Revision does not have to define a passing score.

This supports both practice-oriented and assessment-oriented Quizzes.

```text
Practice
passingScore = none

Assessment
passingScore = defined
```

The absence of a passing score means that the Attempt may still produce a numeric score but does not produce a `PASSED` or `FAILED` Assessment Result.

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

When a Quiz Revision has no passing score:

```text
AssessmentResult = none
```

The QuizAttempt may still have a numeric `totalScore`.

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

### BR-SCORE-06 — Assessment Result Is Determined at Attempt Completion

Assessment Result must not be determined while an Attempt remains `IN_PROGRESS`.

It is determined only when the Attempt reaches an assessment-ending state:

```text
SUBMITTED
or
EXPIRED
```

A `CANCELLED` Attempt does not produce an Assessment Result.

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
* completion policy;
* attempt policy;
* other applicable assessment rules

remain determined by its associated Revision.

---

### BR-ATTEMPT-04 — Quiz Must Be Active for New Attempts

A Quiz must be active before a new QuizAttempt may be created.

Deactivating a Quiz prevents creation of new Attempts.

Deactivation does not automatically terminate existing `IN_PROGRESS` Attempts.

---

### BR-ATTEMPT-05 — Attempt Creation Requires Eligibility

Before creating a new QuizAttempt, the applicable eligibility conditions must be satisfied.

Conceptually:

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
```

The exact prerequisite and unlock conditions remain OPEN.

No specific prerequisite model is assumed until those business conditions are defined.

---

## 8. Attempt Limit and Retry Rules

### BR-ATTEMPT-LIMIT-01 — Multiple Attempts

A Quiz Revision may allow multiple Attempts.

The maximum number of Attempts is defined by the applicable assessment policy.

Examples:

```text
Practice
maxAttempts = 10

Formal Assessment
maxAttempts = 1

Other Assessment
maxAttempts = 3
```

These examples illustrate policy possibilities and are not fixed system defaults.

---

### BR-ATTEMPT-LIMIT-02 — Attempt Quota Is Per User and Published Revision

The attempt quota applies to:

```text
User × Published Quiz Revision
```

Historical Attempts remain associated with the Revision from which they were created.

A new Published Revision has its own applicable attempt quota.

---

### BR-ATTEMPT-LIMIT-03 — Attempt Quota Is Consumed at Creation

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

### BR-ATTEMPT-LIMIT-04 — Cancelled Attempts Do Not Return Quota

A `CANCELLED` Attempt does not return previously consumed quota.

For example:

```text
maxAttempts = 3

Attempt 1 → CANCELLED → quota consumed: 1
Attempt 2 → FAILED    → quota consumed: 2
Attempt 3 → PASSED    → quota consumed: 3
```

Cancellation does not restore the consumed quota.

---

### BR-ATTEMPT-LIMIT-05 — Passed Attempt Retry

A User may retry after `PASSED` when the applicable Attempt Policy and remaining quota permit it.

Passing does not universally prevent another Attempt.

The exact retry policy remains configurable at the assessment-policy level.

---

### BR-ATTEMPT-LIMIT-06 — Failed Attempt Retry

A User may retry after `FAILED` when the applicable Attempt Policy and remaining quota permit it.

A policy may allow immediate retry or impose a temporal restriction.

The exact temporal retry policy remains OPEN.

---

### BR-ATTEMPT-LIMIT-07 — Retry Creates a New Attempt

Retrying a Quiz creates a new QuizAttempt.

A previous Attempt is never reopened or converted into a new Attempt.

```text
Attempt 1
    ↓
FAILED

Retry
    ↓
Attempt 2
    ↓
IN_PROGRESS
```

Answers from the previous Attempt are not carried into the new Attempt.

---

## 9. Concurrent Attempt Rules

### BR-CONCURRENT-01 — One Active Attempt

A User may have at most one `IN_PROGRESS` QuizAttempt for the same Quiz at a time.

Allowed:

```text
Quiz A

User
├── Attempt 1 → SUBMITTED
├── Attempt 2 → EXPIRED
└── Attempt 3 → IN_PROGRESS
```

Not allowed:

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

### BR-RESUME-03 — Resume Uses the Same Attempt

Resuming an Attempt continues the existing QuizAttempt.

It does not create a new Attempt and does not consume another quota.

A new quota is consumed only when a new QuizAttempt is created.

---

## 11. Time Limit Rules

### BR-TIME-01 — Time Limit Starts with the Attempt

When a Quiz Revision has a time limit, the timer starts when the QuizAttempt starts.

---

### BR-TIME-02 — Time Limit Uses Real Elapsed Time

The time limit continues to run in real time.

Leaving the Quiz, closing the browser, or losing network connectivity does not pause the timer.

```text
Attempt starts
      ↓
time continues
      ↓
temporary interruption
      ↓
time continues
      ↓
deadline
```

---

### BR-TIME-03 — Server Time Is the Source of Truth

The server-side time is the source of truth for determining whether the Attempt has reached its deadline.

If a User sends an operation before the deadline but the server processes the operation after the deadline, the Attempt is considered expired.

---

### BR-TIME-04 — Deadline Terminates the Attempt

When the time limit is reached, the Attempt becomes:

```text
EXPIRED
```

Expiration is based on the Attempt deadline rather than on User interaction.

---

### BR-TIME-05 — Expired Attempt Cannot Be Resumed

An `EXPIRED` Attempt is terminal.

It cannot be resumed.

If the User is eligible for another Attempt, a new QuizAttempt must be created.

---

### BR-TIME-06 — Expired Attempt Produces Assessment Result When Applicable

If the Quiz Revision defines a passing score:

```text
EXPIRED
+
passingScore exists
        ↓
calculate final score
        ↓
PASSED or FAILED
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

### BR-TIME-07 — Unanswered Questions on Expiration

When an Attempt expires, unanswered Questions receive:

```text
earnedScore = 0
```

They still produce a `QuestionResult` with:

```text
responseStatus = UNANSWERED
isCorrect = null
earnedScore = 0
```

---

## 12. Completion Policy Rules

### BR-COMPLETION-01 — Completion Policy

A Quiz Revision defines how unanswered Questions affect manual submission.

The supported concepts are:

```text
REQUIRED_ALL
OPTIONAL
```

---

### BR-COMPLETION-02 — REQUIRED_ALL

Under `REQUIRED_ALL`:

* all Questions must be answered before manual submission is accepted;
* the User may leave Questions unanswered while the Attempt is `IN_PROGRESS`;
* skipping is allowed as navigation;
* unanswered Questions receive zero score if the Attempt expires.

---

### BR-COMPLETION-03 — OPTIONAL

Under `OPTIONAL`:

* manual submission is allowed even when Questions remain unanswered;
* unanswered Questions receive zero score;
* unanswered Questions produce `QuestionResult` records.

---

### BR-COMPLETION-04 — Expiration Is Independent of Completion Policy

Completion Policy controls manual submission.

It does not prevent time expiration.

Therefore, even under `REQUIRED_ALL`, a timed-out Attempt becomes:

```text
EXPIRED
```

rather than remaining `IN_PROGRESS` until every Question is answered.

---

## 13. Answer Rules

### BR-ANSWER-01 — One Current Answer per Question

A QuizAttempt maintains one current UserAnswer for each answered Question.

```text
One UserAnswer
per Question
per Attempt
```

---

### BR-ANSWER-02 — Answers May Be Changed

While an Attempt is `IN_PROGRESS`, the User may change the current answer to a Question.

The latest valid answer becomes the current response.

---

### BR-ANSWER-03 — Answers May Be Cleared

A User may clear the current answer.

Clearing an answer returns the Question to an unanswered state.

---

### BR-ANSWER-04 — Skip Is Not an Answer State

Skipping a Question is navigation behavior.

The domain does not persist a separate `SKIPPED` answer state.

A Question is either:

```text
ANSWERED
```

or:

```text
UNANSWERED
```

for assessment-result purposes.

---

### BR-ANSWER-05 — No Answer History

The current target model does not maintain an answer-change history.

Only the current response is required while the Attempt is `IN_PROGRESS`.

---

## 14. Scoring Rules

### BR-SCORING-01 — Question Has Fixed Score

Each Question has a fixed score defined by the Quiz Revision.

---

### BR-SCORING-02 — Single Choice Scoring

For `SINGLE_CHOICE`:

```text
correct answer
    → full Question score

incorrect answer
    → 0
```

---

### BR-SCORING-03 — True/False Scoring

For `TRUE_FALSE`:

```text
correct answer
    → full Question score

incorrect answer
    → 0
```

---

### BR-SCORING-04 — Multiple Choice Scoring

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

Partial credit is not supported.

---

### BR-SCORING-05 — No Partial Credit

A Question is scored using full-score or zero-score evaluation.

There is no partial credit.

---

### BR-SCORING-06 — No Negative Marking

Incorrect answers do not reduce the score below zero.

There is no negative marking.

---

### BR-SCORING-07 — Total Score

When the Attempt reaches an assessment-ending state:

```text
totalScore
=
sum(QuestionResult.earnedScore)
```

---

### BR-SCORING-08 — Total Score Is Historical

Once an Attempt has ended with an assessment outcome, its final `totalScore` represents a historical fact.

A later Quiz Revision must not change that score.

---

### BR-SCORING-09 — Percentage Is Derived

The system does not need to store score percentage as an independent historical fact.

Percentage may be derived using:

```text
totalScore / QuizRevision.maxScore
```

where the exact Quiz Revision is the Revision associated with the Attempt.

---

## 15. Question Result Rules

### BR-RESULT-01 — QuestionResult Is Assessment Evidence

A `QuestionResult` represents the assessment outcome for one Question in one Attempt.

It is not:

* Learner Progress;
* Competency;
* Learner Current Level.

---

### BR-RESULT-02 — Every Question Produces a Result

When an Attempt reaches an assessment-ending state, every Question in the associated Revision produces a `QuestionResult`.

This includes unanswered Questions.

---

### BR-RESULT-03 — Answered Correctly

For a correctly answered Question:

```text
responseStatus = ANSWERED
isCorrect = true
earnedScore = full Question score
```

---

### BR-RESULT-04 — Answered Incorrectly

For an incorrectly answered Question:

```text
responseStatus = ANSWERED
isCorrect = false
earnedScore = 0
```

---

### BR-RESULT-05 — Unanswered

For an unanswered Question:

```text
responseStatus = UNANSWERED
isCorrect = null
earnedScore = 0
```

---

## 16. Submission Rules

### BR-SUBMIT-01 — Manual Submission

A User may submit an `IN_PROGRESS` Attempt when the applicable Completion Policy permits submission.

For `REQUIRED_ALL`, all Questions must be answered.

For `OPTIONAL`, unanswered Questions are allowed.

---

### BR-SUBMIT-02 — Submission Ends the Attempt

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

### BR-SUBMIT-03 — Score Is Calculated at Completion

When an Attempt is submitted:

1. Question Results are determined.
2. Total Score is calculated.
3. Assessment Result is determined when a passing score exists.
4. The Attempt becomes `SUBMITTED`.

---

## 17. Cancellation Rules

### BR-CANCEL-01 — User May Cancel Own Attempt

A User may intentionally cancel their own `IN_PROGRESS` QuizAttempt.

Administrative or Instructor cancellation is outside the current scope.

No cancellation reason is required by the current business model.

---

### BR-CANCEL-02 — Cancellation Is Intentional

Cancellation is an explicit User action.

The frontend should confirm the User's intention before cancellation.

---

### BR-CANCEL-03 — Accidental Interruption Does Not Cancel

The following do not automatically cancel an Attempt:

* browser closure;
* network disconnection;
* leaving the Quiz temporarily.

The Attempt remains `IN_PROGRESS` unless another valid terminal condition occurs.

---

### BR-CANCEL-04 — Cancelled Attempts Are Terminal

A `CANCELLED` Attempt cannot be resumed.

If the User is eligible for another Attempt, a new QuizAttempt must be created.

---

### BR-CANCEL-05 — Cancellation Does Not Produce Assessment Result

A cancelled Attempt does not produce:

```text
PASSED
```

or:

```text
FAILED
```

It is not considered a completed assessment.

---

### BR-CANCEL-06 — Cancellation Does Not Return Quota

The quota consumed when the Attempt was created remains consumed after cancellation.

---

## 18. Revision Changes During an Active Attempt

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

### BR-REV-ATTEMPT-02 — Existing Attempt Remains Bound to Original Revision

An active Attempt continues to use:

* the original Questions;
* the original Answer definitions;
* the original Question scores;
* the original maximum score;
* the original passing score;
* the original time limit;
* the original completion policy;
* the original applicable Attempt Policy.

The new Revision applies to future Attempts.

---

### BR-REV-ATTEMPT-03 — User May Be Informed of Revision Change

When a newer Revision has been published while a User has an active Attempt, the User may be informed that the Quiz has been updated.

This notification does not change the existing Attempt.

---

### BR-REV-ATTEMPT-04 — Prerequisite Assessment Exception

If a Quiz is used as a prerequisite for progressing to a new learning stage, the business may require the User to satisfy that prerequisite using the applicable newer Revision.

This does not change the historical identity or Revision association of an existing Attempt.

The exact prerequisite and learning-stage eligibility rules remain outside the current Quiz model.

---

## 19. Best Score Rules

### BR-BEST-SCORE-01 — Best Score Is Derived

Best Score is not stored as a field on an individual QuizAttempt.

It is derived across historical Attempts for:

```text
User × Published Quiz Revision
```

---

### BR-BEST-SCORE-02 — Highest Score Wins

Best Score is:

```text
MAX(totalScore)
```

across the applicable historical Attempts.

Example:

```text
Attempt 1 → 70
Attempt 2 → 85
Attempt 3 → 60

Best Score = 85
```

A later lower-scoring Attempt does not overwrite a previous higher score.

---

### BR-BEST-SCORE-03 — Historical Attempts Remain Preserved

All historical Attempts remain stored as individual assessment records.

Best Score is a derived view over those Attempts rather than a replacement for them.

---

## 20. Practice and Formal Assessment Rules

### BR-ASSESSMENT-POLICY-01 — Practice and Formal Assessment May Have Different Policies

Practice-oriented and formal assessment-oriented Quizzes may use different policies for:

* retry;
* maximum Attempts;
* feedback;
* access;
* eligibility;
* result visibility.

These differences are policy concerns.

---

### BR-ASSESSMENT-POLICY-02 — No Separate Assessment Type Model Yet

The current domain does not establish a separate `AssessmentType`, `PracticeQuiz`, or `Exam` entity.

Such a model must not be assumed until the business requirements define a concrete domain distinction requiring it.

---

### BR-ASSESSMENT-POLICY-03 — QuestionResult Is Independent of Feedback Policy

QuestionResult represents assessment evidence.

Whether the User can see detailed Question Results or correct answers is a separate feedback/access policy.

Therefore:

```text
QuestionResult
    ≠
Feedback Policy
```

A practice Quiz may expose detailed Question Results, while a formal assessment may restrict detailed feedback.

---

## 21. Learning State Separation Rules

### BR-LEARNING-01 — Assessment Result Is Not Competency

`PASSED` or `FAILED` must not automatically be interpreted as a Competency state.

---

### BR-LEARNING-02 — Score Is Not Current Level

A Quiz score must not automatically be interpreted as the User's Current Level.

---

### BR-LEARNING-03 — Question Result Is Evidence

Question Results may later be consumed by learner-state or analytics capabilities.

However, a single QuestionResult must not directly define:

* Competency;
* Current Level;
* Learning Direction.

Those concepts belong to broader Learning State and Learning Direction decisions.

---

## 22. Rule Summary

The core Quiz business rules can be summarized as:

```text
Quiz
│
├── owns identity, visibility, availability, and revisions
│
└── Published Revision
        │
        ├── Questions
        ├── Answer definitions
        ├── Time limit
        ├── Passing score?
        ├── Maximum attempts
        └── Completion policy
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
    score    score    no result
        │       │
        └───┬───┘
            ▼
     QuestionResult
            │
            ▼
       totalScore
            │
      passingScore?
        /       \
      yes        no
       │          │
 PASSED/FAILED   no AssessmentResult
```

The fundamental historical rule is:

```text
Quiz Revision
      │
      │ exact binding
      ▼
QuizAttempt
      │
      ├── UserAnswer
      ├── QuestionResult
      ├── totalScore
      └── AssessmentResult?
```

A new Quiz Revision never changes the definition or result of an existing Attempt.
