# Quiz Domain Decisions — V2

> This document records the domain decisions closed during V2.
>
> V1 remains the frozen baseline. V2 does not reopen or replace decisions already closed in V1 unless an explicit contradiction is identified during a later consistency review.

---

## 1. QuizRevision Aggregate Boundary

### Decision

`QuizRevision` remains an Entity contained within the `Quiz` Aggregate.

It is **not** an Aggregate Root.

The Quiz Aggregate contains the assessment definition and its contained entities:

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── Answer
````

`QuizAttempt` remains a separate Aggregate Root.

### Rationale

The current domain decisions do not identify a business invariant that requires `QuizRevision` to have an independent consistency boundary.

`QuizRevision` remains strongly associated with the identity and governance of its owning Quiz, while its Questions and Answers form part of the same assessment definition that must be validated consistently when the Revision is published.

The existence of substantial business behavior inside `QuizRevision` does not, by itself, require a separate Aggregate boundary.

### Consequences

* `QuizRevision` belongs to a specific `Quiz`.
* `Question` and `Answer` remain contained within the Revision.
* Revision-level invariants are protected within the Quiz Aggregate.
* `QuizAttempt` is not contained in the Quiz Aggregate.
* A Quiz may contain multiple historical and current Revisions.

---

## 2. Submit for Review

### Decision

A Quiz Revision must pass through an editorial review step before it can become `PUBLISHED`.

The Revision lifecycle is:

```text
DRAFT
  │
  │ Submit for Review
  ▼
IN_REVIEW
  │
  ├── Withdraw Submission ──→ DRAFT
  │
  ├── Request Changes ──────→ DRAFT
  │
  └── Approve ───────────────→ PUBLISHED
```

A Quiz Author cannot directly publish a Revision.

### Separation of Duties

The responsibility for authoring and reviewing is separated:

```text
Quiz Author
    │
    └── Submit for Review
              │
              ▼
        Learning Editor
          ├── Request Changes
          └── Approve
```

The Author of a Revision must not act as its approving reviewer.

### Consequences

* `IN_REVIEW` belongs to the Quiz Revision lifecycle.
* `IN_REVIEW` is not part of the Quiz lifecycle.
* A Revision cannot transition directly from `DRAFT` to `PUBLISHED` through an Author action.
* A Learning Editor is responsible for review and approval.
* Requesting changes returns the Revision to `DRAFT`.

---

## 3. ReviewCycle

### Decision

A Quiz Revision maintains its review history through `ReviewCycle` Entities contained within the Revision.

```text
Quiz
└── QuizRevision
    └── ReviewCycle
```

A Revision may have multiple Review Cycles over its lifetime.

### Review Cycle Data

Each Review Cycle records the historical review interaction at Revision level:

```text
ReviewCycle
├── submittedBy
├── submittedAt
├── reviewedBy
├── reviewedAt
├── result
└── feedback
```

The review result represents the outcome of that review cycle, such as approval or requested changes.

### Historical Review

Review Cycles are historical records.

A later review cycle does not rewrite or replace previous review history.

For example:

```text
Revision
├── ReviewCycle #1 → CHANGES_REQUESTED
└── ReviewCycle #2 → APPROVED
```

Both cycles remain part of the Revision's review history.

### Feedback Scope

Review feedback is Revision-level.

No separate Question-level `ReviewFeedback` model is introduced at this stage.

A reviewer may identify a specific Question or Answer in textual feedback without creating a separate Question-level review domain model.

### Rationale

The existing Content domain already contains a `ReviewCycle` concept associated with `ArticleVersion`. The Quiz domain adopts the corresponding business concept of retaining review history, while keeping its own domain model inside the Learning Context.

The two Contexts do not share the same `ReviewCycle` Entity merely for code reuse.

---

## 4. Availability

### Decision

Quiz Revision Availability is independent from:

* Quiz lifecycle;
* Quiz Revision lifecycle;
* Attempt lifecycle.

Availability belongs to the `QuizRevision`.

```text
Quiz
└── QuizRevision
    └── Availability
```

Availability determines whether a learner may start a new Attempt.

### Availability State

Availability has an independent state:

```text
ACTIVE
INACTIVE
```

`INACTIVE` prevents new Attempts from being started.

Changing Availability does not terminate an existing `IN_PROGRESS` Attempt.

### Availability Window

A Quiz Revision may have **at most one** Availability Window.

The Window is optional.

```text
QuizRevision
└── Availability
    └── optional AvailabilityWindow
```

If no Window is configured, Availability has no time-based restriction.

### Availability Window Model

```text
AvailabilityWindow
├── startsAt: Instant?
└── endsAt: Instant?
```

Both boundaries are independently optional.

Valid configurations include:

```text
startsAt = null
endsAt   = null
```

No time boundary.

```text
startsAt = null
endsAt   = T
```

Available until `T`.

```text
startsAt = T
endsAt   = null
```

Available from `T` onward.

```text
startsAt = T1
endsAt   = T2
```

Available within the specified interval.

### Time Boundary

The Window is considered open when:

```text
startsAt ≤ serverNow < endsAt
```

Therefore:

* At exactly `startsAt`, the Revision is available.
* At exactly `endsAt`, the Revision is no longer available.

### Time Authority

The backend/server time is authoritative.

Availability boundaries use an absolute time representation (`Instant`) rather than relying on the learner's browser timezone.

### Effect on Attempts

Availability is evaluated when starting an Attempt.

Closing or disabling Availability:

* prevents new Attempts;
* does not terminate existing `IN_PROGRESS` Attempts;
* does not change an existing Attempt's bound Revision;
* does not alter historical Attempts.

---

## 5. ChangeAuthor

### Decision

`createdBy` and `author` remain separate concepts.

```text
createdBy
    = immutable historical creator

author
    = current responsible author
```

Changing the Author does not change `createdBy`.

### Authorization

The following actors may change the current Author:

* Current Author
* Admin

A Learning Editor does not gain ownership-management authority merely because they can review Quiz Revisions.

### Lifecycle Rules

`ChangeAuthor` is allowed while the Quiz is:

```text
ACTIVE
ARCHIVED
```

`ChangeAuthor` is not allowed while the Quiz is:

```text
DELETED
```

### Revision Impact

Changing the Author:

* does not create a new Quiz Revision;
* does not modify Published Revisions;
* does not modify historical Review Cycles;
* does not modify historical Quiz Attempts.

The new Author becomes the current responsible manager for the Quiz and its applicable Draft management activities.

### Role Naming

The exact system role representing the person responsible for creating/managing Courses and related learning content remains OPEN.

The current domain decision therefore uses the business concept `Quiz Author` rather than forcing a specific RoleType name.

---

## 6. Learning Prerequisites

### Decision

Learning Prerequisites are defined by the `QuizRevision`.

```text
Quiz
└── QuizRevision
    └── LearningPrerequisite
```

A prerequisite is a domain object/reference describing a condition that must be satisfied before a learner may start an Attempt for that Revision.

### Learning Target

A prerequisite references an existing learning target such as:

```text
Lesson
Section
```

The Quiz Revision does not contain or own the referenced Course, Lesson, or Section.

Conceptually:

```text
QuizRevision
└── LearningPrerequisite
      └── reference → Lesson / Section
```

The referenced learning structures remain owned by their respective learning domain boundaries.

### Evaluation Responsibility

`QuizRevision` defines the prerequisite.

It does not directly query learner state or evidence to determine whether the prerequisite is satisfied.

During Start Attempt, the application/domain orchestration evaluates the prerequisite against the appropriate learner state/evidence.

### Multiple Prerequisites

Multiple prerequisites are supported.

The default semantic is:

```text
Prerequisite A
AND
Prerequisite B
AND
Prerequisite C
```

All prerequisites must be satisfied unless an explicitly supported simple OR condition applies.

### Expression Complexity

No nested generic prerequisite expression engine is introduced.

The model does not support arbitrary expressions such as:

```text
(A AND B) OR (C AND (D OR E))
```

unless a concrete future business requirement justifies additional modeling.

### Failure Behavior

If a Learning Prerequisite is not satisfied:

* the learner cannot Start the Attempt;
* no new QuizAttempt is created;
* no attempt quota is consumed.

### Distinction

Learning Prerequisite remains distinct from:

* Access Requirement;
* Course Access;
* Current Level Recommendation;
* Completion Requirement.

A learner may have Course Access while still failing a Learning Prerequisite.

---

## 7. Assessment Result and Best Score

### Assessment Result

The previously defined Assessment Result model remains unchanged:

```text
PASSED
FAILED
NONE
```

An evaluated Attempt determines its Assessment Result when it reaches a terminal evaluated state.

Evaluated terminal states are:

```text
SUBMITTED
EXPIRED
```

`CANCELLED` does not produce an Assessment Result.

### Best Score Scope

Best Score is calculated within:

```text
User × QuizRevision
```

Attempts belonging to different Quiz Revisions do not contribute to the same Best Score.

For example:

```text
Revision A
├── Attempt 1 → 60
└── Attempt 2 → 80

Revision B
├── Attempt 1 → 90
└── Attempt 2 → 70
```

The learner has:

```text
Best Score for Revision A = 80
Best Score for Revision B = 90
```

These are independent values.

### Eligible Attempts

Best Score considers evaluated, non-cancelled Attempts:

```text
SUBMITTED ✓
EXPIRED   ✓
CANCELLED ✗
```

An expired Attempt is included because expiration produces a final evaluated result.

### Calculation

Best Score is:

```text
max(totalScore)
```

among eligible Attempts for the same User and Quiz Revision.

### Persistence

Best Score is a derived value.

It is not a separate persisted domain fact.

Conceptually:

```text
Attempts
   │
   └── max(totalScore)
          │
          ▼
      Best Score
```

A query/read model may materialize this value later for performance, but the domain source of truth remains the evaluated Attempts.

### Completion Independence

Completion Requirement satisfaction does not depend on Best Score.

A Completion Requirement is satisfied when there is at least one valid Attempt for the referenced Revision with:

```text
AssessmentResult = PASSED
```

Therefore:

```text
Best Score
    ≠
Completion Requirement
```

Best Score answers:

> What is the learner's highest score for this Revision?

Completion Requirement answers:

> Has the learner satisfied the required assessment condition?

---

## 8. Attempt Policy Edge Cases

### Attempt Quota

`maxAttempts` remains scoped to:

```text
User × Published QuizRevision
```

Quota is consumed when a new Attempt is successfully created.

It is not consumed when Start Attempt fails before creation.

### Cancellation

A successfully created Attempt consumes quota immediately.

Therefore:

```text
Start
  ↓
Attempt created
  ↓
quota consumed
  ↓
Cancel
```

A cancelled Attempt does not return its consumed quota.

### One Active Attempt

A learner may have at most one:

```text
IN_PROGRESS
```

Attempt for the same Quiz.

If an `IN_PROGRESS` Attempt exists, Start Attempt does not create another Attempt.

The existing Attempt is resumed instead.

### Revision Binding

An Attempt remains permanently bound to the exact Published Quiz Revision selected when the Attempt was created.

Publishing a newer Revision does not move an existing Attempt to the newer Revision.

For example:

```text
Revision A
    │
    └── Attempt A1 → remains bound to Revision A

Revision B
    │
    └── New Attempts → bind to Revision B
```

### Revision-Specific Quota

Attempt quota does not carry between Revisions.

If Revision A has:

```text
maxAttempts = 2
```

and the learner consumes both Attempts, publishing Revision B with its own attempt policy does not consume Revision B's quota.

Quota is evaluated independently for each Published Revision.

### Resume

Resuming an `IN_PROGRESS` Attempt:

* does not create a new Attempt;
* does not consume additional quota;
* does not create another `IN_PROGRESS` Attempt;
* does not re-run Start Attempt eligibility;
* does not change the bound Revision.

### Concurrent Start

Concurrent Start Attempt operations must not violate either of these invariants:

```text
User × Quiz
→ at most one IN_PROGRESS Attempt

User × QuizRevision
→ successful Attempt creations ≤ maxAttempts
```

Attempt creation and quota consumption must therefore be protected atomically so concurrent requests cannot exceed the configured maximum.

### Availability During an Attempt

If Availability becomes inactive or its Window closes after an Attempt has already started:

```text
IN_PROGRESS
```

remains valid.

The Attempt is not automatically terminated.

### Expiration

When an Attempt reaches its configured expiration:

```text
IN_PROGRESS
    ↓
EXPIRED
```

The Attempt is evaluated using the answers available before expiration.

Unanswered Questions receive zero earned score.

`EXPIRED` is therefore an evaluated terminal Attempt and is not inherently equivalent to `FAILED`.

### Cancellation

Cancellation transitions:

```text
IN_PROGRESS
    ↓
CANCELLED
```

A cancelled Attempt:

* consumes its already allocated quota;
* cannot be resumed;
* cannot be submitted;
* does not receive QuestionResults;
* does not receive an Assessment Result.

### Manual Submit

For `REQUIRED_ALL` Completion Policy:

```text
unanswered Questions > 0
→ Manual Submit is rejected
```

For `OPTIONAL` Completion Policy:

```text
unanswered Questions ≥ 0
→ Manual Submit is allowed
```

Expiration is not blocked by Completion Policy and always evaluates the Attempt.

---

## 9. V2 Closure

The following V2 decisions are now closed:

```text
✓ QuizRevision Aggregate Boundary
✓ Submit for Review
✓ ReviewCycle
✓ Availability
✓ Availability Window
✓ ChangeAuthor / Authorization
✓ Learning Prerequisites
✓ Assessment Result / Best Score
✓ Attempt Policy Edge Cases
```

V1 remains frozen.

No V2 decision introduces a new generic framework, nested rule engine, or cross-context shared domain model without a concrete business requirement.

Any remaining OPEN items from V1 that are outside the V2 scope remain OPEN and are not implicitly resolved by this document.
