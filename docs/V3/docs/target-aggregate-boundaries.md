# Target Aggregate Boundaries

## 1. Purpose

This document defines the target Aggregate Boundaries for the Learning Context in DeutschHub V3.

The boundaries are derived from:

- the current Learning domain implementation;
- current application behavior;
- existing domain invariants;
- the target domain model;
- the domain decisions established in the previous analysis.

The purpose is to determine which domain concepts should share a consistency boundary and which concepts should remain independent.

This document does not define:

- Bounded Context boundaries;
- package structure;
- database schema;
- API structure;
- implementation details.

Aggregate boundaries are determined by business consistency, identity, lifecycle, invariants, and ownership rather than by the current package structure.

---

# 2. Aggregate Boundary Principles

The following principles are used when defining the target boundaries.

## 2.1 Aggregates Are Consistency Boundaries

An Aggregate groups domain concepts that must be kept consistent according to business rules.

An Aggregate is therefore not simply a collection of related classes.

```text
Related concepts
    ≠
Same Aggregate
````

---

## 2.2 Aggregate Root Owns the Boundary

Each Aggregate has a Root responsible for protecting the invariants of that boundary.

External application logic should interact with the Aggregate through its Root rather than directly manipulating internal entities.

---

## 2.3 Relationship Does Not Imply Ownership

Two concepts may be strongly related while remaining in separate Aggregates.

For example:

```text
LessonCompletion
        ↓
contributes to
        ↓
Enrollment.Progress
```

does not require:

```text
Enrollment
└── LessonCompletion
```

---

## 2.4 Application Transaction Does Not Define Aggregate Boundary

Multiple Aggregates may participate in one application use case.

Therefore:

```text
One application use case
    ≠
One Aggregate
```

The current `CompleteLessonService` demonstrates this distinction because lesson completion and enrollment progress are updated within the same use case while remaining separate domain concepts.

---

# 3. Course Aggregate

## 3.1 Boundary

**Status:** Confirmed

```text
Course Aggregate
└── Course
    ├── Section
    │    └── Lesson
    │         └── LessonItem
```

The existing root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

The internal entities are:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

Course owns structural behavior for managing its sections, lessons, and lesson items.

Application services such as:

```text
src/main/java/com/deutschhub/application/learning/service/AddSectionToCourseService.java
src/main/java/com/deutschhub/application/learning/service/AddLessonToSectionService.java
src/main/java/com/deutschhub/application/learning/service/AddLessonItemService.java
```

operate through Course.

## 3.2 Boundary Decision

The existing Course Aggregate is retained.

No additional split of the Course hierarchy is introduced at this stage.

---

# 4. Enrollment Aggregate

## 4.1 Boundary

**Status:** Confirmed

```text
Enrollment Aggregate
├── Enrollment
└── Progress
```

The root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

Progress is:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

Enrollment owns participation-related lifecycle and Progress.

The relationship is:

```text
Enrollment
    └── Progress
```

because changes to Progress can affect Enrollment state.

For example, updating progress can contribute to an Enrollment transition toward completion.

## 4.2 Boundary Decision

Enrollment remains an independent Aggregate Root.

Progress remains inside the Enrollment consistency boundary as a Value Object.

---

# 5. LessonCompletion Boundary

## 5.1 Boundary

**Status:** Confirmed / Preferred

```text
Learning Evidence
└── LessonCompletion
```

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

LessonCompletion contains its own identity and represents an observable learning outcome associated with an enrollment and lesson.

It is therefore treated as an independent Evidence Entity rather than an internal entity of Enrollment.

---

## 5.2 Why LessonCompletion Is Not Inside Enrollment

The current application flow in:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

conceptually performs:

```text
Complete Lesson
      ↓
Create LessonCompletion
      ↓
Persist LessonCompletion
      ↓
Update Enrollment.Progress
      ↓
Persist Enrollment
```

This means the use case involves both concepts.

However, the fact that they participate in the same application use case does not establish that they belong to the same Aggregate.

Enrollment is responsible for:

```text
participation lifecycle
        +
course-scoped progress
```

LessonCompletion is responsible for:

```text
recording an observable completion outcome
```

These responsibilities are sufficiently distinct to justify separate boundaries.

---

## 5.3 Target Relationship

```text
Enrollment Aggregate
└── Progress

LessonCompletion
└── Learning Evidence

LessonCompletion
        │
        │ contributes to
        ↓
Enrollment.Progress
```

The relationship is therefore conceptual/application-level rather than Aggregate ownership.

---

# 6. Quiz Aggregate

## 6.1 Boundary

**Status:** Confirmed

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── Answer
```

The Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

The target internal structure is:

```text
Quiz
└── QuizRevision
    └── Question
        └── Answer
```

`Quiz` represents the stable identity, ownership, governance, and lifecycle of an assessment.

`QuizRevision` represents a concrete version of the assessment definition.

`Question` and `Answer` belong to a specific `QuizRevision` and are part of that revision's consistency boundary.

---

## 6.2 Quiz Aggregate Responsibilities

The Quiz Aggregate is responsible for the consistency of the assessment definition.

Conceptually:

```text
Quiz
├── Identity
├── Ownership
├── Governance
├── Visibility
├── Availability
└── Revision Lifecycle
     └── QuizRevision
          ├── Definition
          ├── Questions
          │    └── Answers
          ├── Scoring Configuration
          └── Assessment Rules
```

The Aggregate therefore protects invariants involving:

```text
Quiz
    +
current / draft Revision
    +
Questions
    +
Answers
    +
Revision-specific assessment configuration
```

The exact application services and persistence representation are outside the scope of this document.

---

## 6.3 QuizRevision Boundary

`QuizRevision` is an Entity inside the Quiz Aggregate.

It is not an independent Aggregate Root.

```text
Quiz Aggregate
└── QuizRevision
```

A Revision represents a concrete assessment definition that can become published and subsequently remain historically stable.

Revision-specific data includes concepts such as:

```text
Title
Description
Difficulty
Time Limit
Maximum Score
Passing Percentage
Maximum Attempts
Questions
Answers
```

`maxScore` is derived from the scores of the Questions in the Revision rather than being an independent author-entered source of truth.

---

## 6.4 Revision Lifecycle

The Quiz lifecycle and QuizRevision lifecycle are separate.

Quiz lifecycle:

```text
ACTIVE
   ↓
ARCHIVED
   ↓
DELETED
```

QuizRevision lifecycle:

```text
DRAFT
   ↓
IN_REVIEW
   ↓
PUBLISHED
   ↓
HISTORICAL
```

Publishing a new Revision does not mutate the previously published Revision.

Conceptually:

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED
```

Published and Historical Revisions remain immutable.

The Quiz Aggregate therefore contains multiple historical Revision definitions while maintaining the stable identity of the Quiz itself.

---

## 6.5 Revision Independence

Each Revision owns its own Question and Answer instances.

For example:

```text
Quiz
├── Revision A
│    ├── Question A1
│    │    ├── Answer A
│    │    └── Answer B
│    └── Question A2
│
└── Revision B
     ├── Question B1
     │    ├── Answer A
     │    └── Answer B
     └── Question B2
```

A new Revision may be created from an existing Revision, but the resulting Questions and Answers are independent domain instances.

Therefore:

```text
Question A1
    ≠
Question B1
```

even when Revision B was created from Revision A.

This prevents later changes to a Draft Revision from altering the definition of an already published or historical Revision.

---

## 6.6 Why QuizRevision Is Inside Quiz

`QuizRevision` remains inside the Quiz Aggregate because Revision lifecycle and Quiz governance are part of the same assessment-definition consistency boundary.

The Quiz Aggregate must be able to enforce rules such as:

```text
A Quiz must have a valid Revision before publication.

A Revision must contain valid Questions before publication.

A Question must satisfy its Answer invariants before publication.

A Published Revision must remain immutable.

A Historical Revision must remain immutable.

A new Revision must belong to the same Quiz identity.
```

These rules concern the integrity of the assessment definition owned by the Quiz.

Therefore:

```text
QuizRevision
    ∈
Quiz Aggregate
```

rather than:

```text
QuizRevision
    =
Independent Aggregate Root
```

---

## 6.7 Quiz and QuizAttempt Are Separate

The Quiz Aggregate represents the assessment definition:

```text
Quiz
└── QuizRevision
    └── Question
        └── Answer
```

The QuizAttempt Aggregate represents the learner's execution:

```text
QuizAttempt
└── UserAnswer(s)
```

Therefore:

```text
Quiz
    ≠
QuizAttempt
```

A Quiz may have many Attempts:

```text
Quiz
├── Attempt A
├── Attempt B
└── Attempt C
```

but these Attempts are not internal entities of the Quiz Aggregate.

---

## 6.8 Revision Binding of QuizAttempt

Each `QuizAttempt` must be bound to the exact Published `QuizRevision` used when the Attempt was started.

Conceptually:

```text
QuizAttempt
    └── QuizRevision identity
```

The Attempt does not dynamically follow the latest Published Revision.

For example:

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED

Attempt 1 → Revision A
Attempt 2 → Revision B
```

An existing Attempt therefore remains evaluated against the Revision it originally started with.

Publishing a newer Revision does not alter the definition used by an existing Attempt.

This preserves the historical meaning of the assessment execution.

---

## 6.9 Aggregate Boundary Summary

The target Quiz Aggregate is therefore:

```text
┌──────────────────────────────────┐
│ Quiz Aggregate                   │
│                                  │
│ Quiz                             │
│ └── QuizRevision                 │
│      └── Question                │
│           └── Answer             │
└──────────────────────────────────┘
```

While assessment execution remains separate:

```text
┌──────────────────────────────────┐
│ QuizAttempt Aggregate            │
│                                  │
│ QuizAttempt                      │
│ └── UserAnswer(s)                │
└──────────────────────────────────┘
```

The distinction is:

```text
Quiz Aggregate
    → Assessment Definition

QuizAttempt Aggregate
    → Assessment Execution
    → Learning Evidence
```

---

## 6.10 Boundary Decision

The target boundary is:

```text
Quiz Aggregate
└── Quiz
    └── QuizRevision
        └── Question
            └── Answer
```

with:

```text
QuizAttempt
    → Separate Aggregate Root
```

`QuizRevision` is intentionally not introduced as a separate Aggregate Root.

The Quiz Aggregate owns the consistency of the assessment definition, while QuizAttempt independently owns the lifecycle and state of an individual learner execution.

---

# 7. QuizAttempt Aggregate

## 7.1 Boundary

**Status:** Confirmed

```text
QuizAttempt Aggregate
└── QuizAttempt
    └── UserAnswer(s)
```

The Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

QuizAttempt represents one learner-specific execution of a Published QuizRevision.

It has its own identity, lifecycle, answers, and evaluation state.

Conceptually:

```text
Quiz
    ↓
Assessment Definition

QuizRevision
    ↓
Concrete Assessment Definition

QuizAttempt
    ↓
Assessment Execution
    ↓
Learning Evidence
```

## 7.2 Revision Binding

Each QuizAttempt is bound to the exact Published `QuizRevision` used when the Attempt was created.

Conceptually:

```text
QuizAttempt
    └── QuizRevision identity
```

The Attempt does not dynamically follow the latest Published Revision.

For example:

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED

Attempt 1 → Revision A
Attempt 2 → Revision B
```

An existing Attempt therefore continues to use the Revision under which it was started.

## 7.3 Why QuizAttempt Is Separate from Quiz

A Quiz may have many learner Attempts:

```text
Quiz
 ├── Attempt A
 ├── Attempt B
 └── Attempt C
```

The Attempts represent independent learner executions and have their own lifecycle.

Therefore, making all Attempts internal to the Quiz Aggregate would unnecessarily expand the consistency boundary.

## 7.4 Boundary Decision

QuizAttempt remains a separate Aggregate Root.

QuizAttempt may additionally serve as Learning Evidence.

These are two different classifications:

```text
Aggregate Root
    → defines consistency ownership

Learning Evidence
    → describes business meaning
```

---

# 8. Competency Boundary

## 8.1 Status

**Open**

No explicit Competency implementation was found in the current Learning domain.

The target model establishes Competency as a distinct domain concept, but current evidence does not establish:

* its lifecycle;
* its invariants;
* its transactional requirements;
* its ownership;
* its final consistency boundary.

Therefore, no Aggregate Root is introduced yet.

```text
Competency
    → Entity candidate
    → Aggregate boundary OPEN
```

---

## 8.2 Boundary Principle

Competency must remain distinct from:

```text
Progress
Evidence
Course Completion
```

The target model does not assume:

```text
Quiz Score
    ↓
Competency update
```

without explicit business rules.

The final boundary should be determined once the business rules governing competency changes are established.

---

# 9. Learner Current Level Boundary

## 9.1 Status

**Open**

The target model distinguishes:

```text
Course Level
Learner Current Level
Certification Level
```

The existing CEFR level representation provides evidence for a value/classification concept, but the current Learning domain does not establish a complete learner-level Current Level lifecycle.

Therefore:

```text
Learner Current Level
    → Learner State concept
    → Aggregate boundary OPEN
```

No independent Current Level Aggregate is introduced at this stage.

---

# 10. Learner State Boundary

## 10.1 Status

**No Single Aggregate Boundary**

Learner State is treated as a business responsibility rather than a single Aggregate.

It represents the broader state of a learner that may be derived from or informed by multiple learning sources.

Potential areas include:

```text
Course-scoped Progress
Competency
Current Level
Learning History
XP
Streak
Achievements
Statistics
```

These concepts do not currently have enough evidence to establish a shared consistency boundary.

Therefore, the target model does not introduce:

```text
LearnerState Aggregate
```

as a replacement for the current `UserProgress`.

Importantly, Course-scoped `Progress` remains owned by the Enrollment Aggregate:

```text
Enrollment
└── Progress
```

Learner State may consume or derive information from such course-scoped state, but this does not make `Progress` an internal entity of a Learner State Aggregate.

---

## 10.2 Boundary Principle

The following distinction must remain explicit:

```text
Enrollment.Progress
    → Course-scoped learning progress

Learner State
    → Broader learner-level condition
```

Therefore:

```text
Progress
    ≠
Complete Learner State
```

The final representation of Learner State remains open until its business rules, sources of truth, and update semantics are established.

---

# 11. Learning Activity Boundary

## 11.1 Status

**Open**

Learning Activity is a target domain concept representing an action or interaction performed by the learner.

Examples may include:

* Practice;
* Review;
* Assessment;
* Listening;
* Speaking;
* Reading;
* Writing.

However, the current implementation does not establish a generic Activity lifecycle or invariant model.

Therefore:

```text
LearningActivity
    → Domain Concept
    → Aggregate boundary OPEN
```

No generic `LearningActivity` Aggregate Root is introduced.

---

## 11.2 LessonItem Is Not Automatically an Activity Aggregate

The current implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

represents content within a Lesson.

Its supported types include:

```text
TEXT
MEDIA
QUIZ
```

Therefore:

```text
LessonItem
    ≠
Generic Learning Activity
```

The target Activity boundary should be determined only after the business semantics of specific activities are established.

---

# 12. Learning Direction Boundaries

## 12.1 Status

**Open**

Learning Direction is a business responsibility concerned with determining or recommending what a learner should do next.

Potential concepts include:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

These concepts may have different lifecycles and consistency requirements.

Therefore, the target model does not introduce:

```text
LearningDirection Aggregate
```

as a generic Aggregate Root.

---

## 12.2 Specific Direction Concepts

The final boundaries for:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

remain open.

They should be evaluated individually once their business rules are established.

---

# 13. Target Aggregate Overview

The currently established boundaries are:

```text
┌──────────────────────────────────┐
│ Course Aggregate                 │
│                                  │
│ Course                           │
│ └── Section                      │
│      └── Lesson                  │
│           └── LessonItem         │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│ Enrollment Aggregate             │
│                                  │
│ Enrollment                       │
│ └── Progress                     │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│ Quiz Aggregate                   │
│                                  │
│ Quiz                             │
│ └── QuizRevision                 │
│      └── Question                │
│           └── Answer             │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│ QuizAttempt Aggregate            │
│                                  │
│ QuizAttempt                      │
│ └── UserAnswer(s)                │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│ Learning Evidence                │
│                                  │
│ LessonCompletion                 │
└──────────────────────────────────┘
```

Additional target concepts remain outside a finalized Aggregate boundary:

```text
Competency
Current Level
Learning Activity
Learning Plan
Recommendation
Review Due
Learning Goal
```

---

# 14. Aggregate Relationships

The target model describes business relationships between Aggregates and domain concepts without implying direct Aggregate ownership.

A simplified conceptual view is:

```text
Course Aggregate
        │
        │ provides learning structure
        ↓
Learning Flow
        │
        ├──────────────→ Learning Activity
        │
        └──────────────→ Learning Activity
                              │
                              ↓
                       Learning Evidence
                              │
                    ┌─────────┴─────────┐
                    ↓                   ↓
             LessonCompletion      QuizAttempt
                    │                   │
                    │                   │
                    ↓                   ↓
              Enrollment          Assessment Result
                    │
                    ↓
                 Progress
```

This diagram represents business relationships and evidence flow.

It does not imply:

```text
Course
    └── LearningActivity
```

or:

```text
Course
    └── LearningEvidence
```

as Aggregate ownership.

A Learning Activity may exist independently of a Course, Section, or Lesson.

Similarly, Learning Evidence may remain an independent domain record or Aggregate depending on its own business boundary.

The exact relationships between Learning Activity, Learning Evidence, Learner State, and Learning Direction remain subject to their respective domain decisions.

---

# 15. Aggregate Reference Rules

The following rules apply to the target model.

## 15.1 Aggregates Should Reference Other Aggregates by Identity

An Aggregate should not require another Aggregate to be loaded as an internal object merely because the two concepts are related.

For example:

```text
Enrollment
    → Course ID
```

is conceptually different from:

```text
Enrollment
    → complete Course Aggregate
```

The exact implementation representation remains outside this document.

---

## 15.2 Evidence Does Not Become Internal State Automatically

A Learning Evidence record may contribute to an Aggregate's state without becoming an internal entity of that Aggregate.

For example:

```text
LessonCompletion
        ↓
Enrollment.Progress
```

does not imply:

```text
Enrollment
└── LessonCompletion[]
```

---

## 15.3 Aggregate Boundaries Should Protect Business Invariants

The boundary should be expanded only when multiple concepts must be changed and validated together to preserve a business invariant.

Otherwise, concepts should remain independent.

---

# 16. Confirmed vs. Open Boundaries

| Concept            | Target Boundary             | Status                |
| ------------------ | --------------------------- | --------------------- |
| Course             | Course Aggregate            | Confirmed             |
| Section            | Inside Course               | Confirmed             |
| Lesson             | Inside Course               | Confirmed             |
| LessonItem         | Inside Course               | Confirmed             |
| Enrollment         | Enrollment Aggregate        | Confirmed             |
| Progress           | Inside Enrollment           | Confirmed             |
| LessonCompletion   | Independent Evidence Entity | Confirmed / Preferred |
| Quiz               | Quiz Aggregate              | Confirmed             |
| QuizRevision       | Inside Quiz Aggregate       | Confirmed             |
| Question           | Inside QuizRevision         | Confirmed             |
| Answer             | Inside Question             | Confirmed             |
| QuizAttempt        | Independent Aggregate       | Confirmed             |
| Competency         | TBD                         | Open                  |
| Current Level      | TBD                         | Open                  |
| Learning Activity  | TBD                         | Open                  |
| Learning Plan      | TBD                         | Open                  |
| Recommendation     | TBD                         | Open                  |
| Review Due         | TBD                         | Open                  |
| Learning Goal      | TBD                         | Open                  |
| Learner State      | No single Aggregate         | Confirmed decision    |
| Learning Direction | No generic Aggregate        | Confirmed decision    |

---

# 17. Boundaries Intentionally Not Introduced

The following Aggregate Roots are intentionally not introduced:

```text
LearnerState
LearningDirection
LearningActivity
Competency
CurrentLevel
```

The absence of an Aggregate Root does not mean that the concept is not part of the domain.

It means that the available evidence is not sufficient to establish an independent consistency boundary.

---

# 18. Final Boundary Principles

The target Learning model follows these principles:

```text
Course
    → owns Course Structure

Enrollment
    → owns participation lifecycle and Course-scoped Progress

LessonCompletion
    → records Learning Evidence independently

Quiz
    → owns assessment identity, governance, and Revision lifecycle

QuizRevision
    → owns a concrete assessment definition

QuizAttempt
    → owns learner-specific assessment execution
```

Learner State and Learning Direction remain broader business responsibilities without assuming a single Aggregate boundary.

The most important boundary decisions are:

```text
Enrollment ≠ LessonCompletion

Enrollment ≠ Learner State

Progress ∈ Enrollment

LessonCompletion ∉ Enrollment

Quiz ≠ QuizAttempt

QuizRevision ∈ Quiz Aggregate

Evidence ≠ Learner State

Course Structure ≠ Learning Activity
```

These boundaries provide the domain foundation for subsequent architecture decisions.

The next stage should determine how these business boundaries should be reflected in the target architecture and code organization without allowing the current package structure to dictate the domain model.

````
