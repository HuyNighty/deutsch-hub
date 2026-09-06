# Quiz Business Actions

## 1. Purpose

This document defines the business actions of the Quiz domain.

It describes the actions that actors can perform throughout the Quiz lifecycle, including:

- creating a Quiz;
- managing Draft Revisions;
- managing Questions and their response configuration;
- submitting a Revision for review;
- reviewing a Revision;
- requesting changes;
- withdrawing a submission;
- approving and publishing a Revision;
- discarding a Draft Revision;
- activating and deactivating a Quiz.

The document also defines the business rules that govern the relationship between Quiz, Quiz Revision, Question, Question Response, and Quiz Attempt.

This document describes the target business behavior. It does not imply that every action is already implemented in the current codebase.

---

# 2. Domain Scope

A Quiz is an independent learning assessment.

A Quiz may be used independently or placed inside a Course through a `LessonItem`.

The Quiz is not owned by a Course or Lesson.

The relationship between a Lesson and a Quiz is represented through the LessonItem:

```text
Lesson
└── LessonItem
     └── quizId
````

The Quiz remains an independent Aggregate Root.

The target conceptual structure is:

```text
Quiz
├── Identity
├── Governance / Lifecycle
├── Visibility
├── Availability
└── Revisions
     ├── Draft Revision
     └── Published Revision
          └── Questions
               └── Answer Options
```

---

# 3. Core Concepts

## 3.1 Quiz

`Quiz` represents the identity and governance container of an assessment.

Quiz is responsible for concepts that remain stable across revisions, including:

* Quiz identity;
* creator/author;
* visibility;
* availability;
* revision lifecycle;
* deletion and audit information.

The actual assessment definition belongs to a `QuizRevision`.

A Quiz is therefore not the same as a single immutable assessment definition.

---

## 3.2 Quiz Revision

`QuizRevision` represents a concrete version of a Quiz's assessment definition.

A Revision contains the assessment data that may change between versions, including:

* title;
* description;
* difficulty;
* time limit;
* maximum score;
* optional passing score;
* maximum attempts;
* Questions;
* response configuration for Questions.

A Quiz may have:

* at most one Draft Revision;
* at most one Published Revision.

A Draft Revision and a Published Revision may coexist.

For example:

```text
Quiz
├── Draft Revision
└── Published Revision
```

A Published Revision must not be modified directly.

If the assessment definition needs to change, a new Draft Revision is created and edited. Publishing the new Revision replaces the currently active Published Revision as the current definition while preserving the historical definition.

---

## 3.3 Question

A Question is an assessment item within a Quiz Revision that presents a prompt or task to the learner, defines the expected form of response, and provides the information required to evaluate that response.

Every Question has an individual score.

The maximum score of a Quiz Revision is the sum of the scores of its Questions:

```text
Revision.maxScore
=
sum(Question.score)
```

Questions do not need to have equal scores.

---

## 3.4 Question Response

Question Response defines how the learner responds to a Question.

The current scope supports Choice Response:

```text
Question Response
└── Choice Response
    ├── SINGLE_CHOICE
    ├── MULTIPLE_CHOICE
    └── TRUE_FALSE
```

Choice Questions use Answer options.

Current rules include:

* a Question must contain at least 2 Answers;
* a Question may contain at most 6 Answers;
* at least one Answer must be correct;
* `SINGLE_CHOICE` allows at most one correct Answer;
* `TRUE_FALSE` requires exactly 2 Answers;
* `TRUE_FALSE` requires exactly one correct Answer.

Text Response is not part of the current scope.

No `TEXT`, speaking, audio, matching, or other response type is introduced by this document.

Future response types may be added when concrete business requirements exist.

---

## 3.5 Quiz Attempt

A `QuizAttempt` represents one learner's execution of a Quiz.

An Attempt can only be started from a Published Quiz Revision.

Once started, an Attempt is permanently bound to the exact Published Revision used at the time of start.

Therefore, publishing a new Revision does not change an existing Attempt.

For example:

```text
Quiz
├── Published Revision A
│    └── Attempt 1
│
└── Published Revision B
     └── Attempt 2
```

Attempt 1 continues to use Revision A even after Revision B becomes the current Published Revision.

This preserves the historical assessment definition used to evaluate the Attempt.

---

## 3.6 Attempt Status

Attempt Status represents the lifecycle state of a QuizAttempt.

The current target states are:

```text
IN_PROGRESS
SUBMITTED
EXPIRED
CANCELLED
```

`Assessment Result` is intentionally separate from Attempt Status.

---

## 3.7 Assessment Result

Assessment Result represents the outcome of an ended assessment.

Possible results are:

```text
PASSED
FAILED
```

Assessment Result is determined only when the Attempt ends through submission or expiration.

If a `passingScore` exists:

```text
totalScore >= passingScore
    → PASSED

totalScore < passingScore
    → FAILED
```

If `passingScore` does not exist, the Attempt has no Pass/Fail Assessment Result.

Pass/Fail must not be interpreted as:

* Competency;
* Current Level;
* learner proficiency.

These are separate Learning concepts.

---

# 4. Business Actions Overview

The Quiz business actions are:

```text
Quiz Business Actions
├── Create Quiz
├── Edit Draft Revision
│   ├── Update Basic Information
│   │   ├── Update Title
│   │   └── Update Description
│   ├── Configure Assessment
│   │   ├── Update Difficulty
│   │   ├── Update Time Limit
│   │   ├── Update Max Score
│   │   ├── Update Passing Score
│   │   └── Update Max Attempts
│   └── Manage Questions
│       ├── Add Question
│       ├── Update Question
│       ├── Remove Question
│       └── Configure Question Response
│           └── Choice Response
│               ├── Add Answer
│               ├── Update Answer
│               ├── Remove Answer
│               └── Mark Correct/Incorrect
├── Submit for Review
├── Withdraw Submission
├── Review Quiz
│   ├── View Revision
│   └── Provide Feedback
├── Request Changes
├── Approve Revision
├── Discard Draft Revision
├── Activate Quiz
└── Deactivate Quiz
```

---

# 5. Create Quiz

**Actor:** Learning Author

## Purpose

Create a new Quiz and its initial Draft Revision.

## Preconditions

* The actor is authorized to create learning content.

## Rules

Creating a Quiz creates:

```text
Quiz
└── Initial Draft Revision
```

The initial Draft Revision may be incomplete.

A Draft does not need to satisfy all publication requirements immediately.

In particular, the title may initially be empty or unset.

The Quiz has no Published Revision immediately after creation.

## Result

```text
Quiz
└── Draft Revision
```

The Author can continue editing the Draft Revision.

---

# 6. Edit Draft Revision

Only the current Draft Revision can be edited.

A Published Revision cannot be edited directly.

Editing a Draft Revision consists of:

```text
Edit Draft Revision
├── Update Basic Information
├── Configure Assessment
└── Manage Questions
```

The Author can edit the Draft while it is in the Draft state.

If the Revision is submitted for review, editing is no longer allowed until the submission is withdrawn or changes are requested.

---

# 7. Update Basic Information

## 7.1 Update Title

**Actor:** Learning Author

Updates the title of the current Draft Revision.

A Draft may initially have no title.

However, a valid title is required when the Revision is submitted for review.

Therefore:

```text
Create Draft
    ↓
Title may be empty
    ↓
Submit for Review
    ↓
Title must be valid
```

The title belongs to the Revision rather than Quiz identity.

---

## 7.2 Update Description

**Actor:** Learning Author

Updates the description of the current Draft Revision.

The description belongs to the Revision and may therefore differ between Quiz versions.

---

# 8. Configure Assessment

## 8.1 Update Difficulty

**Actor:** Learning Author

Updates the difficulty of the Draft Revision.

Difficulty describes the specific assessment definition and therefore belongs to the Revision.

---

## 8.2 Update Time Limit

**Actor:** Learning Author

Updates the time limit of the Draft Revision.

The time limit applies to Attempts started from that Published Revision.

Once an Attempt has started, later Revision changes do not alter the Attempt's bound Revision or its time limit.

The time limit runs in real time from the start of the Attempt.

Browser closure or network loss does not pause the time limit.

---

## 8.3 Update Max Score

**Actor:** Learning Author

Updates the maximum score of the Draft Revision.

The Revision must maintain score consistency:

```text
Revision.maxScore
=
sum(all Question scores)
```

A Revision that does not satisfy this rule cannot be submitted for review.

Each Question has an individual score.

Question scores do not need to be distributed equally.

---

## 8.4 Update Passing Score

**Actor:** Learning Author

Updates the optional passing score of the Draft Revision.

A Quiz may have no passing score.

If a passing score exists:

```text
0 ≤ passingScore ≤ maxScore
```

A Quiz without a passing score does not produce a Pass/Fail Assessment Result.

---

## 8.5 Update Max Attempts

**Actor:** Learning Author

Updates the maximum number of Attempts allowed for the Draft Revision.

`maxAttempts` is part of the assessment policy of the Revision.

The policy may differ according to the purpose of the Quiz.

Multiple Attempts are supported subject to this policy.

Whether an Attempt may be started again after a learner has already passed or failed depends on the broader assessment attempt policy.

The exact authorization boundary for changing this policy remains a Learning governance concern.

---

# 9. Manage Questions

Questions belong to the Draft Revision.

The Author may:

```text
Manage Questions
├── Add Question
├── Update Question
├── Remove Question
└── Configure Question Response
```

Published Questions cannot be modified in place.

---

## 9.1 Add Question

**Actor:** Learning Author

Adds a Question to the current Draft Revision.

The Question may initially be incomplete while the Draft is being edited.

Before submission for review, the Question must satisfy the rules of its response type.

---

## 9.2 Update Question

**Actor:** Learning Author

Updates the content or configuration of a Question in the current Draft Revision.

Question changes are only performed on the Draft Revision.

A Published Revision remains immutable.

---

## 9.3 Remove Question

**Actor:** Learning Author

Removes a Question from the Draft Revision.

After removal, the Draft must still satisfy all requirements when it is submitted for review.

In particular, the Question scores must continue to be consistent with the Revision's maximum score.

---

# 10. Configure Question Response

The current Quiz scope supports Choice Response.

```text
Configure Question Response
└── Choice Response
    ├── Add Answer
    ├── Update Answer
    ├── Remove Answer
    └── Mark Correct/Incorrect
```

No Text Response is currently defined.

---

## 10.1 Add Answer

**Actor:** Learning Author

Adds an Answer option to a Choice Question.

The Question must contain:

```text
2 ≤ number of Answers ≤ 6
```

before it can become valid for publication.

Answer content must not violate the Question's duplicate-answer rule.

---

## 10.2 Update Answer

**Actor:** Learning Author

Updates the content of an Answer within the Draft Question.

The resulting Question must remain valid when submitted for review.

Published Answer definitions cannot be modified directly.

---

## 10.3 Remove Answer

**Actor:** Learning Author

Removes an Answer from a Draft Question.

The resulting Question must satisfy the response-type rules before the Revision can be submitted for review.

---

## 10.4 Mark Correct/Incorrect

**Actor:** Learning Author

Marks an Answer as correct or incorrect.

The resulting Question must satisfy the correctness rules of its response type.

### SINGLE_CHOICE

At most one Answer may be correct.

### MULTIPLE_CHOICE

One or more Answers may be correct.

### TRUE_FALSE

Exactly one of the two Answers must be correct.

---

# 11. Submit for Review

**Actor:** Learning Author

## Purpose

Submit the current Draft Revision to the Learning Editor for review.

## Preconditions

* A Draft Revision exists.
* The actor is authorized as the Author.
* The Revision is not already under review.

## Validation

The Draft Revision must be complete enough for review.

The validation includes:

### Revision

* title is present and valid;
* required assessment configuration is valid;
* maximum score is valid;
* if `passingScore` exists, it is within the valid range;
* maximum attempt policy is valid.

### Questions

* at least one Question exists;
* every Question is valid;
* every Question has a positive score;
* Question scores sum to `maxScore`.

### Choice Response

For current Choice Questions:

* the number of Answers is between 2 and 6;
* at least one Answer is correct;
* `SINGLE_CHOICE` has at most one correct Answer;
* `TRUE_FALSE` contains exactly 2 Answers;
* `TRUE_FALSE` contains exactly one correct Answer.

Visibility and availability are separate concepts and are not part of Revision completeness.

## Result

```text
DRAFT
   ↓ Submit for Review
IN_REVIEW
```

The Author can no longer edit the Revision while it is under review.

Submitting for review does not create a new Revision.

---

# 12. Withdraw Submission

**Actor:** Learning Author

## Purpose

Allow the Author to withdraw their own submitted Revision before publication.

## Preconditions

* The Revision is currently under review.
* The actor is the Author.
* The Revision has not already been published.

## Result

```text
IN_REVIEW
    ↓ Withdraw Submission
DRAFT
```

The same Revision returns to Draft.

No new Revision is created.

The Author may continue editing and submit the Revision again.

`Withdraw Submission` is different from `Request Changes`.

```text
Author
→ Withdraw Submission
→ DRAFT
```

while:

```text
Learning Editor
→ Request Changes
→ DRAFT
```

Both actions return the same Revision to the Draft state, but they have different actors and business meanings.

---

# 13. Review Quiz

**Actor:** Learning Editor

Review is performed on a Revision that has been submitted for review.

```text
Review Quiz
├── View Revision
└── Provide Feedback
```

---

## 13.1 View Revision

The Learning Editor can inspect the submitted Revision, including:

* Revision information;
* Questions;
* Answer options;
* correctness configuration;
* scoring configuration;
* assessment configuration.

The Reviewer does not directly modify the Revision during review.

---

## 13.2 Provide Feedback

The Learning Editor may provide feedback concerning the submitted Revision.

Feedback may concern:

* the Revision as a whole;
* a Question;
* an Answer;
* other relevant assessment configuration.

Review feedback is separate from directly modifying the Revision.

Review history is retained so that multiple review cycles can be represented.

---

# 14. Request Changes

**Actor:** Learning Editor

## Purpose

Return an `IN_REVIEW` Revision to the Author because changes are required.

## Preconditions

* The Revision is currently under review.
* The actor is authorized as Learning Editor.
* Meaningful feedback is provided.
* The Reviewer is not acting as the submitting Author.

## Rules

The Learning Editor provides feedback but does not directly modify the Revision.

No new Revision is created.

The Author may edit the same Revision after it is returned.

## Result

```text
DRAFT
   ↓ Submit for Review
IN_REVIEW
   ↓ Request Changes
DRAFT
```

Multiple review cycles are allowed.

The Author is not required to prove that a specific piece of content changed before resubmitting.

The normal `Submit for Review` validation is performed again.

---

# 15. Approve Revision

**Actor:** Learning Editor

## Purpose

Approve the submitted Revision and publish it.

There is intentionally no separate `APPROVED` state in the current scope.

The workflow is:

```text
IN_REVIEW
    ↓ Approve Revision
PUBLISHED
```

## Preconditions

* The Revision is under review.
* The actor has Learning Editor authority.
* Separation of duties is respected.
* Final publication validation succeeds.

The Author who submitted the Revision cannot approve and publish that same Revision.

## Rules

The Learning Editor does not modify the Revision during approval.

The Revision must pass final publication validation.

If validation fails, the approval is rejected and the Revision remains under review.

## Result

The Draft Revision becomes the new Published Revision.

If a previous Published Revision exists, the previous definition becomes historical.

```text
Before:

Quiz
├── Published Revision A
└── Draft Revision B


After:

Quiz
└── Published Revision B

Revision A → Historical
```

The publication of a new Revision does not invalidate existing Attempts.

Existing Attempts remain bound to the Published Revision from which they were started.

---

# 16. Discard Draft Revision

**Actor:** Learning Author

## Purpose

Discard the current Draft Revision without affecting the Quiz identity or an existing Published Revision.

## Preconditions

* A Draft Revision exists.
* The Draft is not currently under review.

If the Draft is under review, the Author must first withdraw the submission or receive a `Request Changes` result.

## Rules

Discarding a Draft:

* does not delete the Quiz;
* does not modify the Published Revision;
* does not affect historical QuizAttempts;
* does not create another Revision.

If a Published Revision exists:

```text
Quiz
├── Published Revision
└── Draft Revision

        ↓ Discard Draft

Quiz
└── Published Revision
```

If the Quiz has never had a Published Revision, discarding the initial Draft leaves the Quiz without a usable Published Revision.

The exact lifecycle handling of a Quiz that has no remaining Revision is a separate lifecycle-model concern and does not introduce another business action here.

---

# 17. Activate Quiz

**Actor:** Authorized Learning governance actor

## Purpose

Make the Quiz available for new Attempts.

Availability is independent from:

* Revision lifecycle;
* visibility;
* permissions;
* Attempt status;
* Assessment Result.

Activation does not create or modify a Revision.

A Quiz must have a valid Published Revision before a learner can start an Attempt.

---

# 18. Deactivate Quiz

**Actor:** Authorized Learning governance actor

## Purpose

Prevent new Attempts from being started for the Quiz.

Deactivation does not modify the Published Revision.

Deactivation also does not terminate existing `IN_PROGRESS` Attempts.

Therefore:

```text
Quiz
    ↓ Deactivate
No new Attempts
```

while:

```text
Existing IN_PROGRESS Attempt
    ↓
May continue according to its Attempt rules
```

Availability therefore controls whether new Attempts may begin, rather than controlling the lifecycle of Attempts that have already started.

---

# 19. Quiz Attempt Business Rules

Although the main actions above focus on Quiz management, Quiz business behavior must respect the following Attempt rules.

## 19.1 Start Attempt

A learner may start an Attempt only from a Published Quiz Revision.

The exact learner eligibility policy is outside the Quiz Revision itself and may depend on:

* Quiz visibility;
* enrollment;
* learning access;
* assessment policy;
* other Learning rules.

---

## 19.2 Revision Binding

When an Attempt starts, it becomes permanently bound to the exact Published Revision used at that time.

Later publication does not change the Attempt's definition.

```text
Attempt
└── Published Revision used at start
```

---

## 19.3 Multiple Attempts

A learner may have multiple Attempts subject to `maxAttempts`.

At most one Attempt may be `IN_PROGRESS` for the same User and Quiz at a time.

---

## 19.4 Attempt Quota

The following Attempt statuses affect the attempt quota:

```text
SUBMITTED → consumes quota
EXPIRED   → consumes quota
CANCELLED → does not consume quota
```

An `IN_PROGRESS` Attempt is the currently active Attempt rather than a completed attempt.

---

## 19.5 Resume Attempt

A learner may resume an `IN_PROGRESS` Attempt.

The Attempt remains bound to its original Published Revision.

---

## 19.6 Time Limit

The time limit begins when the Attempt starts.

The business meaning of reaching the time limit is:

```text
IN_PROGRESS
    ↓ Time limit reached
EXPIRED
```

The technical mechanism used to detect the time limit is an implementation concern.

Browser closure or network loss does not automatically cancel an Attempt.

---

## 19.7 Expired Attempt

An `EXPIRED` Attempt is terminal.

It cannot be resumed.

Answers from an expired Attempt are not carried into a new Attempt.

Historical retention of expired answers is a persistence/data-retention concern and does not change the Attempt's terminal status.

---

## 19.8 Cancellation

An Attempt may be intentionally cancelled by:

* the learner;
* the system;
* an authorized administrator/instructor where permitted by policy.

Browser closure and network loss do not constitute cancellation.

A cancelled Attempt does not consume the maximum attempt quota.

---

# 20. Assessment Result Rules

Assessment Result is calculated when an Attempt ends through submission or expiration.

It is not assigned while the Attempt is `IN_PROGRESS`.

If `passingScore` exists:

```text
totalScore >= passingScore
    → PASSED

totalScore < passingScore
    → FAILED
```

For a timed-out Attempt:

```text
EXPIRED + passingScore
    → FAILED
```

If the Quiz has no passing score:

```text
EXPIRED
    → no PASSED/FAILED result
```

Assessment Result does not directly determine:

* Competency;
* Current Level;
* Certification Level;
* Learning Direction.

Those concepts are handled by broader Learning domain responsibilities.

---

# 21. Retry Rules

Multiple Attempts are supported according to the assessment policy defined by the Quiz Revision.

The current business model intentionally does not introduce separate fixed rules such as:

```text
allowRetryAfterPass
allowRetryAfterFail
```

Instead, retry behavior belongs to the broader Attempt Policy.

This allows different Quiz purposes to define different retry behavior without making Pass/Fail itself responsible for retry authorization.

---

# 22. Quiz Visibility

Quiz visibility is separate from Quiz availability.

The current visibility concepts are:

```text
PRIVATE
COURSE_ONLY
PUBLIC
```

### PRIVATE

The Quiz is not publicly discoverable.

Access depends on authorization.

### COURSE_ONLY

The Quiz is not independently discoverable.

It is accessible through the Course learning context only when the learner has valid learning access to that Course.

Enrollment is therefore relevant to access to a Course-only Quiz.

### PUBLIC

The Quiz may be independently discoverable.

Public visibility does not automatically mean that any user may start an Attempt.

Starting an Attempt still depends on the Published Revision, availability, and applicable eligibility/assessment policies.

---

# 23. Separation of Business Concepts

The following concepts must remain distinct.

## 23.1 Quiz and Course

```text
Quiz ≠ Course
```

A Quiz may be placed into a Course through a LessonItem, but the Course does not own the Quiz Aggregate.

---

## 23.2 Quiz and Quiz Revision

```text
Quiz ≠ QuizRevision
```

Quiz represents stable identity and governance.

QuizRevision represents a concrete assessment definition.

---

## 23.3 Revision and Attempt

```text
QuizRevision ≠ QuizAttempt
```

Revision defines what is being assessed.

Attempt represents a learner's execution of that definition.

---

## 23.4 Attempt Status and Assessment Result

```text
Attempt Status ≠ Assessment Result
```

For example:

```text
SUBMITTED + PASSED
SUBMITTED + FAILED
EXPIRED + FAILED
```

are conceptually different dimensions.

---

## 23.5 Assessment Result and Competency

```text
PASSED/FAILED ≠ Competency
```

A Quiz result does not automatically establish a learner's competency.

---

## 23.6 Assessment Result and Current Level

```text
PASSED/FAILED ≠ Current Level
```

A Quiz score must not be directly converted into a learner's current level without an explicit Learning domain rule.

---

## 23.7 Visibility and Availability

```text
Visibility ≠ Availability
```

Visibility answers whether and how the Quiz can be discovered/accessed.

Availability answers whether new Attempts may currently begin.

---

## 23.8 Visibility and Permission

```text
Visibility ≠ Permission
```

A Quiz being public does not mean every action on the Quiz is available to every actor.

Authorization remains a separate concern.

---

## 23.9 Revision Lifecycle and Availability

```text
Revision Lifecycle ≠ Quiz Availability
```

Publishing a Revision does not by itself represent activation/deactivation of the Quiz.

A Quiz can therefore have:

```text
Published Revision
+
Inactive Quiz
```

which means the assessment definition exists but new Attempts cannot start.

---

# 24. Separation of Duties

Learning governance follows a separation-of-duties principle.

The conceptual responsibilities are:

```text
Learning Author
→ create and edit own learning content
→ submit Revision for review
→ withdraw own submission

Learning Editor
→ review submitted learning content
→ provide feedback
→ request changes
→ approve and publish

Admin
→ system-level authority
```

The Author who submits a Revision cannot approve and publish that same Revision.

A Learning Editor's review authority does not automatically imply permission to edit another Author's content.

The exact implementation of roles and permissions is a broader Learning/Identity concern.

---

# 25. Revision Lifecycle

The current target workflow is:

```text
                  ┌──────────────────────┐
                  │                      │
                  │ Request Changes      │
                  │                      │
                  ▼                      │
DRAFT ──Submit──> IN_REVIEW ─────────────┘
  │                  │
  │                  │ Approve Revision
  │                  ▼
  │              PUBLISHED
  │
  └── Discard Draft
```

Author withdrawal provides:

```text
IN_REVIEW
    │
    └── Withdraw Submission
              ↓
            DRAFT
```

There is intentionally no:

```text
APPROVED
```

state in the current scope.

Approval directly results in publication.

---

# 26. Published Revision and Historical Attempts

A Published Revision may later be replaced by a newer Published Revision.

The previous definition must remain historically available because existing QuizAttempts depend on the exact assessment definition used when they started.

Example:

```text
Quiz
├── Revision A
│    ├── Attempt 1
│    └── Attempt 2
│
└── Revision B
     ├── Attempt 3
     └── Attempt 4
```

Attempts 1 and 2 continue to use Revision A even after Revision B becomes the current Published Revision.

Publishing a new Revision therefore does not invalidate existing Attempts.

---

# 27. Business Invariants

The following invariants must remain true.

## 27.1 Revision Count

A Quiz has:

```text
At most one Draft Revision
At most one Published Revision
```

A Draft and Published Revision may coexist.

---

## 27.2 Published Immutability

```text
Published Revision
→ cannot be edited directly
```

Changes require a new Draft Revision.

---

## 27.3 Score Consistency

```text
Revision.maxScore
=
sum(Question.score)
```

---

## 27.4 Passing Score

`passingScore` is optional.

If present:

```text
0 ≤ passingScore ≤ maxScore
```

---

## 27.5 Choice Answer Count

For current Choice Questions:

```text
2 ≤ number of Answers ≤ 6
```

---

## 27.6 Correct Answer

Every Choice Question must contain at least one correct Answer before publication.

Type-specific rules must also hold.

---

## 27.7 Single Choice

```text
SINGLE_CHOICE
→ maximum one correct Answer
```

---

## 27.8 True / False

```text
TRUE_FALSE
→ exactly 2 Answers
→ exactly 1 correct Answer
```

---

## 27.9 Attempt Revision Binding

```text
QuizAttempt
→ permanently bound to the Published Revision used at start
```

---

## 27.10 Active Attempt

For the same User and Quiz:

```text
At most one IN_PROGRESS Attempt
```

---

## 27.11 Assessment Result Timing

```text
IN_PROGRESS
→ no PASSED/FAILED result
```

Assessment Result is determined only when the Attempt ends.

---

# 28. Current Scope and Future Scope

## 28.1 Current Scope

The current Quiz domain includes:

* Quiz identity;
* Quiz Revision;
* Draft Revision;
* Published Revision;
* Revision-based editing;
* Quiz review workflow;
* Revision publication;
* Quiz activation/deactivation;
* Choice Questions;
* Single Choice;
* Multiple Choice;
* True/False;
* Answer configuration;
* individual Question scores;
* Revision maximum score;
* optional passing score;
* maximum attempts;
* Quiz Attempts;
* Attempt lifecycle;
* Pass/Fail Assessment Result.

---

## 28.2 Explicitly Out of Current Scope

The following are not defined as current Quiz capabilities:

* Text Response;
* AI-based text evaluation;
* Speaking Response;
* Audio Response;
* Matching Response;
* specialized `TextQuiz`;
* specialized `SpeakingQuiz`;
* specialized `ReadingQuiz`;
* generic `LearningActivity` abstraction;
* separate Assessment bounded context;
* automatic Competency inference;
* automatic Current Level inference;
* automatic Certification Level inference from Quiz results.

These concepts may be considered later if concrete business requirements require them.

---

# 29. Summary

The Quiz domain is centered around the distinction between the Quiz identity and its assessment definitions.

```text
Quiz
│
├── Draft Revision
│     └── Questions
│           └── Choice Answers
│
└── Published Revision
      └── Questions
            └── Choice Answers
```

The core lifecycle is:

```text
Create Quiz
    ↓
Draft Revision
    ↓
Submit for Review
    ↓
In Review
    ├── Request Changes ──→ Draft
    ├── Withdraw Submission ──→ Draft
    └── Approve Revision ──→ Published
```

A published definition is never edited directly.

When changes are required after publication, a new Draft Revision is created and goes through the review process again.

Quiz Attempts are permanently bound to the Published Revision from which they started.

This allows new Quiz definitions to evolve without changing the meaning of historical assessment Attempts.

The current Question Response scope is intentionally limited to Choice Response:

```text
Choice Response
├── SINGLE_CHOICE
├── MULTIPLE_CHOICE
└── TRUE_FALSE
```

Future response mechanisms are not introduced until concrete requirements justify them.

