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
```

The existing root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

Quiz represents the definition of an assessment.

It is therefore distinct from the learner's execution of that assessment.

---

# 7. QuizAttempt Aggregate

## 7.1 Boundary

**Status:** Confirmed

```text
QuizAttempt Aggregate
└── QuizAttempt
    └── UserAnswer(s)
```

The existing root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

QuizAttempt represents a learner-specific assessment execution.

It has its own identity and assessment state.

Conceptually:

```text
Quiz
    ↓
Assessment Definition

QuizAttempt
    ↓
Assessment Execution
    ↓
Learning Evidence
```

---

## 7.2 Why QuizAttempt Is Separate from Quiz

A Quiz can have multiple learner attempts:

```text
Quiz
 ├── Attempt A
 ├── Attempt B
 └── Attempt C
```

The attempts belong to different learner executions and have their own lifecycle.

Therefore, making all attempts internal to the Quiz Aggregate would unnecessarily expand the consistency boundary.

## 7.3 Boundary Decision

Quiz and QuizAttempt remain independent Aggregate Roots.

QuizAttempt can additionally serve as Learning Evidence.

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

Conceptually:

```text
Learner State
├── Progress
├── Competency
├── Current Level
├── Learning History
├── XP
├── Streak
├── Achievements
└── Statistics
```

These concepts do not currently have enough evidence to establish a shared consistency boundary.

Therefore, the target model does not introduce:

```text
LearnerState Aggregate
```

as a replacement for the current `UserProgress`.

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

The target model can be represented conceptually as:

```text
                  Course Aggregate
                         │
                         │
                    Course Structure
                         │
                         ↓
                  Learning Activity
                         │
                         ↓
                  Learning Evidence
                    ┌────┴────┐
                    ↓         ↓
            LessonCompletion  QuizAttempt
                    │         │
                    │         │
                    ↓         ↓
              Enrollment   Assessment
                 │
                 ↓
              Progress
                 │
                 ↓
           Learner State
             ├── Competency
             └── Current Level
                 │
                 ↓
        Learning Direction
                 │
                 ↓
       Next Learning Activity
```

This diagram describes business relationships and does not imply direct Aggregate ownership.

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
    → owns assessment definition

QuizAttempt
    → owns learner-specific assessment execution

Learner State
    → represents broader learner condition without assuming one Aggregate

Learning Direction
    → determines or recommends next learning actions without assuming one Aggregate
```

The most important boundary decisions are:

```text
Enrollment ≠ LessonCompletion

Enrollment ≠ Learner State

Progress ∈ Enrollment

LessonCompletion ∉ Enrollment

Quiz ≠ QuizAttempt

Evidence ≠ Learner State

Course Structure ≠ Learning Activity
```

These boundaries provide the domain foundation for subsequent architecture decisions.

The next stage should determine how these business boundaries should be reflected in the target architecture and code organization without allowing the current package structure to dictate the domain model.

