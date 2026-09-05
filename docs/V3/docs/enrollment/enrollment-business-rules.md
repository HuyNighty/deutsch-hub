# Enrollment Business Rules

## 1. Purpose

This document defines the business rules currently implemented for the Enrollment domain.

It focuses on:

- enrollment creation;
- enrollment lifecycle;
- progress invariants;
- lesson completion;
- progress and status transitions;
- enrollment termination;
- the relationship between Enrollment, Progress, and LessonCompletion.

The rules are based on the current implementation under:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
src/main/java/com/deutschhub/domain/learning/model/enums/EnrollmentStatus.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
src/main/java/com/deutschhub/application/learning/service/
````

---

# 2. Enrollment Creation Rules

## 2.1 Enrollment Requires a Published Course

A user can enroll in a Course only when the Course is published.

The check is performed by the enrollment application flow before creating the Enrollment.

```text
User
  ↓
Enroll Course
  ↓
Course must be published
  ↓
Create Enrollment
```

**Status:** Confirmed.

---

## 2.2 Duplicate Enrollment Is Not Allowed

A user cannot create another active enrollment for the same Course when an existing enrollment already exists.

The enrollment flow checks for an existing enrollment before creating a new one.

**Status:** Confirmed.

---

## 2.3 Enrollment Initial State

A newly created Enrollment starts with:

```text
status = ENROLLED
```

The initial lifecycle timestamps are unset:

```text
completedAt = null
droppedAt = null
expiredAt = null
```

**Status:** Confirmed.

---

# 3. Initial Progress Rules

When an Enrollment is created, its initial Progress is created using the number of active Lessons currently available in the Course.

The initial Progress starts with:

```text
completedLessons = 0
totalLessons = course lesson count
totalStudyMinutes = 0
completionPercentage = 0
```

The Course lesson count is determined at enrollment time rather than dynamically on every Progress calculation.

**Status:** Confirmed.

---

# 4. Progress Invariants

The `Progress` Value Object is responsible for maintaining its own consistency.

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

## 4.1 Total Lessons Must Be Positive

```text
totalLessons > 0
```

An invalid total lesson count is rejected.

**Status:** Confirmed.

---

## 4.2 Completed Lessons Cannot Be Negative

```text
completedLessons >= 0
```

**Status:** Confirmed.

---

## 4.3 Completed Lessons Cannot Exceed Total Lessons

```text
completedLessons <= totalLessons
```

Therefore:

```text
0 <= completedLessons <= totalLessons
```

**Status:** Confirmed.

---

## 4.4 Study Minutes Cannot Be Negative

```text
totalStudyMinutes >= 0
```

**Status:** Confirmed.

---

## 4.5 Completion Percentage Is Derived

The completion percentage is calculated from:

```text
completedLessons / totalLessons × 100
```

The value is rounded to two decimal places by the current implementation.

It is therefore derived from Progress state rather than independently supplied as an authoritative value.

**Status:** Confirmed.

---

# 5. Progress Update Rules

## 5.1 Only Active Enrollment Can Update Progress

`Enrollment.updateProgress(...)` requires the Enrollment to be active.

The current active statuses are:

```text
ENROLLED
IN_PROGRESS
```

The following statuses are inactive:

```text
COMPLETED
DROPPED
EXPIRED
```

Therefore, an Enrollment cannot continue updating Progress after reaching an inactive state.

**Status:** Confirmed.

---

## 5.2 Progress Is Updated as a New Value

`Progress` is immutable.

Updating Progress creates a new `Progress` value and replaces the previous value held by Enrollment.

Conceptually:

```text
Current Progress
       ↓
updateProgress(...)
       ↓
New Progress
       ↓
Enrollment.progress = New Progress
```

**Status:** Confirmed.

---

# 6. Enrollment Status Rules

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/enums/EnrollmentStatus.java
```

The current Enrollment statuses are:

```text
ENROLLED
IN_PROGRESS
COMPLETED
DROPPED
EXPIRED
```

---

## 6.1 Start Learning

An Enrollment initially has:

```text
ENROLLED
```

When Progress indicates that learning has started, the Enrollment transitions to:

```text
ENROLLED → IN_PROGRESS
```

The current implementation considers learning started when either completed lessons or study minutes indicate activity.

**Status:** Confirmed.

---

## 6.2 Complete Learning

When Progress reaches completion:

```text
completedLessons == totalLessons
```

the Enrollment can transition to:

```text
IN_PROGRESS → COMPLETED
```

The current implementation also records:

```text
completedAt = now
```

**Status:** Confirmed.

---

## 6.3 Terminal States

The following statuses are terminal in the current transition model:

```text
COMPLETED
DROPPED
EXPIRED
```

They do not transition to another Enrollment status through the current `EnrollmentStatus.canTransitionTo(...)` rules.

**Status:** Confirmed.

---

# 7. Enrollment Status Transition Matrix

The current transition model is:

| Current Status | Allowed Next Statuses               |
| -------------- | ----------------------------------- |
| `ENROLLED`     | `IN_PROGRESS`, `DROPPED`, `EXPIRED` |
| `IN_PROGRESS`  | `COMPLETED`, `DROPPED`, `EXPIRED`   |
| `COMPLETED`    | None                                |
| `DROPPED`      | None                                |
| `EXPIRED`      | None                                |

Conceptually:

```text
                 ┌───────────────┐
                 │   ENROLLED    │
                 └───────┬───────┘
                         │
                         ▼
                 ┌───────────────┐
                 │ IN_PROGRESS   │
                 └───┬───────┬───┘
                     │       │
              complete│       │drop / expire
                     ▼       ▼
              COMPLETED   DROPPED / EXPIRED
```

`ENROLLED` can also transition directly to `DROPPED` or `EXPIRED`.

**Status:** Confirmed.

---

# 8. Lesson Completion Rules

The lesson completion use case is implemented in:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

---

## 8.1 Enrollment Must Be Active

A lesson cannot be completed for an inactive Enrollment.

The use case requires the Enrollment to be in an active state.

**Status:** Confirmed.

---

## 8.2 Lesson Must Belong to the Course

The Lesson being completed must belong to the Course associated with the Enrollment.

The current implementation searches the Course structure for an active Lesson.

The Lesson must therefore belong to:

```text
Course
└── active Section
    └── active Lesson
```

**Status:** Confirmed.

---

## 8.3 Deleted Lessons Cannot Be Completed

The completion flow only accepts active Lessons.

A deleted Lesson cannot be newly completed through the current use case.

**Status:** Confirmed.

---

## 8.4 A Lesson Cannot Be Completed Twice

Before creating a `LessonCompletion`, the application checks whether a completion already exists for:

```text
enrollmentId + lessonId
```

If a completion already exists, the operation is rejected.

The current error is:

```text
LESSON_ALREADY_COMPLETED
```

**Status:** Confirmed.

---

# 9. Study Time Rules During Lesson Completion

When completing a Lesson, the supplied study time is constrained by the Lesson's estimated duration.

The current calculation is conceptually:

```text
studyMinutes =
    min(
        max(0, requestedStudyMinutes),
        lesson.estimatedMinutes
    )
```

Therefore:

```text
requested < 0
    → 0

requested > lesson.estimatedMinutes
    → lesson.estimatedMinutes
```

The resulting study time is then added to the Enrollment's existing total study minutes.

**Status:** Confirmed implementation rule.**

---

# 10. Lesson Completion and Progress Update

The current lesson completion flow connects evidence and progress.

Conceptually:

```text
Complete Lesson
      │
      ├── Create LessonCompletion
      │
      ├── Count completed lessons
      │
      ├── Calculate study time
      │
      └── Enrollment.updateProgress(...)
```

The resulting Progress is then responsible for determining whether the Enrollment has started or completed.

This establishes a distinction between:

```text
LessonCompletion
    = evidence that a Lesson was completed

Progress
    = current aggregate progress state
```

**Status:** Confirmed from the current implementation.

---

# 11. Drop Enrollment Rules

The drop behavior is implemented through:

```text
Enrollment.drop()
```

The operation is permitted only when the corresponding status transition is valid according to:

```text
EnrollmentStatus.canTransitionTo(...)
```

When successful:

```text
status = DROPPED
droppedAt = now
```

The Enrollment becomes inactive and cannot continue updating Progress.

**Status:** Confirmed.

---

# 12. Expire Enrollment Rules

The expire behavior is implemented through:

```text
Enrollment.expire()
```

The operation is permitted only when the corresponding status transition is valid.

When successful:

```text
status = EXPIRED
expiredAt = now
```

The Enrollment becomes inactive and cannot continue updating Progress.

**Status:** Confirmed.

---

# 13. LessonCompletion Is Separate from Enrollment

`LessonCompletion` is implemented as a separate Entity:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

Its state includes:

```text
id
enrollmentId
lessonId
completedAt
```

It is not stored as a child collection inside the Enrollment Aggregate.

The persistence model also uses a separate repository boundary:

```text
LessonCompletionRepositoryPort
```

Therefore:

```text
Enrollment
└── Progress

LessonCompletion
├── enrollmentId
└── lessonId
```

The current model does not treat LessonCompletion as an internal Enrollment Entity.

**Status:** Confirmed.

---

# 14. Progress Is Not Lesson Completion

The current model distinguishes:

```text
Progress
├── completedLessons
├── totalLessons
├── completionPercentage
└── totalStudyMinutes
```

from:

```text
LessonCompletion
├── enrollmentId
├── lessonId
└── completedAt
```

Progress represents the aggregate-level progress state.

LessonCompletion represents individual lesson completion evidence.

Therefore:

```text
Progress ≠ LessonCompletion
```

**Status:** Confirmed by the current domain and application model.

---

# 15. Course Relationship

Enrollment stores:

```text
courseId
```

rather than embedding a Course object.

Therefore, Course and Enrollment are separate Aggregate boundaries:

```text
Course Aggregate
       ▲
       │ courseId
       │
Enrollment Aggregate
```

The Enrollment Aggregate does not own the Course.

**Status:** Confirmed.

---

# 16. Progress and LessonCompletion Consistency

The current implementation maintains both:

```text
LessonCompletion records
```

and:

```text
Enrollment.Progress
```

During `CompleteLessonService`, the application creates LessonCompletion evidence and then updates Enrollment Progress.

The current service is transactional, so these operations participate in the same application transaction.

However, the source does not explicitly define a formal domain-level statement identifying which representation is the authoritative source of completed lessons.

Therefore:

```text
LessonCompletion
        ↓
Progress
```

is confirmed as the current application flow, but the long-term consistency/source-of-truth model remains an open decision.

**Status:** Open domain/consistency decision.

---

# 17. Rule Summary

| Area              | Rule                                                     | Status    |
| ----------------- | -------------------------------------------------------- | --------- |
| Enrollment        | Course must be published                                 | Confirmed |
| Enrollment        | Duplicate enrollment is rejected                         | Confirmed |
| Enrollment        | Initial status is `ENROLLED`                             | Confirmed |
| Progress          | `totalLessons > 0`                                       | Confirmed |
| Progress          | `completedLessons >= 0`                                  | Confirmed |
| Progress          | `completedLessons <= totalLessons`                       | Confirmed |
| Progress          | `totalStudyMinutes >= 0`                                 | Confirmed |
| Progress          | Completion percentage is derived                         | Confirmed |
| Progress          | Only active Enrollment can update Progress               | Confirmed |
| Lifecycle         | Learning starts → `IN_PROGRESS`                          | Confirmed |
| Lifecycle         | Completed Progress → `COMPLETED`                         | Confirmed |
| Lifecycle         | `COMPLETED`, `DROPPED`, `EXPIRED` are terminal           | Confirmed |
| Lesson Completion | Enrollment must be active                                | Confirmed |
| Lesson Completion | Lesson must belong to Course                             | Confirmed |
| Lesson Completion | Deleted Lesson cannot be completed                       | Confirmed |
| Lesson Completion | Lesson cannot be completed twice                         | Confirmed |
| Study Time        | Study time is bounded by Lesson duration                 | Confirmed |
| Drop              | Records `droppedAt`                                      | Confirmed |
| Expire            | Records `expiredAt`                                      | Confirmed |
| Boundary          | LessonCompletion is separate from Enrollment             | Confirmed |
| Semantics         | Progress ≠ LessonCompletion                              | Confirmed |
| Consistency       | Authoritative relationship between evidence and Progress | Open      |

---

# 18. Open Decisions

The following questions are not resolved by the current implementation and should remain open rather than being inferred.

## 18.1 Source of Truth for Lesson Completion

When both `LessonCompletion` and `Progress.completedLessons` exist, which representation is the authoritative source for learner completion state?

The current implementation synchronizes them during the Complete Lesson use case, but does not explicitly establish a domain-level source-of-truth rule.

---

## 18.2 Historical Course Lesson Count

Enrollment stores the Course lesson count inside its initial Progress.

It remains an open domain question whether `Progress.totalLessons` represents:

```text
the Course structure at enrollment time
```

or should evolve when the Course structure changes later.

The current implementation establishes the initial snapshot behavior but does not by itself establish the intended long-term business semantics.

---

## 18.3 Progress and Evidence Reconciliation

The current implementation updates Progress when a LessonCompletion is created.

The domain does not currently expose a separate reconciliation behavior for reconstructing Progress entirely from LessonCompletion records.

Whether such reconciliation is required is an open decision.

---

# 19. Final Domain Rule Summary

The current Enrollment model establishes the following core concepts:

```text
Enrollment
    │
    ├── owns Progress
    │
    ├── references Course
    │
    └── controls learner enrollment lifecycle

LessonCompletion
    │
    └── records individual lesson completion evidence
```

The central progression flow is:

```text
ENROLLED
    │
    │ learner starts activity
    ▼
IN_PROGRESS
    │
    │ all lessons completed
    ▼
COMPLETED
```

with alternative termination paths:

```text
ENROLLED / IN_PROGRESS
        │
        ├── drop   → DROPPED
        │
        └── expire → EXPIRED
```

Progress remains a Value Object owned by Enrollment, while LessonCompletion remains separate evidence.

The current implementation provides sufficient evidence for these boundaries and rules without requiring a broader redesign of the Enrollment domain.
