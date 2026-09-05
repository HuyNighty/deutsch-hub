# Course Business Rules

## 1. Purpose

This document defines the business rules currently enforced by the **Course Aggregate** in the DeutschHub Learning Context.

The rules are derived from the current implementation and are intended to serve as a domain baseline for future refactoring and validation.

This document distinguishes between:

- rules confirmed by the current implementation;
- implementation issues where the code does not correctly realize the apparent rule;
- open business questions that cannot be determined from the current source alone.

This document does not introduce new business requirements.

---

# 2. Aggregate Scope

The current Course Aggregate is:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
````

The Aggregate Root is:

```text
Course
```

`Section`, `Lesson`, and `LessonItem` are managed through the Course Aggregate.

The following concepts are outside the Course Aggregate:

```text
Quiz
Media
Enrollment
LessonCompletion
```

References to external concepts are represented through identifiers where applicable, rather than by embedding their aggregate state into Course.

---

# 3. Course Creation Rules

## CR-001 — Title is required

A Course must have a non-null, non-blank title.

The title is trimmed before being stored.

Source:

`src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java`

---

## CR-002 — CEFR level is required

A Course must have a non-null CEFR level.

Source:

`Course.validateLevel()`

---

## CR-003 — Price is required

A Course must have a non-null price.

Source:

`Course.validatePrice()`

---

## CR-004 — Price cannot be negative

A Course price must not be negative.

Source:

`Course.validatePrice()`

---

## CR-005 — Instructor is required

A Course must have a non-null instructor identifier.

Source:

`Course.validateInstructorId()`

---

## CR-006 — Description is optional

The current implementation allows a null description.

A null description is normalized to an empty string.

Source:

`Course` constructor.

---

## CR-007 — A newly created Course is unpublished

A newly created Course starts with:

```text
published = false
```

This means publication is an explicit state transition rather than the default creation state.

---

## CR-008 — A newly created Course has zero estimated hours

The initial value of:

```text
estimatedHours
```

is zero.

Estimated hours are recalculated as the Course structure changes.

---

# 4. Course Mutation Rules

## CR-009 — Deleted Courses cannot be mutated

A deleted Course cannot be modified through Course mutation operations.

The current implementation protects mutation operations using a deleted-state check.

---

## CR-010 — Published Courses cannot be structurally modified

Once a Course is published, its learning structure cannot be modified through the current Course behaviors.

This applies to operations including:

```text
Add Section
Update Section
Delete Section

Add Lesson
Update Lesson
Delete Lesson

Add LessonItem
```

The current implementation rejects these operations for a published Course.

This rule is one of the primary consistency rules of the Course Aggregate.

---

## CR-011 — Course metadata cannot be updated after publication

The current implementation prevents `updateMetadata()` from modifying a published Course.

Therefore, publication currently freezes both:

```text
Course metadata
Course structure
```

through the available mutation operations.

---

# 5. Course Publication Rules

## PR-001 — A deleted Course cannot be published

A deleted Course cannot transition to the published state.

---

## PR-002 — A published Course cannot be published again

A Course that is already published cannot be published again.

---

## PR-003 — A Course must contain an active Section before publication

A Course must contain at least one non-deleted Section before it can be published.

If no active Section exists, publication is rejected.

---

## PR-004 — A Course must contain an active Lesson before publication

A Course must contain at least one non-deleted Lesson within its active structure before it can be published.

If no active Lesson exists, publication is rejected.

---

## PR-005 — Publishing changes the Course publication state

After successful publication:

```text
published = true
```

and the Course modification timestamp is updated.

---

# 6. Course Unpublication Rules

## UR-001 — A deleted Course cannot be unpublished

The Course must not be deleted when the unpublish operation is executed.

---

## UR-002 — Only a published Course can be unpublished

Attempting to unpublish an unpublished Course is rejected.

---

## UR-003 — Successful unpublication changes the publication state

After successful unpublication:

```text
published = false
```

and the Course modification timestamp is updated.

---

# 7. Course Structure Rules

## 7.1. Section

### SR-001 — Section is managed through Course

Sections are added, updated, and deleted through Course Aggregate behavior.

The current Application layer does not persist Sections through an independent Section repository.

---

### SR-002 — A Section cannot be added to a published Course

Adding a Section requires the Course to be mutable.

---

### SR-003 — A null Section cannot be added

The current Course implementation rejects a null Section.

---

### SR-004 — Adding a Section updates Course state

When a Section is successfully added:

```text
Course.sections
    ↓
updated

Course.lastModifiedAt
    ↓
updated

Course.estimatedHours
    ↓
recalculated
```

---

## 7.2. Lesson

### LR-001 — Lesson is managed through Course

Lessons are added, updated, and deleted through Course Aggregate behavior.

---

### LR-002 — A Lesson can only be added to an active Section

The target Section must exist and must not be deleted.

---

### LR-003 — A Lesson cannot be added to a published Course

The Course must remain mutable.

---

### LR-004 — Updating a Lesson requires an active Section

The current Course implementation resolves the Lesson through an active Section.

---

### LR-005 — Deleting a Lesson requires an active Section

The current Course implementation resolves the Lesson through an active Section before performing the deletion.

---

### LR-006 — Lesson structural changes update Course estimated hours

After relevant Lesson changes, the Course recalculates its estimated hours.

---

# 8. LessonItem Rules

## LIR-001 — LessonItem belongs to a Lesson within the Course Aggregate

LessonItems are added and managed through the Course → Lesson hierarchy.

---

## LIR-002 — LessonItem type determines payload requirements

The current implementation supports:

```text
TEXT
MEDIA
QUIZ
```

Each type has different required payload data.

---

## LIR-003 — TEXT LessonItem requires content

A TEXT LessonItem must contain non-blank content.

---

## LIR-004 — MEDIA LessonItem requires a Media identifier

A MEDIA LessonItem must contain a Media identifier.

The Media resource itself remains outside the Course Aggregate.

---

## LIR-005 — QUIZ LessonItem requires a Quiz identifier

A QUIZ LessonItem must contain a Quiz identifier.

The Quiz Aggregate remains outside the Course Aggregate.

---

## LIR-006 — Irrelevant payload fields are cleared according to LessonItem type

The current implementation normalizes payload based on the selected type.

Conceptually:

```text
TEXT
 → content
 → no mediaId
 → no quizId

MEDIA
 → mediaId
 → no quizId
 → content cleared

QUIZ
 → quizId
 → no mediaId
 → content cleared
```

This protects the internal consistency of a LessonItem.

---

## LIR-007 — LessonItem type cannot currently be changed

The current `LessonItem` model stores its type as immutable state and does not expose a domain operation for changing it.

This is a confirmed implementation behavior.

Whether this represents an intentional business rule or a missing capability remains open.

---

# 9. Deletion Rules

## DR-001 — Course deletion is soft deletion

Course deletion is represented through deletion state rather than immediate removal from the domain object.

The current implementation records deletion using:

```text
deletedAt
```

---

## DR-002 — Deleted Course cannot continue normal mutation

Once deleted, Course mutation operations are protected by the deleted-state checks.

---

## DR-003 — Child soft-deletion is independently represented

`Section`, `Lesson`, and `LessonItem` each contain their own deletion state.

The current Course implementation does not establish a domain rule that deleting a Course automatically soft-deletes every child entity.

Therefore, no cascading soft-delete rule is defined here.

---

# 10. Authorization Rules

## AR-001 — Course mutations require an authorized actor

Course mutation methods receive:

```text
actorId
isAdmin
```

The current implementation allows mutation when:

```text
actor is an administrator
```

or:

```text
actorId == Course.instructorId
```

Unauthorized actors are rejected.

---

## AR-002 — Authorization is enforced inside Course behavior

The current Course Aggregate performs the authorization check through its domain methods rather than relying exclusively on Application Services.

This is the current implementation design.

Whether all authorization checks should remain inside the Aggregate or be represented through a dedicated authorization policy remains an architectural question for future refinement.

No change is prescribed by this document.

---

# 11. Derived State Rules

## DSR-001 — Course estimated hours are derived from Lesson estimated minutes

The current Course implementation calculates:

```text
Course.estimatedHours
    =
sum of active Lesson.estimatedMinutes
    converted to hours
```

The value is recalculated after relevant structural changes.

---

## DSR-002 — Estimated hours are maintained by the Course Aggregate

The calculation is performed by Course behavior rather than by an Application Service.

This means the Course currently maintains its own derived state.

---

# 12. Modification Timestamp Rules

## TSR-001 — Successful Course state changes update modification time

Successful Course mutations call the internal state-touching behavior.

This updates the Course modification timestamp.

This applies to relevant:

```text
metadata changes
structural changes
publication changes
deletion changes
```

---

# 13. Business Rule vs Implementation Issues

The following findings are implementation issues identified during the audit.

They should not be interpreted as new business requirements.

## ISSUE-001 — Section update null-handling is inconsistent

Source:

`src/main/java/com/deutschhub/domain/learning/model/entity/Section.java`

The `update()` method checks the existing `this.title` value rather than consistently checking the incoming `title` argument.

This may cause invalid null input to reach title validation unexpectedly.

The intended partial-update semantics should be confirmed before changing the implementation.

---

## ISSUE-002 — Lesson order index update does not assign the new value

Source:

`src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java`

The current `changeOrderIndex()` method validates the supplied value but does not assign the validated value to the `orderIndex` field.

Therefore, a valid order-index update may not actually change the stored state.

This is a concrete implementation defect.

---

# 14. Open Business Questions

The following questions cannot be answered conclusively from the current source.

## OQ-001 — Should Course soft deletion cascade to child entities?

Current behavior confirms:

```text
Course.softDelete()
    ↓
Course.deletedAt
```

but does not establish:

```text
Section.softDelete()
Lesson.softDelete()
LessonItem.softDelete()
```

as an automatic consequence.

Business intent must be confirmed before introducing such a rule.

---

## OQ-002 — Should LessonItem estimated minutes contribute to Course estimated hours?

The current calculation uses:

```text
Lesson.estimatedMinutes
```

and does not directly aggregate:

```text
LessonItem.estimatedMinutes
```

It is therefore unclear whether:

1. Lesson duration is intentionally the source of truth; or
2. LessonItem duration should eventually contribute to Course duration.

No architectural or domain change is prescribed until the intended business behavior is known.

---

## OQ-003 — Should LessonItem type be changeable?

The current implementation does not expose a type-change operation.

It is unclear whether:

```text
TEXT → MEDIA
MEDIA → QUIZ
```

should be supported as a business operation.

---

## OQ-004 — Should authorization remain an Aggregate responsibility?

The current implementation performs actor authorization within Course methods.

Whether this remains the preferred long-term representation should be evaluated together with the broader authorization model.

This is an architectural refinement question, not a current Course boundary problem.

---

# 15. Confirmed Course Rules Summary

The current Course Aggregate therefore has the following major confirmed rule groups:

```text
Course Creation
    ├── Valid title
    ├── Required CEFR level
    ├── Required instructor
    └── Non-negative price

Course Mutation
    ├── Deleted Course cannot be mutated
    └── Published Course cannot be modified

Publication
    ├── Cannot publish deleted Course
    ├── Cannot publish already-published Course
    ├── Requires active Section
    └── Requires active Lesson

Course Structure
    ├── Section managed by Course
    ├── Lesson managed through Section
    └── LessonItem managed through Lesson

LessonItem
    ├── TEXT requires content
    ├── MEDIA requires mediaId
    └── QUIZ requires quizId

Authorization
    ├── Instructor may mutate
    └── Administrator may mutate

Derived State
    └── estimatedHours maintained by Course
```

---

# 16. Domain Interpretation

The current implementation supports the following interpretation of the Course Aggregate:

> **Course is the consistency boundary for a structured learning offering and its internal learning structure.**

The Aggregate protects important relationships between:

```text
Course
Section
Lesson
LessonItem
```

while keeping external concepts such as:

```text
Quiz
Media
Enrollment
LessonCompletion
```

outside the boundary.

The strongest business invariant currently identified is:

```text
Published Course
        ↓
Structure is frozen
```

This invariant, together with Course-owned structural mutation and persistence ownership, provides strong evidence for keeping:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

inside the same Aggregate.

---

# 17. Scope of This Document

This document does not define:

* database schema;
* API contracts;
* Application Service implementation;
* final package structure;
* new learning capabilities;
* learner-level state;
* competency;
* recommendations;
* Learning Direction.

Those concerns belong to the broader Learning Context architecture and target-domain documents.

This document focuses specifically on the business rules of the existing Course Aggregate.

