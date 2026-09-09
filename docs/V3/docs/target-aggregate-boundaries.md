# Target Aggregate Boundaries

## 1. Purpose

This document defines the target Aggregate Boundaries for the Learning Context in DeutschHub V3.

The boundaries are derived from:

- the current Learning domain implementation;
- current application behavior;
- existing domain invariants;
- the target domain model;
- established domain decisions;
- and subsequent business analysis of Assessment, Learning Evidence, Competency, and Current Level.

The purpose is to determine which domain concepts should share a consistency boundary and which concepts should remain independent.

This document does not define:

- Bounded Context boundaries;
- package structure;
- database schema;
- API structure;
- implementation details.

Aggregate boundaries are determined by:

- business consistency;
- identity;
- lifecycle;
- invariants;
- ownership;
- and transactional consistency requirements.

They are not determined merely by the current package structure or by conceptual relationships between objects.

---

# 2. Aggregate Boundary Principles

## 2.1 Aggregates Are Consistency Boundaries

An Aggregate groups domain concepts that must be kept consistent according to business rules.

An Aggregate is therefore not simply a collection of related classes.

```text
Related concepts
    ≠
Same Aggregate
````

Two concepts may be strongly related while still belonging to different Aggregates.

---

## 2.2 Aggregate Root Owns the Boundary

Each Aggregate has a Root responsible for protecting the invariants of that boundary.

External application logic should interact with the Aggregate through its Root rather than directly manipulating internal entities.

---

## 2.3 Relationship Does Not Imply Ownership

Two concepts may participate in the same business flow while remaining in separate Aggregate boundaries.

For example:

```text
LessonCompletion
        ↓
contributes to
        ↓
Enrollment.Progress
```

does not imply:

```text
Enrollment
└── LessonCompletion
```

---

## 2.4 Application Use Case Does Not Define Aggregate Boundary

Multiple Aggregates may participate in one application use case.

Therefore:

```text
One application use case
    ≠
One Aggregate
```

The current `CompleteLessonService` demonstrates this distinction because lesson completion and enrollment progress participate in the same business flow while remaining separate domain concepts.

---

## 2.5 Domain Concept Does Not Automatically Require an Aggregate

The existence of a domain concept does not automatically imply that it must become:

* an Aggregate Root;
* an Entity with an independent repository;
* or an independent persistence boundary.

The boundary must be justified by concrete business consistency requirements.

---

# 3. Course Aggregate

## 3.1 Boundary

**Status: Confirmed**

```text
Course Aggregate
└── Course
    ├── Section
    │    └── Lesson
    │         └── LessonItem
```

The Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

The internal entities are:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The Course Aggregate owns the consistency of the Course structure.

---

## 3.2 Course Boundary Decision

The following structure remains inside the Course Aggregate:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

No split of the Course hierarchy is introduced at this stage.

The current application services that manipulate Course structure operate through Course, including:

```text
src/main/java/com/deutschhub/application/learning/service/AddSectionToCourseService.java

src/main/java/com/deutschhub/application/learning/service/AddLessonToSectionService.java

src/main/java/com/deutschhub/application/learning/service/AddLessonItemService.java
```

---

## 3.3 Course Level

Course Level is a property of the learning content represented by the Course.

The existing CEFR representation is:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

The existence of `CEFRLevel` does not create a separate Aggregate boundary.

Course Level remains inside the Course domain model.

It must not be confused with:

```text
Learner Current Level
Certification Level
```

Therefore:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

---

# 4. Enrollment Aggregate

## 4.1 Boundary

**Status: Confirmed**

```text
Enrollment Aggregate
└── Enrollment
    └── Progress
```

The Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

Progress is represented by:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

---

## 4.2 Boundary Decision

Enrollment remains an independent Aggregate Root.

Progress remains inside the Enrollment consistency boundary as a Value Object.

```text
Enrollment
    └── Progress
```

Enrollment is responsible for participation-related state and Course-scoped progress.

---

## 4.3 Enrollment and Course

Enrollment represents a learner's participation in a Course.

Conceptually:

```text
Course
    ↑
    │
Enrollment
```

Course and Enrollment are separate Aggregate Roots.

A Course may therefore be referenced by Enrollment without making Enrollment an internal entity of the Course Aggregate.

---

# 5. LessonCompletion Boundary

## 5.1 Boundary

**Status: Confirmed / Preferred**

```text
Learning Evidence
└── LessonCompletion
```

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

LessonCompletion represents an observable historical completion event associated with a learner's enrollment and lesson.

It therefore remains independent from the Enrollment Aggregate.

---

## 5.2 Why LessonCompletion Is Not Inside Enrollment

The application flow in:

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

The same use case involving both concepts does not make them one Aggregate.

Their responsibilities remain distinct:

```text
Enrollment
    → participation lifecycle
    → Course-scoped Progress

LessonCompletion
    → historical learning evidence
```

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

This is a business relationship rather than Aggregate ownership.

---

# 6. Quiz Aggregate

## 6.1 Boundary

**Status: Confirmed**

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

---

## 6.2 Quiz Responsibilities

The Quiz Aggregate owns the consistency of the assessment definition represented by the Quiz.

Conceptually:

```text
Quiz
├── stable identity
├── ownership
├── governance
└── Revision lifecycle
     └── QuizRevision
          ├── definition
          ├── questions
          │    └── answers
          └── assessment configuration
```

The Aggregate protects invariants involving:

```text
Quiz
    +
QuizRevision
    +
Questions
    +
Answers
    +
Revision-specific configuration
```

---

# 7. QuizRevision Boundary

## 7.1 Boundary

**Status: Confirmed**

`QuizRevision` is an Entity inside the Quiz Aggregate.

It is not an independent Aggregate Root.

```text
Quiz Aggregate
└── QuizRevision
```

A Revision represents a concrete version of the Quiz definition.

---

## 7.2 Revision Contents

A QuizRevision may contain concepts such as:

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

`maxScore` is derived from Question scores rather than becoming an independent source of truth.

---

## 7.3 Revision Lifecycle

The established Revision lifecycle is:

```text
DRAFT
   ↓
IN_REVIEW
   ↓
PUBLISHED
   ↓
HISTORICAL
```

A published Revision remains historically stable.

Publishing a new Revision does not mutate the previously published Revision.

Conceptually:

```text
Quiz
├── Revision A → HISTORICAL
└── Revision B → PUBLISHED
```

---

## 7.4 Revision Independence

Each Revision owns its own Questions and Answers.

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

Questions belonging to different Revisions are independent domain instances.

Therefore:

```text
Question A1
    ≠
Question B1
```

even if Revision B was created from Revision A.

---

# 8. QuizAttempt Aggregate

## 8.1 Boundary

**Status: Confirmed**

```text
QuizAttempt Aggregate
└── QuizAttempt
    ├── UserAnswer(s)
    └── QuestionResult(s)
```

The Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

QuizAttempt represents one learner-specific execution of a Published QuizRevision.

---

## 8.2 Quiz and QuizAttempt Are Separate

The Quiz Aggregate represents:

```text
Assessment Definition
```

The QuizAttempt Aggregate represents:

```text
Assessment Execution
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

but those Attempts are not internal entities of the Quiz Aggregate.

---

## 8.3 Revision Binding

Each QuizAttempt binds to the exact Published QuizRevision used when the Attempt starts.

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

Publishing Revision B does not change the Revision used by Attempt 1.

---

## 8.4 Attempt and Evidence

QuizAttempt may serve as Learning Evidence.

This is a classification separate from Aggregate ownership:

```text
Aggregate Root
    → defines consistency ownership

Learning Evidence
    → defines business meaning
```

Therefore:

```text
QuizAttempt
    = Aggregate Root

QuizAttempt
    = may serve as Learning Evidence
```

without requiring:

```text
Learning Evidence
    = Aggregate
```

---

# 9. Assessment Domain Boundary

## 9.1 Business Boundary

**Status: Confirmed Domain Concept / Aggregate Boundary OPEN**

Assessment is broader than Quiz.

The target conceptual structure is:

```text
Assessment
    │
    ├── Component
    │      └── Task(s)
    │
    ├── Completion Policy
    │
    ├── Time Limit
    │
    └── Attempt Rules
```

Assessment may evaluate one or more Skill Dimensions, such as:

```text
Listening
Speaking
Reading
Writing
Grammar
Vocabulary
```

An Assessment does not have to evaluate every dimension.

---

## 9.2 Assessment Component

A Component belongs to one Skill Dimension.

```text
Assessment
├── Listening Component
│    ├── Task 1
│    └── Task 2
│
├── Reading Component
│    ├── Task 1
│    └── Task 2
│
└── Speaking Component
     └── Task 1
```

One Component may contain multiple Tasks.

The Component boundary itself is not established as an independent Aggregate.

---

## 9.3 Assessment Task

A Task is an executable part of an Assessment Component.

Different Tasks may use different Evaluation Mechanisms.

The exact representation of Evaluation Mechanism remains OPEN.

Therefore:

```text
Assessment
    ≠
Quiz
```

and:

```text
Assessment
    ≠
Assessment Component
```

and:

```text
Assessment Task
    ≠
Aggregate Root
```

unless later business requirements establish otherwise.

---

## 9.4 Assessment Attempt

Each execution of an Assessment by a User is a separate Assessment Attempt.

The target business rule is:

```text
One User
+
One Assessment
    ↓
At most one active Assessment Attempt
```

An Attempt may be resumed after:

* closing the browser;
* losing connection;
* changing device;
* pausing the execution.

These events do not automatically create a new Attempt.

---

## 9.5 Assessment Attempt Boundary

**Status: OPEN**

The business concept of Assessment Attempt is confirmed.

However, the final Aggregate boundary is not yet fixed.

The existing QuizAttempt Aggregate provides a concrete precedent for assessment execution, but the broader Assessment model must not automatically inherit every Quiz-specific rule.

Therefore, this document does not yet assert:

```text
Assessment
└── AssessmentAttempt
```

as a finalized Aggregate boundary.

---

## 9.6 Assessment Result

Assessment Result is the historical official result of one Assessment Attempt.

Conceptually:

```text
Assessment Attempt
    ↓
Component Result(s)
    ↓
Assessment Result
```

The result belongs historically to the Attempt that produced it.

A later Attempt does not modify a previous Assessment Result.

Assessment Result is therefore a historical result concept rather than an independent Aggregate Root.

---

## 9.7 Component Result

Component Result represents the evaluated result of an Assessment Component.

Conceptually:

```text
Assessment Attempt
    └── Component Result(s)
```

It is historical evidence associated with that Attempt.

It does not automatically become an independent Aggregate.

---

## 9.8 Assessment Revision

An Assessment Attempt must remain bound to a stable Assessment definition/version from start to completion.

The exact Assessment Revision model and lifecycle remain OPEN.

Therefore:

```text
Assessment Revision
    → required concept for execution stability

Assessment Revision Aggregate boundary
    → OPEN
```

---

## 9.9 Assessment and Quiz

Quiz remains an existing assessment-related domain concept.

The target model establishes:

```text
Assessment
    > broader concept
```

than:

```text
Quiz
```

However, the exact structural relationship between:

```text
Assessment
Quiz
Task
Evaluation Mechanism
```

is not fully closed.

Therefore this document does not assert:

```text
Quiz = Assessment
```

or:

```text
Quiz = Assessment Task
```

or:

```text
Quiz = Evaluation Mechanism
```

The confirmed relationship is only that Quiz may participate in broader assessment behavior.

---

# 10. Competency Boundary

## 10.1 Business Meaning

Competency represents a learner's demonstrated capability within a defined learning domain.

For the current DeutschHub scope, the relevant competency scope is German Language.

The conceptual identity is:

```text
User
+
Competency Scope
```

For a given User and Competency Scope:

```text
At most one Competency
```

exists.

---

## 10.2 Competency Is a Learner-State Concept

Competency belongs to the Learner State responsibility.

It is distinct from:

```text
Progress
Learning Evidence
Assessment Result
Course Completion
```

Therefore:

```text
Competency
    ≠
Progress
```

and:

```text
Competency
    ≠
Assessment Result
```

---

## 10.3 Competency Lifecycle

The confirmed business lifecycle is:

```text
UNASSESSED
    ↓
ASSESSED
```

An initially unassessed Competency has:

```text
Current Level = UNKNOWN
```

A valid passed Level Assessment establishes the corresponding Current Level.

A higher valid passed Level Assessment may update the Current Level upward.

A lower-level passed Assessment does not lower an already established level.

A failed Assessment does not automatically downgrade the Competency.

---

## 10.4 Competency Aggregate Boundary

**Status: OPEN / DEFERRED**

Competency has:

* identity;
* lifecycle;
* invariants;
* and learner-state meaning.

However, the final Aggregate boundary is intentionally deferred.

The current business analysis does not yet require Competency to be an independent Aggregate Root.

Therefore:

```text
Competency
    → confirmed learner-state domain concept
    → Aggregate boundary OPEN / DEFERRED
```

No independent Competency Aggregate is introduced at this stage.

---

# 11. Current Level Boundary

## 11.1 Business Meaning

Current Level represents the current CEFR proficiency classification established by DeutschHub for a learner's Competency based on valid passed Level Assessment evidence.

Conceptually:

```text
Competency
    └── Current Level
```

---

## 11.2 Current Level Is Not an Aggregate

Current Level has no independent identity.

It does not require:

```text
CurrentLevelId
```

or an independent repository.

It is a value/state belonging to Competency.

Therefore:

```text
Current Level
    ≠
Independent Aggregate Root
```

and:

```text
Current Level
    ≠
Independent Entity
```

---

## 11.3 CEFR Representation

The existing:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

provides the CEFR classification:

```text
A1
A2
B1
B2
C1
C2
```

This existing Value Object is a representation baseline.

However:

```text
CEFRLevel
    ≠
Learner Current Level business concept
```

`CEFRLevel` represents the classification value.

Current Level represents that value as the learner's currently established proficiency state.

---

## 11.4 Current Level Establishment

A valid passed Level Assessment may establish a Current Level directly.

Sequential progression is not required.

For example:

```text
UNKNOWN
   ↓
B2
```

is valid when supported by a valid passed Level Assessment.

The system does not require:

```text
A1 → A2 → B1 → B2
```

before B2 can be established.

---

## 11.5 Current Level Must Not Decrease Automatically

Current Level does not decrease merely because:

* a learner fails a later Assessment;
* a learner passes a lower-level Assessment;
* a new Assessment Revision is published.

An explicit reset or revocation mechanism is not part of the current V3 boundary model.

---

# 12. Learner State Boundary

## 12.1 Status

**Confirmed Business Responsibility / No Single Aggregate**

Learner State is a business responsibility rather than one Aggregate Root.

It may contain or expose concepts such as:

```text
Competency
Current Level
Course Progress
Learning History
XP
Streak
Achievements
Statistics
```

These concepts do not automatically share one consistency boundary.

---

## 12.2 Course Progress Is Not Inside Learner State Aggregate

Course-scoped Progress remains inside:

```text
Enrollment Aggregate
└── Progress
```

Learner State may use or derive information from Course Progress without owning it.

Therefore:

```text
Enrollment.Progress
    ≠
Learner State Aggregate
```

---

## 12.3 Learner State Boundary Decision

No generic:

```text
LearnerState Aggregate
```

is introduced.

The exact boundaries of future learner-state concepts are determined individually according to their own business invariants and consistency requirements.

---

# 13. Learning Activity Boundary

## 13.1 Status

**OPEN / DEFERRED**

Learning Activity is a domain concept representing a learner-facing learning action.

Potential examples include:

```text
Practice
Review
Assessment
Listening
Speaking
Reading
Writing
```

The current implementation does not establish a generic Learning Activity Aggregate.

Therefore:

```text
Learning Activity
    → Domain Concept
    → Aggregate Boundary OPEN / DEFERRED
```

---

## 13.2 LessonItem Is Not Automatically Learning Activity

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

with types such as:

```text
TEXT
MEDIA
QUIZ
```

LessonItem belongs to the Course Aggregate.

This does not establish:

```text
LessonItem
    =
Learning Activity
```

The exact relationship remains open.

---

# 14. Learning Direction Boundary

## 14.1 Status

**OPEN / DEFERRED**

Learning Direction represents the responsibility for determining what the learner should do next.

Potential concepts include:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

The exact Aggregate boundaries of these concepts are not established.

---

## 14.2 Boundary Decision

No generic:

```text
LearningDirection Aggregate
```

is introduced.

Each future concept should be evaluated according to its own:

* identity;
* lifecycle;
* invariants;
* ownership;
* consistency requirements.

---

# 15. UserProgress

## 15.1 Current Implementation

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

Its responsibilities overlap with Course-scoped Progress already represented by:

```text
Enrollment
└── Progress
```

---

## 15.2 Target Boundary Decision

`UserProgress` is not retained as the target representation of Learner State.

The target responsibilities are separated as follows:

```text
Enrollment
└── Progress
    → Course-scoped advancement

LessonCompletion
    → Learning Evidence

QuizAttempt
    → Assessment execution
    → may serve as Learning Evidence

Assessment Result
    → Historical Assessment Result

Competency
    → Learner capability

Current Level
    → Established CEFR proficiency state
```

Therefore:

```text
UserProgress
    ≠
Target Learner State Aggregate
```

---

# 16. Aggregate Reference Rules

## 16.1 Aggregate-to-Aggregate Relationships

Aggregates may reference other Aggregates through identity without becoming part of the same boundary.

For example:

```text
Enrollment
    → Course identity
```

does not imply:

```text
Enrollment
    → Course Aggregate
```

as an internal object.

The exact implementation representation is outside this document.

---

## 16.2 Historical Evidence Does Not Become Aggregate State Automatically

Evidence may contribute to Aggregate state without becoming an internal entity.

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

## 16.3 Aggregate Boundaries Protect Invariants

A boundary should be expanded only when multiple concepts must be changed and validated together to preserve a business invariant.

Otherwise, concepts should remain independent.

---

# 17. Target Aggregate Overview

The currently established Aggregate boundaries are:

```text
┌──────────────────────────────────┐
│ Course Aggregate                 │
│                                  │
│ Course                           │
│ └── Section                      │
│      └── Lesson                  │
│           └── LessonItem         │
└──────────────────────────────────┘
```

```text
┌──────────────────────────────────┐
│ Enrollment Aggregate             │
│                                  │
│ Enrollment                       │
│ └── Progress                     │
└──────────────────────────────────┘
```

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

```text
┌──────────────────────────────────┐
│ QuizAttempt Aggregate            │
│                                  │
│ QuizAttempt                      │
│ ├── UserAnswer(s)                │
│ └── QuestionResult(s)            │
└──────────────────────────────────┘
```

The following are confirmed domain concepts but do not currently have finalized independent Aggregate boundaries:

```text
Assessment
Assessment Component
Assessment Task
Assessment Attempt
Assessment Result
Competency
Learning Activity
Learning Direction
```

Current Level is not an independent Aggregate or Entity.

It is a state/value belonging to Competency.

---

# 18. Aggregate Classification

| Concept              | Target Boundary                  | Status                             |
| -------------------- | -------------------------------- | ---------------------------------- |
| Course               | Course Aggregate Root            | Confirmed                          |
| Section              | Inside Course Aggregate          | Confirmed                          |
| Lesson               | Inside Course Aggregate          | Confirmed                          |
| LessonItem           | Inside Course Aggregate          | Confirmed                          |
| Enrollment           | Enrollment Aggregate Root        | Confirmed                          |
| Progress             | Inside Enrollment Aggregate      | Confirmed                          |
| LessonCompletion     | Independent Evidence Entity      | Confirmed / Preferred              |
| Quiz                 | Quiz Aggregate Root              | Confirmed                          |
| QuizRevision         | Inside Quiz Aggregate            | Confirmed                          |
| Question             | Inside QuizRevision              | Confirmed                          |
| Answer               | Inside Question                  | Confirmed                          |
| QuizAttempt          | Independent Aggregate Root       | Confirmed                          |
| UserAnswer           | Inside QuizAttempt               | Confirmed                          |
| QuestionResult       | Inside QuizAttempt               | Confirmed concept                  |
| Assessment           | Domain Concept                   | Aggregate boundary OPEN            |
| Assessment Component | Domain Concept                   | Aggregate boundary OPEN            |
| Assessment Task      | Domain Concept                   | Aggregate boundary OPEN            |
| Assessment Attempt   | Domain Concept                   | Aggregate boundary OPEN            |
| Component Result     | Historical Assessment Evidence   | Aggregate boundary OPEN            |
| Assessment Result    | Historical Assessment Result     | Aggregate boundary OPEN            |
| Competency           | Learner State Domain Concept     | Aggregate boundary OPEN / DEFERRED |
| Current Level        | State/Value of Competency        | Not an independent Aggregate       |
| Learner State        | Business Responsibility          | No single Aggregate                |
| Learning Activity    | Domain Concept                   | OPEN / DEFERRED                    |
| Learning Direction   | Business Responsibility          | OPEN / DEFERRED                    |
| Learning Plan        | Learning Direction Concept       | Deferred                           |
| Recommendation       | Learning Direction Concept       | Deferred                           |
| Review Due           | Learning Direction Concept       | Deferred                           |
| Learning Goal        | Learning Direction Concept       | Deferred                           |
| UserProgress         | Not retained as target Aggregate | Confirmed direction                |

---

# 19. Boundaries Intentionally Not Introduced

The following Aggregate Roots are intentionally not introduced:

```text
Assessment
AssessmentAttempt
Competency
CurrentLevel
LearnerState
LearningActivity
LearningDirection
LearningPlan
Recommendation
ReviewDue
LearningGoal
```

The absence of an Aggregate Root does not mean that the concept is not part of the domain.

It means that the current evidence is not sufficient to establish an independent consistency boundary.

---

# 20. Important Boundary Distinctions

The following distinctions are fundamental to the target model.

```text
Course
    ≠
Enrollment
```

```text
Enrollment
    ≠
LessonCompletion
```

```text
Progress
    ≠
Learning Evidence
```

```text
Progress
    ≠
Competency
```

```text
Learning Evidence
    ≠
Learner State
```

```text
Assessment Result
    ≠
Competency
```

```text
Assessment Score
    ≠
Current Level
```

```text
Competency
    ≠
Current Level
```

```text
Current Level
    ∈
Competency
```

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

```text
Quiz
    ≠
QuizAttempt
```

```text
QuizRevision
    ∈
Quiz Aggregate
```

```text
QuizAttempt
    ∈
separate Aggregate
```

```text
LessonItem
    ≠
Generic Learning Activity
```

```text
Historical Evidence
    ≠
Current Learner State
```

---

# 21. Final Target Aggregate Model

The current target Aggregate model is:

```text
Learning Context
│
├── Course Aggregate
│   └── Course
│       └── Section
│           └── Lesson
│               └── LessonItem
│
├── Enrollment Aggregate
│   └── Enrollment
│       └── Progress
│
├── Learning Evidence
│   └── LessonCompletion
│
├── Quiz Aggregate
│   └── Quiz
│       └── QuizRevision
│           └── Question
│               └── Answer
│
├── QuizAttempt Aggregate
│   ├── QuizAttempt
│   ├── UserAnswer(s)
│   └── QuestionResult(s)
│
├── Assessment
│   ├── Component
│   │   └── Task(s)
│   ├── Completion Policy
│   ├── Time Limit
│   ├── Attempt
│   ├── Component Result
│   └── Assessment Result
│       └── Aggregate boundary OPEN
│
├── Learner State
│   └── Competency
│       └── Current Level
│
└── Learning Direction
    └── Aggregate boundaries deferred
```

The model intentionally distinguishes between:

```text
Established Aggregate Boundaries
```

and:

```text
Confirmed Domain Concepts
whose Aggregate boundaries remain OPEN
```

The currently established Aggregate Roots are:

```text
Course
Enrollment
Quiz
QuizAttempt
```

LessonCompletion remains an independent Learning Evidence Entity.

Competency is a confirmed Learner State concept, but its Aggregate boundary is deferred.

Current Level is a state/value belonging to Competency and is not an independent Aggregate.

Assessment is a confirmed broader domain concept, but its Aggregate boundaries remain OPEN until concrete consistency requirements justify a decision.

Learning Activity and Learning Direction remain OPEN / DEFERRED.

These boundaries provide the current domain foundation for subsequent Module Boundary, Application, and implementation decisions without allowing the existing package structure to dictate the target domain model.

````
