# Enrollment Business Actions

## 1. Purpose

This document describes the business actions currently implemented for the Enrollment domain.

It focuses on state-changing use cases that operate on the Enrollment Aggregate and on the application-level orchestration required to coordinate Enrollment with Course and LessonCompletion.

The document does not redefine business rules. The detailed business rules are documented separately in `enrollment-business-rules.md`.

The analysis is based on the current implementation under:

```text
src/main/java/com/deutschhub/application/learning/service/
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
src/main/java/com/deutschhub/domain/learning/model/enums/EnrollmentStatus.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
````

---

# 2. Action Model

Enrollment command actions generally follow this pattern:

```text
Client
  ↓
Application Service
  ↓
Load required Aggregate(s)
  ↓
Validate application-level conditions
  ↓
Invoke Domain Behavior
  ↓
Persist state changes
  ↓
Build application response
```

The Application Layer coordinates the use case.

The Enrollment Aggregate remains responsible for its own lifecycle and Progress state.

LessonCompletion is handled as a separate evidence model rather than as a child entity of Enrollment.

---

# 3. Command Actions

The current Enrollment command actions are:

```text
Enrollment
├── Enroll Course
├── Complete Lesson
├── Drop Course
└── Expire Enrollment
```

These actions have different levels of complexity.

`Enroll Course`, `Drop Course`, and `Expire Enrollment` primarily operate on the Enrollment Aggregate.

`Complete Lesson` is a cross-aggregate application action involving Course, LessonCompletion, and Enrollment.

---

# 4. Enroll Course

## 4.1 Application Service

```text
EnrollCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/EnrollCourseService.java
```

## 4.2 Flow

```text
EnrollCourseService
    ↓
Find Course
    ↓
Verify Course is published
    ↓
Check existing Enrollment
    ↓
Count active Lessons
    ↓
Enrollment.create(...)
    ↓
EnrollmentRepositoryPort.save(...)
    ↓
Response
```

## 4.3 Application Responsibilities

The Application Service:

* identifies the requested Course;
* verifies that the Course can be enrolled in;
* checks for an existing Enrollment;
* determines the current active Lesson count;
* creates the Enrollment;
* persists the Enrollment;
* maps the result to the application response.

## 4.4 Domain Behavior

Enrollment creation is delegated to:

```text
Enrollment.create(...)
```

The Aggregate establishes:

```text
status = ENROLLED
```

and creates initial Progress using the Course Lesson count.

## 4.5 Aggregate Interaction

```text
Course Aggregate
      │
      │ read
      ▼
EnrollCourseService
      │
      │ create
      ▼
Enrollment Aggregate
```

Course and Enrollment remain separate Aggregates.

## 4.6 State Change

A new Enrollment is created with:

```text
status = ENROLLED
progress = initial Progress
```

## 4.7 Status

**Confirmed.**

The current separation between application orchestration and Enrollment creation is consistent with the existing domain model.

---

# 5. Complete Lesson

## 5.1 Application Service

```text
CompleteLessonService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

`Complete Lesson` is the most complex Enrollment-related command in the current implementation.

## 5.2 Flow

```text
CompleteLessonService
        ↓
Find Enrollment
        ↓
Verify Enrollment is active
        ↓
Find Course
        ↓
Verify Course is not deleted
        ↓
Find active Lesson in Course
        ↓
Verify Lesson has not already been completed
        ↓
Create LessonCompletion
        ↓
Save LessonCompletion
        ↓
Count completed Lessons
        ↓
Calculate study minutes
        ↓
Enrollment.updateProgress(...)
        ↓
Save Enrollment
        ↓
Build response
```

## 5.3 Application Responsibilities

The Application Service coordinates several domain concepts:

* Enrollment lookup;
* Course lookup;
* Lesson lookup;
* duplicate completion detection;
* LessonCompletion creation;
* completed Lesson counting;
* study-time calculation;
* Enrollment Progress update;
* persistence of the resulting state.

The service is therefore an application-level orchestration point rather than a single-Aggregate command.

---

## 5.4 Course Interaction

The Course Aggregate is used to locate the Lesson associated with the Enrollment's Course.

Conceptually:

```text
Enrollment.courseId
        ↓
Course
        ↓
active Section
        ↓
active Lesson
```

The Course itself is not embedded inside Enrollment.

The Application Service coordinates the relationship using identifiers.

---

## 5.5 LessonCompletion Interaction

After validating the Lesson, the application creates:

```text
LessonCompletion.create(
    enrollmentId,
    lessonId
)
```

and persists it through:

```text
LessonCompletionRepositoryPort
```

LessonCompletion is therefore created as separate evidence.

It is not added to the Enrollment Aggregate.

```text
Enrollment Aggregate
        │
        │ enrollmentId
        ▼
LessonCompletion
```

---

## 5.6 Progress Interaction

After creating LessonCompletion, the application determines the current number of completed Lessons and invokes:

```text
Enrollment.updateProgress(...)
```

The Enrollment Aggregate then creates a new Progress value and determines whether the Enrollment should remain:

```text
ENROLLED
```

or transition to:

```text
IN_PROGRESS
```

or:

```text
COMPLETED
```

The Application Service does not directly modify Enrollment status.

---

## 5.7 Study Time

The application calculates the study time associated with the completed Lesson.

The current behavior bounds the supplied value by:

```text
0
```

and:

```text
Lesson.estimatedMinutes
```

Conceptually:

```text
studyMinutes =
    min(
        max(0, requestedStudyMinutes),
        lesson.estimatedMinutes
    )
```

The resulting value is then incorporated into Enrollment Progress.

---

## 5.8 State Changes

A successful Complete Lesson action can result in changes to multiple models:

```text
LessonCompletion
    ↓
new completion evidence

Enrollment
    ↓
Progress updated
    ↓
possibly status changed
```

The resulting state can therefore be summarized as:

```text
Complete Lesson
      │
      ├── LessonCompletion created
      │
      └── Enrollment Progress updated
             │
             ├── ENROLLED → IN_PROGRESS
             │
             └── IN_PROGRESS → COMPLETED
```

## 5.9 Transaction Boundary

The current `CompleteLessonService` is transactional.

The LessonCompletion persistence and Enrollment persistence therefore participate in the same application transaction.

This provides the current implementation with a transactional consistency boundary for the use case.

## 5.10 Status

**Confirmed.**

The use case is intentionally application-oriented because it coordinates multiple Aggregate boundaries and evidence.

No broad refactoring is justified by the current implementation.

---

# 6. Drop Course

## 6.1 Application Service

```text
DropCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/DropCourseService.java
```

## 6.2 Flow

```text
DropCourseService
    ↓
Find Enrollment
    ↓
Enrollment.drop()
    ↓
EnrollmentRepositoryPort.save(...)
```

## 6.3 Application Responsibilities

The Application Service:

* identifies the Enrollment;
* invokes the domain behavior;
* persists the updated Enrollment.

It does not directly modify the Enrollment status.

## 6.4 Domain Behavior

The Aggregate performs:

```text
Enrollment.drop()
```

and validates the status transition through:

```text
EnrollmentStatus.canTransitionTo(DROPPED)
```

On successful transition:

```text
status = DROPPED
droppedAt = now
```

## 6.5 State Change

```text
ENROLLED / IN_PROGRESS
        ↓
     DROPPED
```

The resulting Enrollment becomes inactive.

## 6.6 Status

**Confirmed.**

This is a simple Aggregate command with a clean Application → Domain → Repository flow.

---

# 7. Expire Enrollment

## 7.1 Application Service

```text
ExpireEnrollmentService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/ExpireEnrollmentService.java
```

## 7.2 Flow

```text
ExpireEnrollmentService
    ↓
Find Enrollment
    ↓
Enrollment.expire()
    ↓
EnrollmentRepositoryPort.save(...)
    ↓
Query LessonCompletion data
    ↓
Build response
```

## 7.3 Domain Behavior

The Aggregate performs:

```text
Enrollment.expire()
```

and validates:

```text
EnrollmentStatus.canTransitionTo(EXPIRED)
```

On successful transition:

```text
status = EXPIRED
expiredAt = now
```

## 7.4 LessonCompletion Interaction

The service may subsequently query LessonCompletion data to construct the application response.

This is read orchestration and does not make LessonCompletion a child of Enrollment.

The boundary remains:

```text
Enrollment Aggregate
        +
LessonCompletion evidence
        ↓
Application response
```

## 7.5 State Change

```text
ENROLLED / IN_PROGRESS
        ↓
      EXPIRED
```

The resulting Enrollment becomes inactive.

## 7.6 Status

**Confirmed.**

---

# 8. Query Actions

The Enrollment application layer also contains read-oriented services.

Examples include:

```text
GetEnrollmentDetailService
GetMyCourseProgressService
GetCourseEnrollmentsService
GetCompletedLessonsService
GetMyCoursesService
```

These are Query responsibilities rather than Aggregate business actions.

The distinction is:

```text
Command
    ↓
State change
    ↓
Domain behavior
```

versus:

```text
Query
    ↓
Read state
    ↓
Build response
```

Query services are therefore not included in the command action list above.

---

# 9. Action-to-Domain Mapping

| Business Action   | Application Service       | Domain Behavior               | Main Domain Objects                               | Status    |
| ----------------- | ------------------------- | ----------------------------- | ------------------------------------------------- | --------- |
| Enroll Course     | `EnrollCourseService`     | `Enrollment.create()`         | Course + Enrollment                               | Confirmed |
| Complete Lesson   | `CompleteLessonService`   | `Enrollment.updateProgress()` | Course + LessonCompletion + Enrollment + Progress | Confirmed |
| Drop Course       | `DropCourseService`       | `Enrollment.drop()`           | Enrollment                                        | Confirmed |
| Expire Enrollment | `ExpireEnrollmentService` | `Enrollment.expire()`         | Enrollment + LessonCompletion read                | Confirmed |

---

# 10. Responsibility Boundary

The current Enrollment actions establish the following responsibility split.

## Application Layer

The Application Layer is responsible for:

* coordinating use cases;
* loading required Aggregates;
* performing cross-Aggregate lookups;
* validating use-case-specific conditions;
* creating independent evidence such as LessonCompletion;
* invoking Aggregate behavior;
* persisting changed state;
* constructing application responses.

## Enrollment Aggregate

The Enrollment Aggregate is responsible for:

* Enrollment lifecycle;
* status transitions;
* Progress ownership;
* Progress update;
* determining lifecycle changes based on Progress;
* preventing invalid state transitions.

## Progress Value Object

`Progress` is responsible for:

* Progress invariants;
* completed Lesson count;
* total Lesson count;
* study minutes;
* derived completion percentage.

## LessonCompletion

`LessonCompletion` represents individual Lesson completion evidence.

It is persisted independently from the Enrollment Aggregate.

---

# 11. Cross-Aggregate Action

`Complete Lesson` is the clearest example of an application-level cross-Aggregate action.

```text
                 Complete Lesson
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
       Course     LessonCompletion  Enrollment
          │             │             │
          │             │             ▼
          │             │          Progress
          │             │             │
          └─────────────┴─────────────┘
```

The Application Layer coordinates these boundaries.

The Course Aggregate does not own Enrollment.

Enrollment does not own LessonCompletion.

LessonCompletion does not become part of Progress.

This preserves the current Aggregate boundaries.

---

# 12. Implementation Findings

## 12.1 Complete Lesson Has Significant Orchestration

`CompleteLessonService` coordinates:

```text
Enrollment
Course
Lesson
LessonCompletion
Progress
```

This makes the service more complex than the other Enrollment command services.

However, the complexity is a consequence of the use case crossing multiple domain boundaries.

There is currently no concrete evidence that the service should be split or that the domain boundaries should be changed.

**Classification:** Observation.

**Action:** No refactor required at this stage.

---

## 12.2 LessonCompletion and Progress Consistency

The current Complete Lesson flow creates LessonCompletion and then updates Enrollment Progress within the same transactional use case.

The current implementation therefore establishes an operational relationship:

```text
LessonCompletion
       ↓
completed Lesson count
       ↓
Enrollment.updateProgress()
```

However, the domain model does not explicitly define whether LessonCompletion or Progress is the long-term authoritative source of completed Lesson state.

**Classification:** Open domain/consistency decision.

---

# 13. Open Decisions

## 13.1 Source of Truth

The system currently maintains:

```text
LessonCompletion records
```

and:

```text
Progress.completedLessons
```

The long-term source-of-truth relationship between these representations remains open.

---

## 13.2 Progress Reconciliation

The current implementation updates Progress during the Complete Lesson use case.

It does not establish a separate domain operation for rebuilding Progress from LessonCompletion records.

Whether such reconciliation is required remains open.

---

## 13.3 Historical Total Lesson Count

Initial Progress stores the Course Lesson count at enrollment time.

The long-term semantics of `Progress.totalLessons` when the Course structure changes after enrollment remain open.

The current action model does not introduce a rule for automatically synchronizing this value with later Course changes.

---

# 14. Final Action Model

The current Enrollment command model is:

```text
                    ┌─────────────────┐
                    │  Enroll Course  │
                    └────────┬────────┘
                             ↓
                        ENROLLED
                             │
                ┌────────────┼────────────┐
                │            │            │
                │            │            │
             Complete      Drop        Expire
                │            │            │
                ▼            ▼            ▼
          IN_PROGRESS     DROPPED      EXPIRED
                │
                │ complete all Lessons
                ▼
           COMPLETED
```

The `Complete Lesson` action additionally produces:

```text
LessonCompletion
       +
Enrollment Progress update
```

The current Application Services generally follow the intended DDD/Hexagonal responsibility boundary:

```text
Application
    ↓
orchestrate
    ↓
Domain behavior
    ↓
Persistence Port
```

The current implementation provides sufficient evidence for these business actions without requiring a broader architectural redesign.

