# Target Module Boundaries

## 1. Purpose

This document defines the target business module boundaries for the Learning Context in DeutschHub V3 within the Modular Monolith.

The purpose of this document is to identify coherent business responsibilities and establish the boundaries between them.

The module boundaries are derived from:

- the domain analysis;
- established domain decisions;
- the target domain model;
- the target Aggregate boundaries;
- and the current Learning implementation where it provides relevant evidence.

This document defines business responsibility boundaries.

It does not define:

- final Java package structure;
- database boundaries;
- API boundaries;
- Aggregate implementations;
- deployment boundaries;
- infrastructure structure;
- or every future domain object.

The target module model should guide implementation without allowing the current technical structure to dictate the target business model.

---

# 2. Module Boundary Principles

## 2.1 Business Responsibility Defines a Module

A module represents a coherent business responsibility.

A module should not exist merely because a set of classes:

- shares a package;
- uses the same database tables;
- belongs to the same technical layer;
- or has similar implementation details.

For example:

```text
Course
Section
Lesson
LessonItem
````

belong to the Learning Structure responsibility because together they represent the organization of learning content.

---

## 2.2 Module Does Not Equal Aggregate

A module may contain multiple Aggregates.

For example:

```text
Learning Evidence
├── LessonCompletion
└── Assessment Evidence
```

does not imply that all Evidence concepts belong to one Aggregate.

Similarly:

```text
Learning Structure
└── Course Aggregate
```

does not mean that the module and Aggregate are the same boundary.

Therefore:

```text
Module
    ≠
Aggregate
```

---

## 2.3 Module Does Not Equal Bounded Context

The target modules are internal business boundaries within the broader Learning Context.

They do not automatically represent separate Bounded Contexts.

The current target is:

```text
Learning Context
    ├── Learning Structure
    ├── Enrollment
    ├── Learning Activities
    ├── Learning Evidence
    ├── Learner State
    └── Learning Direction
```

A module should become a separate Bounded Context only when its:

* domain language;
* ownership;
* business responsibility;
* model;
* and integration requirements

justify that separation.

No such decision is made here.

---

## 2.4 Module Does Not Equal Database Structure

Database tables and relationships do not determine module boundaries automatically.

For example:

```text
lesson_completions
```

does not automatically imply:

```text
LessonCompletion Module
```

Likewise:

```text
Enrollment → Course
```

does not imply that Course and Enrollment must belong to the same module.

Business responsibility remains the primary criterion.

---

## 2.5 Module Does Not Require One Aggregate

A module may contain:

* one Aggregate;
* multiple Aggregates;
* independent Entities;
* Value Objects;
* domain services;
* domain concepts whose final Aggregate boundary is still OPEN.

Therefore:

```text
One Module
    ≠
One Aggregate
```

---

# 3. High-Level Target Structure

The Learning Context is organized around six major business responsibilities:

```text
Learning
│
├── Learning Structure
├── Enrollment
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

These six responsibilities form the target internal business boundary model of Learning.

They are not automatically six Bounded Contexts.

They are also not required to map one-to-one to Java modules.

---

# 4. Learning Structure Module

## 4.1 Responsibility

Learning Structure is responsible for defining what can be learned and how learning content is organized.

The established structure is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

The module therefore owns the business responsibility for:

* Courses;
* Sections;
* Lessons;
* Lesson Items;
* organization of learning content;
* content-level classification associated with learning structures.

---

## 4.2 Current Domain Foundation

The current implementation provides:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java

src/main/java/com/deutschhub/domain/learning/model/entity/Section.java

src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java

src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The established Aggregate boundary is:

```text
Course Aggregate
└── Course
    └── Section
        └── Lesson
            └── LessonItem
```

---

## 4.3 Responsibility Boundary

Learning Structure answers:

```text
What can be learned?
How is it organized?
```

It does not own:

```text
Who is participating?
What did the learner do?
What is the learner's current capability?
What should the learner do next?
```

Those responsibilities belong to other modules.

---

## 4.4 Course Level

Course Level belongs to Learning Structure because it describes the level associated with the learning content.

The existing representation is:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

However:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

Learning Structure must not become responsible for maintaining learner Current Level.

---

## 4.5 Module Boundary

The Learning Structure module contains the Course-related domain model.

The module boundary is:

```text
Learning Structure
└── Course Aggregate
    ├── Section
    ├── Lesson
    └── LessonItem
```

Additional future learning structures may be added when concrete business requirements require them.

They do not automatically require new modules.

---

# 5. Enrollment Module

## 5.1 Responsibility

Enrollment is responsible for representing learner participation in a specific learning structure.

For the current product, this primarily means Course enrollment.

The module owns:

* enrollment lifecycle;
* participation status;
* Course-scoped Progress;
* enrollment completion state.

---

## 5.2 Current Domain Foundation

The current Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

Progress is:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

The established Aggregate boundary is:

```text
Enrollment Aggregate
└── Enrollment
    └── Progress
```

---

## 5.3 Responsibility Boundary

Enrollment answers:

```text
Who is participating in what?
What is this learner's progress within that enrollment?
```

It does not own:

```text
Course structure
Learner Competency
Learner Current Level
Global learner history
Learning Direction
```

---

## 5.4 Relationship with Learning Structure

The conceptual relationship is:

```text
Learning Structure
        ↓
     Course
        ↑
        │
    Enrollment
```

Course and Enrollment remain separate business responsibilities.

Therefore:

```text
Learning Structure
    ≠
Enrollment
```

even though Enrollment refers to a Course.

---

## 5.5 Progress Boundary

Course-scoped Progress belongs to Enrollment.

Therefore:

```text
Enrollment
└── Progress
```

Progress must not be treated as the complete state of the learner.

```text
Enrollment.Progress
    ≠
Learner State
```

---

# 6. Learning Activities Module

## 6.1 Responsibility

Learning Activities represent learner-facing actions or interactions through which learning takes place.

Examples include:

* Practice;
* Review;
* Listening;
* Speaking;
* Reading;
* Writing;
* Assessment-related activities.

The module answers:

```text
What does the learner do?
```

---

## 6.2 Relationship with Learning Structure

Learning Structure defines what can be learned.

Learning Activity represents what the learner does.

Conceptually:

```text
Learning Structure
        ↓
Learning Activity
```

However, this relationship does not imply that Learning Activity is owned by the Course Aggregate.

---

## 6.3 LessonItem Is Not Automatically Learning Activity

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

LessonItem is part of the Course Aggregate.

It should not automatically be interpreted as the generic representation of Learning Activity.

Therefore:

```text
LessonItem
    ≠
Generic Learning Activity
```

The exact relationship remains open.

---

## 6.4 Assessment Relationship

Assessment is a broader domain concept used to structure evaluation.

An Assessment may involve multiple Components and Tasks.

Conceptually:

```text
Learning Activity
        ↓
Assessment-related Activity
        ↓
Assessment
```

However, the exact relationship between:

```text
Learning Activity
Assessment
Quiz
Task
```

is not fully established.

No generic Activity Aggregate is introduced at this stage.

---

## 6.5 Module Boundary

**Status: Confirmed as Business Responsibility / Internal Structure OPEN**

The Learning Activities module exists as a target responsibility.

However, its internal Aggregate structure remains OPEN / DEFERRED.

No generic:

```text
LearningActivity Aggregate
```

is introduced merely to unify different activity types.

---

# 7. Learning Evidence Module

## 7.1 Responsibility

Learning Evidence represents observable historical facts produced by learner activities and assessment interactions.

The module answers:

```text
What happened?
```

Examples include:

```text
LessonCompletion
QuizAttempt
QuestionResult
ComponentResult
AssessmentResult
```

These concepts do not necessarily share one Aggregate.

---

## 7.2 LessonCompletion

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

LessonCompletion represents evidence that a learner completed a lesson.

The target classification is:

```text
LessonCompletion
    → Learning Evidence
    → Independent Evidence Entity
```

It is not an internal entity of Enrollment.

---

## 7.3 LessonCompletion and Progress

The conceptual flow is:

```text
LessonCompletion
        ↓
Learning Evidence
        ↓
contributes to
        ↓
Enrollment.Progress
```

This does not imply:

```text
Enrollment
└── LessonCompletion
```

The two concepts remain in different business boundaries.

---

## 7.4 Assessment Evidence

Assessment produces historical information through an Assessment Attempt.

The conceptual structure is:

```text
Assessment
    ↓
Assessment Attempt
    ├── Component Result(s)
    └── Assessment Result
```

For the existing Quiz model:

```text
Quiz
    ↓
QuizAttempt
    ├── UserAnswer(s)
    └── QuestionResult(s)
```

QuizAttempt may serve as Learning Evidence.

---

## 7.5 Historical Nature of Evidence

Evidence represents historical facts.

Examples:

```text
LessonCompletion
QuestionResult
ComponentResult
AssessmentResult
```

should preserve what happened at the relevant point in time.

Therefore:

```text
Historical Evidence
    ≠
Current Learner State
```

A later assessment or learning event should not rewrite the historical meaning of an earlier event.

---

## 7.6 Evidence and Learner State

Evidence may contribute to Learner State.

Conceptually:

```text
Learning Evidence
        ↓
Interpretation / Domain Rule
        ↓
Learner State
```

However, not every Evidence record automatically changes Learner State.

For example:

```text
Ordinary Quiz Result
    ≠
Automatic Current Level update
```

A Current Level update requires the appropriate Level Assessment rule.

---

## 7.7 Module Boundary

**Status: Confirmed Business Responsibility**

Learning Evidence is an established business responsibility.

Its concepts may have different Aggregate boundaries.

The module therefore does not require:

```text
LearningEvidence Aggregate
```

as a single Aggregate Root.

---

# 8. Assessment Capability

## 8.1 Responsibility

Assessment is a structured evaluation capability within the Learning Context.

Assessment is broader than Quiz.

It may evaluate one or more Skill Dimensions, including:

```text
Listening
Speaking
Reading
Writing
Grammar
Vocabulary
```

An Assessment does not have to evaluate all dimensions.

---

## 8.2 Assessment Structure

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

A Component belongs to one Skill Dimension.

A Component may contain multiple Tasks.

---

## 8.3 Assessment and Learning Activities

Assessment is related to Learning Activities but is not automatically identical to them.

The exact relationship remains open:

```text
Learning Activity
        ↕
Assessment
        ↕
Quiz
```

The current target model does not assert:

```text
Assessment
    =
Learning Activity
```

or:

```text
Assessment
    =
Quiz
```

or:

```text
Quiz
    =
Assessment Task
```

---

## 8.4 Assessment and Evidence

Assessment execution can produce historical evidence.

Conceptually:

```text
Assessment
    ↓
Assessment Attempt
    ↓
Component Result(s)
    ↓
Assessment Result
    ↓
Learning Evidence
```

The Assessment capability therefore interacts strongly with the Learning Evidence module.

---

## 8.5 Assessment and Learner State

A Level Assessment may establish or update Competency and Current Level when its required result is valid.

Conceptually:

```text
Level Assessment
        ↓
Passed Assessment Result
        ↓
Competency
        ↓
Current Level
```

This relationship does not apply automatically to every Assessment.

Ordinary practice, Course Quiz, or external quiz may support learning without directly establishing Current Level.

---

## 8.6 Module Placement

Assessment remains within the Learning Context.

It is not currently established as a separate Bounded Context.

Its final module placement relative to:

```text
Learning Activities
Learning Evidence
Quiz
Learner State
```

is an internal module design decision that must remain consistent with the established business responsibilities.

---

# 9. Learner State Module

## 9.1 Responsibility

Learner State represents the current state that DeutschHub establishes about a learner.

The module answers:

```text
What do we currently know about this learner?
```

The responsibility includes learner-centered concepts such as:

```text
Competency
Current Level
```

Other concepts may be added later when concrete requirements justify them.

---

## 9.2 Learner State Is Not One Aggregate

Learner State is a business responsibility.

It is not automatically one Aggregate Root.

Different learner-state concepts may have different:

* identities;
* lifecycles;
* invariants;
* update rules;
* consistency requirements.

Therefore:

```text
Learner State
    ≠
Single Aggregate
```

---

## 9.3 Competency

Competency represents demonstrated capability within a defined learning domain.

The current target identity is:

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

Competency has a lifecycle:

```text
UNASSESSED
    ↓
ASSESSED
```

with:

```text
Current Level = UNKNOWN
```

before a valid Level Assessment establishes a level.

---

## 9.4 Competency Boundary

Competency is a confirmed Learner State domain concept.

However:

```text
Competency
    → Aggregate boundary OPEN / DEFERRED
```

No independent Competency Aggregate is introduced until its consistency requirements justify one.

---

## 9.5 Current Level

Current Level represents the CEFR proficiency classification established for a learner's Competency.

Conceptually:

```text
Competency
    └── Current Level
```

Current Level has no independent identity.

Therefore:

```text
Current Level
    ≠
Independent Aggregate
```

and:

```text
Current Level
    ≠
Independent Entity
```

The existing:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

provides the CEFR classification baseline:

```text
A1
A2
B1
B2
C1
C2
```

---

## 9.6 Current Level Rules

A valid passed Level Assessment may establish Current Level.

The learner does not need to progress sequentially through every CEFR level.

For example:

```text
UNKNOWN
    ↓
B2
```

is valid when supported by a valid passed Level Assessment.

A lower-level passed Assessment does not reduce an established higher level.

A failed Assessment does not automatically downgrade Current Level.

---

## 9.7 Learner State and Enrollment

Learner State is broader than Course-scoped Enrollment Progress.

```text
Enrollment
    = participation in a specific Course

Learner State
    = learner-level state across learning experiences
```

Therefore:

```text
Enrollment.Progress
    ≠
Complete Learner State
```

---

# 10. Learning Direction Module

## 10.1 Responsibility

Learning Direction represents what the learner should do next.

The module answers:

```text
What should this learner do next?
```

Potential concepts include:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

---

## 10.2 Relationship with Learner State

Learning Direction uses information about Learner State.

Conceptually:

```text
Learner State
        ↓
Learning Direction
        ↓
Next Learning Activity
```

Learning Direction does not own Learner State.

---

## 10.3 Boundary Status

**Status: Confirmed Business Responsibility / Internal Structure OPEN**

The responsibility is recognized in the target Learning model.

However, the exact internal Aggregates and persisted concepts remain OPEN / DEFERRED.

No generic:

```text
LearningDirection Aggregate
```

is introduced.

---

# 11. UserProgress

## 11.1 Current Implementation

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

The current model overlaps with Course-scoped Progress represented by:

```text
Enrollment
└── Progress
```

---

## 11.2 Target Decision

`UserProgress` is not retained as the canonical representation of Learner State.

The target responsibilities are:

```text
Enrollment
└── Progress
    → Course-scoped progress

LessonCompletion
    → Learning Evidence

QuizAttempt
    → Assessment execution
    → may serve as Learning Evidence

Competency
    → Demonstrated capability

Current Level
    → Established CEFR proficiency state
```

Therefore:

```text
UserProgress
    ≠
Target Learner State
```

---

# 12. Module Interaction Model

The target business flow is:

```text
Learning Structure
        │
        ↓
    Enrollment
        │
        ↓
Learning Activities
        │
        ↓
 Learning Evidence
        │
        ↓
   Learner State
        │
        ↓
 Learning Direction
        │
        ↓
Next Learning Activity
```

This represents a business relationship rather than a strict technical dependency graph.

Not every flow must pass through every module.

For example:

```text
Learning Structure
    → Course

Enrollment
    → Course participation

Lesson Activity
    → LessonCompletion

Assessment
    → Assessment Result

Level Assessment
    → Competency
    → Current Level
```

---

# 13. Module Interaction Rules

## 13.1 Learning Structure Does Not Own Learner State

Learning Structure owns the organization of learning content.

It does not own:

```text
Competency
Current Level
XP
Streak
```

---

## 13.2 Enrollment Does Not Own Global Learner State

Enrollment owns Course participation and Course-scoped Progress.

It does not become the owner of the learner's complete state.

---

## 13.3 Learning Evidence Does Not Automatically Own Learner State

Evidence records historical facts.

Learner State represents the current state established by the system.

Therefore:

```text
Evidence
    ≠
Learner State
```

---

## 13.4 Assessment Does Not Automatically Establish Competency

Only the appropriate Level Assessment rules may establish or update Competency and Current Level.

Therefore:

```text
Quiz Result
    ≠
Automatic Current Level
```

---

## 13.5 Learning Direction Does Not Own Learner State

Learning Direction consumes learner information to determine or recommend next actions.

It does not become the canonical source of Learner State.

---

# 14. Module and Aggregate Relationship

The target relationship between Modules and Aggregates is:

```text
Learning Structure
    └── Course Aggregate

Enrollment
    └── Enrollment Aggregate

Learning Activities
    └── Aggregate boundaries OPEN

Learning Evidence
    ├── LessonCompletion
    └── Assessment Evidence
        └── Aggregate boundaries depend on source concept

Learner State
    ├── Competency
    │    └── Aggregate boundary OPEN / DEFERRED
    └── Current Level
         └── state/value of Competency

Learning Direction
    └── Aggregate boundaries OPEN / DEFERRED
```

The existing Quiz model remains:

```text
Quiz Module Responsibility
    └── Quiz Aggregate
        └── QuizRevision
            └── Question
                └── Answer
```

with:

```text
QuizAttempt
    → separate Aggregate Root
    → may serve as Learning Evidence
```

Assessment is broader than Quiz and does not automatically inherit the Quiz Aggregate boundary.

---

# 15. Target Module Classification

| Business Responsibility | Main Concepts                                                                    | Aggregate Status                                            | Module Status            |
| ----------------------- | -------------------------------------------------------------------------------- | ----------------------------------------------------------- | ------------------------ |
| Learning Structure      | Course, Section, Lesson, LessonItem                                              | Course Aggregate confirmed                                  | Confirmed                |
| Enrollment              | Enrollment, Progress                                                             | Enrollment Aggregate confirmed                              | Confirmed                |
| Learning Activities     | Practice, Review, Activity-related concepts                                      | OPEN / DEFERRED                                             | Confirmed responsibility |
| Learning Evidence       | LessonCompletion, QuizAttempt, QuestionResult, ComponentResult, AssessmentResult | Multiple boundaries                                         | Confirmed responsibility |
| Assessment              | Assessment, Component, Task, Attempt, Result                                     | OPEN                                                        | Confirmed capability     |
| Learner State           | Competency, Current Level                                                        | Competency boundary deferred; Current Level not independent | Confirmed responsibility |
| Learning Direction      | Learning Plan, Recommendation, Review Due, Goal, Exam Preparation                | OPEN / DEFERRED                                             | Confirmed responsibility |

Assessment is shown separately in the table because it is a major domain capability, but this does not imply that it must become a seventh top-level module.

Its final placement inside the Learning module model remains an implementation-level decision constrained by the business boundaries defined here.

---

# 16. Confirmed Module Boundaries

The following business responsibilities are confirmed:

```text
Learning Structure
Enrollment
Learning Activities
Learning Evidence
Learner State
Learning Direction
```

The following domain capability is also confirmed:

```text
Assessment
```

However, Assessment is currently treated as part of the broader Learning domain rather than as an independently established Bounded Context.

The exact internal module placement of Assessment remains OPEN.

---

# 17. Open / Deferred Module Decisions

The following decisions remain intentionally unresolved.

## 17.1 Learning Activities

Open questions include:

* generic activity model versus activity-specific models;
* activity lifecycle;
* activity persistence;
* relationship between LessonItem and Activity;
* relationship between Activity and Assessment.

---

## 17.2 Assessment

Open questions include:

* exact module placement;
* Assessment Revision model;
* Evaluation Mechanism model;
* Assessment Attempt Aggregate boundary;
* Component Result representation;
* Assessment Result representation;
* structural relationship between Assessment and Quiz.

The business rules already established for Assessment must be preserved when these technical boundaries are decided.

---

## 17.3 Competency

The business meaning, scope, lifecycle, and core invariants are established.

The remaining question is:

```text
What is the final Aggregate boundary of Competency?
```

This is deferred until concrete consistency requirements require the decision.

---

## 17.4 Learner State

The following remain deferred:

```text
Vocabulary State
Grammar State
Skill State
XP
Streak
Achievement
Statistics
```

Their existence does not automatically imply separate modules or Aggregates.

---

## 17.5 Learning Direction

The following remain deferred:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

Their final module and Aggregate boundaries should be decided individually when concrete requirements emerge.

---

# 18. Implementation Principles

The target module model should be introduced incrementally.

Existing code should not be reorganized merely to make the package tree visually match this document.

Changes should be made when there is a concrete reason, such as:

* an existing component crosses a documented business boundary;
* an existing model represents an outdated domain concept;
* two responsibilities have become concretely coupled in a way that violates their intended boundary;
* a new feature requires a boundary already supported by the domain model;
* or an architectural dependency violates established rules.

No module should be created solely because a domain concept exists.

No Aggregate should be created solely because a module exists.

No Bounded Context should be created solely because a module exists.

---

# 19. Final Target Module Model

The current target Learning module model is:

```text
Learning Context
│
├── Learning Structure
│   └── Course
│       └── Section
│           └── Lesson
│               └── LessonItem
│
├── Enrollment
│   └── Enrollment
│       └── Progress
│
├── Learning Activities
│   └── Internal structure deferred
│
├── Learning Evidence
│   ├── LessonCompletion
│   └── Assessment Evidence
│       ├── QuizAttempt
│       ├── QuestionResult
│       ├── ComponentResult
│       └── AssessmentResult
│
├── Learner State
│   └── Competency
│       └── Current Level
│
└── Learning Direction
    └── Internal structure deferred
```

Assessment operates across the relevant responsibilities:

```text
Assessment
├── Component
│   └── Task(s)
├── Completion Policy
├── Time Limit
├── Attempt
└── Result
```

with important relationships to:

```text
Learning Activities
        ↓
Assessment
        ↓
Learning Evidence
        ↓
Learner State
```

The exact internal Aggregate boundaries of Assessment remain OPEN.

---

# 20. Final Boundary Principle

The target module model follows this hierarchy:

```text
Business Responsibility
        ↓
Module Boundary
        ↓
Aggregate Boundary
        ↓
Entity / Value Object
```

but the boundaries must not be inferred mechanically from one another.

In particular:

```text
Module
    ≠
Aggregate

Aggregate
    ≠
Bounded Context

Domain Concept
    ≠
Module

Database Table
    ≠
Module
```

The target module boundaries exist to keep business responsibilities coherent while allowing Aggregate boundaries and technical implementation structures to evolve according to concrete domain requirements.

The current target therefore establishes:

```text
Learning Structure
Enrollment
Learning Activities
Learning Evidence
Learner State
Learning Direction
```

as the six major internal business responsibilities of the Learning Context.

Assessment is a confirmed broader Learning capability that interacts with Learning Activities, Learning Evidence, and Learner State, but its final internal module and Aggregate placement remains OPEN.

Competency is a confirmed Learner State concept with its Aggregate boundary deferred.

Current Level is a state/value belonging to Competency and is not an independent module or Aggregate.

This provides the current module boundary foundation for the next architectural decisions without prematurely restructuring the codebase.
