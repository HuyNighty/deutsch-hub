# Enrollment Domain Model

## 1. Purpose

This document defines the current domain model of the Enrollment Aggregate.

It describes:

- the Aggregate Root;
- Value Objects owned by the Aggregate;
- Enrollment lifecycle state;
- Progress modeling;
- relationships with Course;
- relationships with LessonCompletion;
- domain behavior distribution;
- Aggregate boundaries;
- derived state;
- open domain decisions.

The model is based on the current implementation under:

```text
src/main/java/com/deutschhub/domain/learning/model/
src/main/java/com/deutschhub/application/learning/service/
src/main/java/com/deutschhub/infrastructure/learning/persistence/
````

The document describes the domain model supported by the current source code. It does not introduce additional domain objects or architectural abstractions that are not established by the implementation.

---

# 2. Enrollment Aggregate

The current Enrollment Aggregate is:

```text
Enrollment Aggregate
│
└── Enrollment
    │
    └── Progress
```

`Enrollment` is the Aggregate Root.

`Progress` is a Value Object owned by Enrollment.

Unlike the Course Aggregate, Enrollment does not contain a hierarchy of child Entities.

---

# 3. Aggregate Root

## 3.1 Enrollment

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

`Enrollment` is the Aggregate Root responsible for the learner's enrollment lifecycle and current Course progress.

The current model contains state including:

```text
Enrollment
├── id
├── userId
├── courseId
├── status
├── progress
├── enrolledAt
├── completedAt
├── droppedAt
└── expiredAt
```

The Aggregate exposes domain behavior for:

```text
create()
updateProgress()
drop()
expire()
```

These operations control Enrollment state transitions and Progress updates.

---

# 4. Enrollment Identity

Enrollment has its own identity:

```text
Enrollment.id
```

It also stores references identifying the associated learner and Course:

```text
userId
courseId
```

These identifiers do not turn User or Course into child entities of the Enrollment Aggregate.

The relationship is therefore:

```text
Enrollment
├── userId   ─────→ User
└── courseId ─────→ Course
```

Both are external references.

---

# 5. Progress Value Object

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

`Progress` is modeled as an immutable Value Object.

It does not have an independent identity.

The current Progress state includes:

```text
Progress
├── completedLessons
├── totalLessons
├── completionPercentage
├── totalStudyMinutes
└── lastUpdatedAt
```

Enrollment owns the Progress value:

```text
Enrollment
    │
    └── Progress
```

---

# 6. Progress Immutability

Progress updates do not mutate the existing Progress object in place.

Conceptually:

```text
Current Progress
      │
      │ updateProgress(...)
      ▼
New Progress
      │
      ▼
Enrollment.progress
```

The current implementation therefore treats Progress as a value that can be replaced when the Enrollment changes.

This is consistent with Value Object semantics.

---

# 7. Progress Invariants

`Progress` is responsible for protecting its own internal consistency.

The current implementation enforces:

```text
totalLessons > 0

completedLessons >= 0

completedLessons <= totalLessons

totalStudyMinutes >= 0
```

Therefore:

```text
0 <= completedLessons <= totalLessons
```

and:

```text
totalStudyMinutes >= 0
```

The completion percentage is derived from completed Lessons and total Lessons.

These invariants belong to the Progress Value Object rather than to the Application Service.

---

# 8. Enrollment Lifecycle State

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

The lifecycle is represented explicitly through `EnrollmentStatus`.

The current transition model is:

```text
ENROLLED
    │
    ├──→ IN_PROGRESS
    ├──→ DROPPED
    └──→ EXPIRED

IN_PROGRESS
    │
    ├──→ COMPLETED
    ├──→ DROPPED
    └──→ EXPIRED
```

`COMPLETED`, `DROPPED`, and `EXPIRED` are terminal states in the current transition model.

---

# 9. Enrollment Creation State

Enrollment creation is implemented through:

```text
Enrollment.create(...)
```

The initial state is:

```text
status = ENROLLED
```

and:

```text
completedAt = null
droppedAt = null
expiredAt = null
```

Initial Progress is created with:

```text
completedLessons = 0
totalLessons = course lesson count
totalStudyMinutes = 0
```

The Course lesson count is supplied when the Enrollment is created.

---

# 10. Progress and Enrollment Lifecycle

Enrollment is responsible for interpreting Progress in the context of its lifecycle.

The relationship is:

```text
Enrollment
    │
    ├── owns Progress
    │
    └── interprets Progress state
             │
             ├── learning started
             │       ↓
             │   IN_PROGRESS
             │
             └── learning completed
                     ↓
                 COMPLETED
```

The Progress Value Object does not directly change Enrollment status.

Instead:

```text
Enrollment.updateProgress(...)
```

updates Progress and then applies the corresponding Enrollment lifecycle transition.

This keeps lifecycle decisions inside the Aggregate Root.

---

# 11. Learning Started

The current Progress model provides a `hasStarted()` decision.

An Enrollment is considered to have started learning when Progress indicates learning activity through:

```text
completedLessons > 0
```

or:

```text
totalStudyMinutes > 0
```

When the Enrollment is currently `ENROLLED`, this can result in:

```text
ENROLLED → IN_PROGRESS
```

The transition is performed by Enrollment rather than by Progress itself.

---

# 12. Learning Completed

Progress provides a completion decision based on:

```text
completedLessons == totalLessons
```

When the resulting Progress is complete, Enrollment can transition to:

```text
IN_PROGRESS → COMPLETED
```

The current implementation also records:

```text
completedAt = now
```

This makes completion time part of the Enrollment lifecycle state.

---

# 13. External Course Reference

Enrollment stores:

```text
courseId
```

rather than an embedded Course object.

The relationship is therefore:

```text
Course Aggregate
      ▲
      │
   courseId
      │
Enrollment Aggregate
```

Course and Enrollment are separate Aggregate boundaries.

Enrollment does not own the Course structure.

It does not contain:

```text
Section
Lesson
LessonItem
```

from the Course Aggregate.

---

# 14. LessonCompletion Boundary

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

`LessonCompletion` is modeled separately from Enrollment.

Its current state includes:

```text
LessonCompletion
├── id
├── enrollmentId
├── lessonId
└── completedAt
```

It is not contained in:

```text
Enrollment
```

as a child Entity collection.

The current persistence model also provides a separate repository boundary for LessonCompletion.

Therefore:

```text
Enrollment Aggregate
└── Progress

LessonCompletion
└── separate evidence model
```

---

# 15. Progress vs. LessonCompletion

The current model establishes two different representations.

## Progress

```text
Progress
├── completedLessons
├── totalLessons
├── completionPercentage
└── totalStudyMinutes
```

Progress represents the current aggregate-level learning state.

## LessonCompletion

```text
LessonCompletion
├── enrollmentId
├── lessonId
└── completedAt
```

LessonCompletion represents evidence that a particular Lesson was completed.

Therefore:

```text
Progress ≠ LessonCompletion
```

and:

```text
Progress = current progress state
LessonCompletion = individual completion evidence
```

---

# 16. Relationship Between Progress and LessonCompletion

The current application flow connects the two concepts through the Complete Lesson use case.

Conceptually:

```text
Complete Lesson
      │
      ├── create LessonCompletion
      │
      ├── count completed Lessons
      │
      └── Enrollment.updateProgress(...)
                 │
                 ▼
              Progress
```

This relationship is implemented at the Application Layer rather than by making LessonCompletion part of the Enrollment Aggregate.

The current implementation therefore preserves separate boundaries while coordinating the two concepts during the use case.

---

# 17. Enrollment Domain Behaviors

## 17.1 `create()`

Creates a new Enrollment and establishes its initial state.

Responsibilities include:

* creating Enrollment identity;
* setting the initial status;
* creating initial Progress;
* establishing initial lifecycle timestamps.

---

## 17.2 `updateProgress()`

Updates Enrollment Progress through a new Progress value.

It also evaluates the resulting Progress against Enrollment lifecycle behavior.

The method can cause:

```text
ENROLLED → IN_PROGRESS
```

or:

```text
IN_PROGRESS → COMPLETED
```

when the corresponding conditions are satisfied.

---

## 17.3 `drop()`

Changes the Enrollment lifecycle to:

```text
DROPPED
```

and records:

```text
droppedAt
```

The transition is validated through `EnrollmentStatus`.

---

## 17.4 `expire()`

Changes the Enrollment lifecycle to:

```text
EXPIRED
```

and records:

```text
expiredAt
```

The transition is validated through `EnrollmentStatus`.

---

# 18. Active Enrollment

The current model defines:

```text
ENROLLED
IN_PROGRESS
```

as active statuses.

These states permit Progress updates.

The following states are inactive:

```text
COMPLETED
DROPPED
EXPIRED
```

An inactive Enrollment cannot continue updating Progress through the current `updateProgress()` behavior.

---

# 19. Persistence Model

The persistence representation is located under:

```text
src/main/java/com/deutschhub/infrastructure/learning/persistence/entity/
```

The current Enrollment persistence model stores Enrollment state together with Progress state.

Progress fields are persisted as part of the Enrollment persistence representation rather than through an independent Progress repository.

Conceptually:

```text
EnrollmentJpaEntity
├── Enrollment state
└── Progress state
```

The repository adapter restores the Progress Value Object and then reconstructs the Enrollment Aggregate.

This supports the domain boundary:

```text
Enrollment
└── Progress
```

---

# 20. Aggregate Boundary Summary

The current Enrollment boundary can be represented as:

```text
┌──────────────────────────────────────┐
│          Enrollment Aggregate        │
│                                      │
│  Enrollment                          │
│  ├── id                              │
│  ├── userId                          │
│  ├── courseId                        │
│  ├── status                          │
│  ├── Progress                        │
│  │   ├── completedLessons            │
│  │   ├── totalLessons                │
│  │   ├── completionPercentage        │
│  │   ├── totalStudyMinutes           │
│  │   └── lastUpdatedAt               │
│  └── lifecycle timestamps            │
│                                      │
└──────────────────────────────────────┘

        │
        ├──────── courseId ──────→ Course Aggregate
        │
        └──────── enrollmentId ──→ LessonCompletion
```

The external references do not imply ownership.

---

# 21. Domain Responsibility Distribution

The current responsibility distribution is:

```text
Enrollment
├── Aggregate lifecycle
├── Status transitions
├── Progress ownership
└── Progress update coordination

Progress
├── Progress invariants
├── Completion percentage
├── Learning activity state
└── Study-time state

LessonCompletion
└── Individual Lesson completion evidence

Course
└── Course learning structure
```

This separation avoids treating Course structure, learner progress, and completion evidence as one domain object.

---

# 22. Domain Model and Complete Lesson

The Complete Lesson use case crosses multiple domain boundaries:

```text
Course Aggregate
       │
       │ identify Lesson
       ▼
CompleteLessonService
       │
       ├── LessonCompletion
       │
       └── Enrollment
              │
              └── Progress
```

The Application Layer coordinates this use case.

The Enrollment Aggregate does not directly load Course or LessonCompletion repositories.

This preserves the current Aggregate boundaries.

---

# 23. Open Domain Decisions

## 23.1 Source of Truth for Completed Lessons

The system currently maintains:

```text
LessonCompletion records
```

and:

```text
Progress.completedLessons
```

The current application flow keeps them aligned during Complete Lesson.

However, the domain model does not explicitly establish which representation is the authoritative long-term source of completed Lesson state.

This remains an open decision.

---

## 23.2 Progress Reconciliation

The current model does not expose a dedicated domain operation for rebuilding Progress entirely from LessonCompletion records.

Whether reconciliation or reconstruction is required remains open.

---

## 23.3 Historical Total Lessons

Initial Progress receives the number of active Lessons from the Course at enrollment time.

The current implementation therefore establishes an initial snapshot.

It does not establish whether:

```text
Progress.totalLessons
```

should automatically change when the Course structure changes after enrollment.

This remains an open domain decision.

---

# 24. Confirmed Domain Model

The current Enrollment model is:

```text
Enrollment Aggregate
│
├── Aggregate Root
│   └── Enrollment
│
├── Value Object
│   └── Progress
│
├── Lifecycle
│   ├── ENROLLED
│   ├── IN_PROGRESS
│   ├── COMPLETED
│   ├── DROPPED
│   └── EXPIRED
│
├── External References
│   ├── userId
│   └── courseId
│
└── Lifecycle timestamps
    ├── enrolledAt
    ├── completedAt
    ├── droppedAt
    └── expiredAt
```

Separately:

```text
LessonCompletion
├── enrollmentId
├── lessonId
└── completedAt
```

The current implementation therefore supports the following conceptual distinction:

```text
Enrollment
    = learner's enrollment and lifecycle state

Progress
    = current learning progress owned by Enrollment

LessonCompletion
    = individual lesson completion evidence

Course
    = learning structure being enrolled in
```

No broader Aggregate redesign is justified by the current source.

