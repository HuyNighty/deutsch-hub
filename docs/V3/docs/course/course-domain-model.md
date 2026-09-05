# Course Domain Model

## 1. Purpose

This document defines the current domain model of the Course Aggregate.

It describes:

- the Aggregate Root;
- entities owned by the Aggregate;
- Value Objects used by the Course domain;
- entity relationships and ownership;
- domain behavior distribution;
- lifecycle and derived state;
- external references;
- implementation findings and open domain decisions.

The model is based on the current implementation under:

```text
src/main/java/com/deutschhub/domain/learning/model/
src/main/java/com/deutschhub/infrastructure/learning/persistence/entity/
````

The document describes the model supported by the current source code. It does not introduce concepts that are not currently established by the implementation or previously accepted domain decisions.

---

# 2. Course Aggregate

The current Course Aggregate is structured as:

```text
Course Aggregate
│
└── Course
    │
    └── Section
        │
        └── Lesson
            │
            └── LessonItem
```

The Aggregate Root is `Course`.

`Section`, `Lesson`, and `LessonItem` are entities contained within the Course Aggregate.

The Aggregate is persisted and restored as a single Course graph through the Course repository boundary.

---

# 3. Aggregate Root

## 3.1 Course

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

`Course` is the Aggregate Root of the Course Aggregate.

The Aggregate Root controls both Course lifecycle behavior and structural mutations.

Current Course-level behaviors include:

```text
create()
updateMetadata()
softDelete()
publish()
unpublish()

addSection()
updateSection()
deleteSection()

addLessonToSection()
updateLesson()
deleteLesson()

addLessonItemToLesson()
```

These operations ensure that mutations to Sections, Lessons, and LessonItems occur through the Course Aggregate boundary.

The current implementation does not expose independent mutation repositories for these child entities.

---

# 4. Course State

The `Course` Aggregate currently contains state representing:

```text
Course
├── Identity
├── Metadata
├── Instructor
├── CEFR level
├── Price
├── Publication state
├── Deletion state
├── Estimated duration
└── Sections
```

The exact implementation is located in:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

Important lifecycle fields include:

```text
published
deletedAt
```

The current model therefore represents lifecycle state through these fields rather than through a dedicated `CourseStatus` enum or state object.

---

# 5. Internal Entities

## 5.1 Section

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
```

`Section` is an Entity inside the Course Aggregate.

A Section has its own identity and mutable state, including:

```text
id
title
description
orderIndex
lessons
createdAt
updatedAt
deletedAt
```

A Section does not have an independent Aggregate boundary in the current model.

Its structural lifecycle is controlled through the Course Aggregate.

---

## 5.2 Lesson

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
```

`Lesson` is an Entity owned by a Section.

A Lesson contains state including:

```text
id
title
description
estimatedMinutes
CEFR level
orderIndex
isFreePreview
items
createdAt
updatedAt
deletedAt
```

Lesson behavior includes updating its own state, managing LessonItems, and handling its own soft-deletion state.

However, access to the Lesson as part of Course structure remains controlled by the Course Aggregate.

---

## 5.3 LessonItem

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

`LessonItem` is an Entity owned by a Lesson.

Its state includes:

```text
id
type
title
description
content
mediaId
quizId
estimatedMinutes
orderIndex
createdAt
updatedAt
deletedAt
```

`LessonItem` also contains type-specific payload validation.

The current supported types establish different payload requirements:

```text
TEXT
 └── content required

MEDIA
 └── mediaId required

QUIZ
 └── quizId required
```

Fields that are not relevant to the selected type are cleared by the current domain implementation.

---

# 6. Entity Ownership

The current ownership relationship is:

```text
Course
  │
  └── owns
       │
       ▼
     Section
       │
       └── owns
            │
            ▼
          Lesson
            │
            └── owns
                 │
                 ▼
             LessonItem
```

This ownership is expressed both in the domain model and in the persistence model.

The persistence entities are located under:

```text
src/main/java/com/deutschhub/infrastructure/learning/persistence/entity/
```

The current JPA mappings use cascading and orphan removal across the Course structure.

This provides implementation evidence that:

```text
Course → Sections
Section → Lessons
Lesson → LessonItems
```

are treated as owned child structures rather than independent persistence aggregates.

---

# 7. Aggregate Boundary

The current Aggregate boundary is:

```text
┌──────────────────────────────────────────┐
│            Course Aggregate              │
│                                          │
│  Course                                  │
│    │                                     │
│    └── Section                           │
│          │                               │
│          └── Lesson                      │
│                │                         │
│                └── LessonItem            │
│                                          │
└──────────────────────────────────────────┘

       │
       │ external references
       ▼

   Media / Quiz
```

The Course Aggregate owns the learning structure.

It does not own Media or Quiz objects.

---

# 8. Value Objects

## 8.1 CEFRLevel

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

`CEFRLevel` represents the CEFR proficiency level used by Course and Lesson.

The current supported values are:

```text
A1
A2
B1
B2
C1
C2
```

It is modeled by value rather than identity.

`CEFRLevel` is therefore a Value Object in the Course domain model.

It is used by:

```text
Course
Lesson
```

---

## 8.2 Money

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Money.java
```

`Money` represents Course price.

It encapsulates:

```text
amount
currency
```

and provides value-based equality semantics.

The Course therefore does not represent price as an unstructured primitive value.

---

# 9. Value Objects Outside the Course Model

The Learning domain also contains other Value Objects, such as `Progress`.

However, not every Value Object under:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/
```

belongs to the Course Aggregate.

For example, `Progress` is associated with Enrollment/learner progress concerns rather than Course structure.

Therefore, this document only identifies Value Objects that are part of the current Course domain model.

---

# 10. External References

## 10.1 Media Reference

`LessonItem` stores:

```text
mediaId
```

rather than a Media entity.

The relationship is therefore:

```text
Course
└── Lesson
    └── LessonItem
         └── mediaId ─────→ Media
```

Media is not owned by the Course Aggregate.

When adding a MEDIA LessonItem, the current Application Service validates the external Media reference through the Media repository port before adding the LessonItem.

This keeps the Media model outside the Course Aggregate boundary.

---

## 10.2 Quiz Reference

`LessonItem` also stores:

```text
quizId
```

for QUIZ LessonItems.

The current relationship is:

```text
Course
└── Lesson
    └── LessonItem
         └── quizId ──────→ Quiz
```

Quiz is not an internal entity of the Course Aggregate.

The current inspected `AddLessonItemService` does not establish a corresponding Quiz repository validation step.

Therefore, Quiz existence validation is not documented here as a confirmed Course domain requirement.

---

# 11. Domain Behavior Distribution

The current model distributes behavior according to entity ownership.

## 11.1 Course Responsibilities

`Course` controls Aggregate-level behavior, including:

```text
Lifecycle
├── publish
├── unpublish
└── softDelete

Structure
├── addSection
├── updateSection
├── deleteSection
├── addLessonToSection
├── updateLesson
├── deleteLesson
└── addLessonItemToLesson
```

Course also coordinates derived Course state such as estimated hours.

---

## 11.2 Section Responsibilities

`Section` is responsible for its own entity state and Lesson collection.

Current responsibilities include:

```text
addLesson()
update()
changeOrderIndex()
softDelete()
```

The Section remains inside the Course Aggregate.

---

## 11.3 Lesson Responsibilities

`Lesson` is responsible for its own state and LessonItem collection.

Current responsibilities include:

```text
update()
addItem()
removeItem()
softDelete()
changeOrderIndex()
```

The Lesson remains inside the Course Aggregate.

---

## 11.4 LessonItem Responsibilities

`LessonItem` is responsible for its own state and type-specific payload rules.

Current responsibilities include:

```text
creation validation
update
softDelete
payload validation
```

The LessonItem remains inside the Course Aggregate through its owning Lesson.

---

# 12. Lifecycle State

The current Course lifecycle is represented by:

```text
published
deletedAt
```

The primary publication lifecycle can be represented as:

```text
             publish
Draft ─────────────────────→ Published
  ▲                              │
  │                              │
  └────────── unpublish ─────────┘
```

Deletion is represented independently through:

```text
deletedAt != null
```

Therefore, the current implementation does not use a dedicated lifecycle state machine object.

No additional state abstraction is introduced by this domain model.

---

# 13. Publication Behavior

Publication is controlled by the Course Aggregate.

The current implementation requires the Course to satisfy publication conditions before changing:

```text
published: false → true
```

The relevant behavior is implemented in:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

The publication rules themselves are documented in:

```text
course-business-rules.md
```

This document therefore treats `publish()` as Aggregate behavior rather than duplicating the complete rule definitions.

---

# 14. Derived State

## 14.1 Estimated Hours

Course maintains:

```text
estimatedHours
```

as derived state.

The current implementation recalculates it through:

```text
Course.recalculateEstimatedHours()
```

The calculation is based on Lesson estimated minutes:

```text
All active/relevant Lessons
        ↓
sum estimatedMinutes
        ↓
convert minutes to hours
        ↓
ceil(...)
        ↓
Course.estimatedHours
```

Structural changes such as adding, updating, or deleting Sections and Lessons trigger recalculation in the current implementation.

The current implementation does not directly add `LessonItem.estimatedMinutes` to the Course total.

Whether this is intentional domain behavior or an incomplete business rule remains open.

---

# 15. Soft Deletion

The current Course structure supports soft deletion.

The following entities contain deletion state:

```text
Course
Section
Lesson
LessonItem
```

Each uses:

```text
deletedAt
```

as part of its state.

This establishes that deletion is modeled as a domain state transition rather than immediate physical removal.

However, the current Course deletion behavior does not establish a confirmed cascade rule from:

```text
Course.softDelete()
```

to:

```text
Section
Lesson
LessonItem
```

Therefore:

```text
Soft deletion support
        = Confirmed

Automatic child soft deletion
        = Open
```

---

# 16. Authorization in the Domain Model

Course mutation behavior currently receives actor information and checks whether the actor is permitted to mutate the Course.

The current rule allows the instructor or an administrator to perform authorized Course mutations.

This behavior is implemented inside Course mutation methods.

Therefore, the current domain model includes authorization checks as part of Aggregate behavior.

Whether this should eventually be extracted into a dedicated domain policy is not currently established as necessary.

No architectural change is proposed here.

---

# 17. Domain Model Integrity

The current model has several implementation details that should be distinguished from the conceptual domain model.

## 17.1 Lesson Order Index

File:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
```

The current `changeOrderIndex(int orderIndex)` method validates the incoming value but does not assign it to the entity state.

Therefore, a valid order-index update may not actually change `Lesson.orderIndex`.

This is an implementation defect and does not change the intended entity relationship.

---

## 17.2 Section Update Null Handling

File:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
```

The current `update(...)` implementation checks existing field values in some conditions instead of consistently checking the incoming values.

This may produce unexpected behavior for partial updates involving `null`.

This is an implementation concern rather than an established business rule.

---

# 18. Open Domain Decisions

The following questions remain unresolved by the current implementation.

## 18.1 Course Deletion Cascade

Should:

```text
Course.softDelete()
```

also soft-delete:

```text
Sections
Lessons
LessonItems
```

?

The current source does not provide enough evidence to establish the intended business rule.

---

## 18.2 LessonItem Duration

Should `LessonItem.estimatedMinutes` contribute to:

```text
Course.estimatedHours
```

?

The current implementation calculates Course estimated hours from Lesson estimated minutes.

The business intention is not yet confirmed.

---

## 18.3 LessonItem Type Mutation

The current model creates a LessonItem with a specific type and does not expose a domain operation for changing:

```text
TEXT
MEDIA
QUIZ
```

after creation.

It is not yet established whether type mutation is intentionally prohibited or simply unsupported.

---

# 19. Domain Model Summary

The current Course domain model can be summarized as:

```text
Course Aggregate
│
├── Aggregate Root
│   └── Course
│
├── Entities
│   ├── Section
│   │   └── Lesson
│   │       └── LessonItem
│   │
│   └── All entities remain inside the Course Aggregate
│
├── Value Objects
│   ├── CEFRLevel
│   └── Money
│
├── Lifecycle State
│   ├── published
│   └── deletedAt
│
├── Derived State
│   └── estimatedHours
│
└── External References
    ├── LessonItem.mediaId → Media
    └── LessonItem.quizId  → Quiz
```

The current model provides a clear Aggregate boundary:

```text
Course
  owns Section
    owns Lesson
      owns LessonItem
```

The Course Aggregate is responsible for maintaining the integrity of this structure, while external concepts such as Media and Quiz remain outside the Aggregate and are referenced by identifiers.

The current implementation does not provide evidence that the Course Aggregate requires a broader abstraction or a different ownership structure.

The identified implementation defects and unresolved business decisions should be handled independently from the established domain model.
\