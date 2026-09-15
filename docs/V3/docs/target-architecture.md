# Target Architecture

## 1. Purpose

This document defines the target architectural direction for DeutschHub V3.

The target architecture is not intended to replace the existing architecture wholesale.

The current system already provides:

- a Modular Monolith structure;
- Domain / Application / Infrastructure separation;
- Ports & Adapters patterns;
- domain-oriented behavior in several core models.

The purpose of the target architecture is to:

- preserve architectural foundations that already work;
- make established business boundaries more explicit;
- strengthen separation between Domain, Application, and Infrastructure;
- align implementation with the target domain model;
- prevent identified architectural problems from being reproduced;
- provide clear architectural constraints for future development;
- avoid prematurely fixing unresolved domain concepts.

The target architecture therefore represents an **evolution of the existing architecture**, not a complete architectural rewrite.

---

# 2. Architectural Foundation

DeutschHub V3 uses the following architectural combination:

```text
Modular Monolith
        +
Domain-Driven Design
        +
Hexagonal Architecture
        +
Business-oriented organization
````

Each principle has a different responsibility.

### Modular Monolith

The system remains a single deployable application while maintaining explicit internal business boundaries.

### Domain-Driven Design

DDD provides the approach for modeling:

* business concepts;
* Aggregates;
* Entities;
* Value Objects;
* domain behavior;
* domain invariants;
* business boundaries.

### Hexagonal Architecture

Hexagonal Architecture protects the application and domain from infrastructure and external technology concerns.

### Business-oriented organization

Business-oriented organization makes established business responsibilities visible in the internal code organization where doing so improves clarity.

It does not require every business concept to become:

* a module;
* an Aggregate;
* a package;
* or a Bounded Context.

---

# 3. Current Architectural Foundation

The current implementation is organized around:

```text
Domain
Application
Infrastructure
```

and applies Ports & Adapters principles.

The target architecture preserves this foundation.

The intended dependency direction is:

```text
Infrastructure
       ↓
Application
       ↓
Domain
```

with external input entering through appropriate adapters and infrastructure implementing application-defined output ports.

---

## 3.1 Existing Architectural Strengths

Several architectural foundations already work and should be preserved.

### Domain Model

The Learning domain contains behavior-rich domain objects.

Examples include:

```text
Course
Enrollment
Progress
Quiz
QuizAttempt
```

These objects contain domain behavior rather than functioning only as persistence data structures.

### Application Layer

Application services generally orchestrate use cases while delegating domain behavior to domain objects.

### Ports and Adapters

Repository ports are defined as abstractions and implemented by Infrastructure adapters.

Conceptually:

```text
Application Port
       ↑
Infrastructure Adapter
```

### Persistence Isolation

JPA persistence entities are kept in Infrastructure rather than being used directly as Domain entities.

These existing foundations are retained.

---

# 4. Identified Architectural Problems

The target architecture is driven by concrete problems observed in the current implementation.

---

## 4.1 Framework Leakage into the Domain

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/media/service/MediaTypeResolver.java
```

which uses Spring's `@Component`.

This creates a dependency from Domain toward the Spring framework:

```text
Domain
   ↓
Spring
```

This violates the intended architectural dependency rule.

The target architecture therefore requires:

```text
Domain
  ✕ Spring
  ✕ JPA
  ✕ Infrastructure implementation
```

The correction is specifically to remove the Domain layer's dependency on framework-specific concerns.

This is a concrete architectural problem, not a stylistic preference.

---

## 4.2 Business Boundaries Are Not Sufficiently Visible

The current Learning implementation is primarily organized around:

```text
domain/learning/
application/learning/
infrastructure/learning/
```

Domain analysis has established several distinct business responsibilities inside Learning:

```text
Learning
├── Learning Structure
├── Enrollment
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

These are internal business responsibilities.

They do not automatically represent six Bounded Contexts.

The target architecture therefore allows business-oriented organization to become more visible inside Learning as implementation evolves.

However, this should happen only where the established business boundaries justify it.

---

## 4.3 UserProgress Does Not Represent the Target Learner State

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

Its responsibility overlaps with:

```text
Enrollment
└── Progress
```

particularly around Course-scoped progress and learner activity information.

The current `UserProgress` representation is also associated with:

```text
userId
courseId
enrollmentId
```

rather than representing the complete learner-level state.

The target architecture therefore does not use `UserProgress` as the canonical Learner State model.

The target Learner State responsibility is represented through domain concepts such as:

```text
Competency
Current Level
```

while other learner-state concepts remain deferred.

---

## 4.4 Business Rules Must Not Accumulate in Application Services

The current implementation contains application services that coordinate multiple domain concepts.

For example:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

calculates study-time-related information and coordinates lesson completion with enrollment progress.

This is not inherently invalid.

Application services are expected to orchestrate use cases and may coordinate multiple domain boundaries.

However, the target responsibility rule is:

```text
Application
    → orchestrates use cases

Domain
    → owns domain invariants and business rules
```

Therefore, future implementation should avoid allowing Application Services to become the primary location for business rules that belong to established domain concepts.

This does not mean every calculation must be moved into the Domain.

The correct location depends on the actual business meaning and ownership of the rule.

---

# 5. Target Dependency Architecture

The target dependency direction is:

```text
             Infrastructure
             /            \
            ↓              ↓
    Input Adapters     Output Adapters
            ↓              ↑
            ↓              │
        Application ───────┘
            ↓
          Domain
```

The simplified rule is:

```text
Infrastructure
       ↓
Application
       ↓
Domain
```

Dependencies must not point inward from Domain toward Infrastructure.

---

# 6. Domain Layer

The Domain layer contains business concepts and business behavior.

It must remain independent from:

* Spring;
* JPA;
* database implementations;
* HTTP;
* controllers;
* Infrastructure adapters;
* framework-specific infrastructure concerns.

The Domain layer may contain:

```text
Entities
Value Objects
Aggregates
Domain Services
Domain Rules
Domain Exceptions
```

when justified by the domain model.

---

## 6.1 Domain Independence Rule

The intended dependency rule is:

```text
Domain
  ✕ Spring
  ✕ JPA
  ✕ Infrastructure
  ✕ Database
```

If an external framework capability is required, the dependency should be introduced at an outer architectural layer or represented through an appropriate abstraction.

---

# 7. Application Layer

The Application layer defines and executes application use cases.

Its responsibilities include:

* receiving commands or queries;
* loading required domain objects;
* invoking domain behavior;
* coordinating multiple Aggregates;
* coordinating persistence through output ports;
* defining application transaction boundaries;
* returning application-level results.

The Application layer may depend on:

```text
Domain
Application Ports
```

but should not depend on concrete Infrastructure implementations.

---

## 7.1 Application Orchestration

The Application layer answers:

```text
How is this use case executed?
```

The Domain answers:

```text
What must be true for this operation to be valid?
```

For example:

```text
Complete Lesson
      │
      ├── Lesson / Course Structure
      ├── LessonCompletion
      ├── Enrollment
      └── Progress
```

A single use case may therefore coordinate multiple domain boundaries.

This does not imply that those concepts belong to one Aggregate.

---

# 8. Infrastructure Layer

Infrastructure contains technical implementations of external concerns.

Examples include:

* JPA;
* database access;
* repository adapters;
* framework integration;
* HTTP adapters;
* external service integrations.

Infrastructure may depend on:

```text
Application abstractions
Domain abstractions
```

where required to implement adapters.

Infrastructure must not become a dependency of Domain.

---

# 9. Ports and Adapters

The target architecture retains Ports & Adapters.

---

## 9.1 Input Ports

Input Ports represent application use cases.

Conceptually:

```text
External Request
      ↓
Input Adapter
      ↓
Input Port
      ↓
Application Service
```

Controllers and other external adapters should not directly manipulate domain persistence.

---

## 9.2 Output Ports

Output Ports represent external capabilities required by the Application layer.

Examples include:

```text
CourseRepositoryPort
EnrollmentRepositoryPort
LessonCompletionRepositoryPort
```

Infrastructure implements these ports.

Conceptually:

```text
Application
    │
    ↓
Repository Port
    ↑
Repository Adapter
    ↑
JPA / Database
```

The Application layer therefore depends on the abstraction rather than the persistence implementation.

---

# 10. Business Module Architecture

The Learning Context contains six major business responsibilities:

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

These are internal business boundaries.

They are not automatically:

```text
6 Bounded Contexts
6 Aggregates
6 Java packages
6 database schemas
```

The actual technical organization should follow the domain decisions established for each responsibility.

---

# 11. Learning Structure Architecture

Learning Structure represents what can be learned and how learning content is organized.

The established domain boundary is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

The corresponding current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java

src/main/java/com/deutschhub/domain/learning/model/entity/Section.java

src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java

src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The Course Aggregate remains the primary consistency boundary for Course structure.

---

## 11.1 Course Structure

The target architecture preserves:

```text
Course Aggregate
└── Section
    └── Lesson
        └── LessonItem
```

No structural split is introduced without a concrete business consistency requirement.

---

## 11.2 Course Level

Course Level is associated with learning content.

The existing representation:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

provides CEFR classification.

Course Level remains distinct from learner state:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

---

# 12. Enrollment Architecture

Enrollment represents learner participation in a specific Course.

The established boundary is:

```text
Enrollment Aggregate
└── Enrollment
    └── Progress
```

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java

src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

Enrollment remains an independent Aggregate Root.

---

## 12.1 Enrollment Responsibility

Enrollment owns:

* participation lifecycle;
* enrollment state;
* Course-scoped Progress;
* completion state associated with the enrollment.

It does not own:

```text
Course Structure
Learner Competency
Learner Current Level
Complete Learner State
Learning Direction
```

---

# 13. Learning Activities Architecture

Learning Activities represent learner-facing actions or interactions.

Examples include:

```text
Practice
Review
Listening
Speaking
Reading
Writing
Assessment-related activities
```

The current implementation does not establish a complete generic Learning Activity model.

Therefore:

```text
Learning Activities
    → confirmed business responsibility
    → internal domain structure OPEN / DEFERRED
```

---

## 13.1 LessonItem and Learning Activity

The current:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

belongs to the Course Aggregate.

It must not automatically be treated as the generic Learning Activity model.

Therefore:

```text
LessonItem
    ≠
Generic Learning Activity
```

The exact relationship remains open.

---

## 13.2 Learning Activity Boundary

The target architecture does not introduce a generic:

```text
LearningActivity Aggregate
```

without a concrete business requirement.

Activity-specific models may be introduced when their business semantics and consistency requirements justify them.

---

# 14. Assessment Architecture

Assessment is a broader evaluation capability within Learning.

It is broader than Quiz.

An Assessment may contain:

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

A Component is associated with one Skill Dimension.

Possible dimensions include:

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

## 14.1 Assessment and Learning Activities

Assessment is related to Learning Activities but is not automatically identical to them.

The exact relationship between:

```text
Learning Activity
Assessment
Task
Quiz
```

remains open.

The architecture therefore does not assert:

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

---

## 14.2 Assessment and Quiz

Quiz remains an established assessment-related domain concept.

The target distinction is:

```text
Quiz
    = assessment definition

QuizAttempt
    = learner-specific execution
```

Assessment is broader than Quiz.

The exact structural relationship between Assessment and Quiz remains open.

No separate Assessment Bounded Context is introduced at this stage.

---

## 14.3 Assessment Attempt

Each execution of an Assessment by a learner is an Assessment Attempt.

The business rule is:

```text
One User
+
One Assessment
    ↓
At most one active Attempt
```

An Attempt may be resumed across:

* browser sessions;
* devices;
* connection interruptions.

The final Aggregate boundary of Assessment Attempt remains OPEN.

The existing QuizAttempt Aggregate is a concrete precedent, but Quiz-specific rules must not automatically be applied to all Assessment types.

---

## 14.4 Assessment Result

An Assessment Result is the official historical result of an Assessment Attempt.

Conceptually:

```text
Assessment Attempt
        ↓
Component Result(s)
        ↓
Assessment Result
```

The result is historical and must remain associated with the Attempt that produced it.

A later Attempt does not modify an earlier Assessment Result.

---

# 15. Learning Evidence Architecture

Learning Evidence represents observable historical outcomes.

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

These concepts do not automatically belong to one Aggregate.

---

## 15.1 LessonCompletion

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

LessonCompletion is treated as independent Learning Evidence.

It is not an internal entity of Enrollment.

The relationship is:

```text
LessonCompletion
        ↓
Learning Evidence
        ↓
contributes to
        ↓
Enrollment.Progress
```

---

## 15.2 QuizAttempt as Evidence

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

QuizAttempt represents a learner-specific assessment execution and may serve as Learning Evidence.

These are different classifications:

```text
Aggregate Root
    → consistency ownership

Learning Evidence
    → business meaning
```

Therefore:

```text
QuizAttempt
    = Aggregate Root
    = may serve as Learning Evidence
```

---

## 15.3 Evidence and Learner State

Evidence may contribute to Learner State.

Conceptually:

```text
Learning Evidence
        ↓
Domain Interpretation
        ↓
Learner State
```

However, not every Evidence record changes Learner State.

For example:

```text
Ordinary Quiz Result
    ≠
Automatic Current Level update
```

A Current Level update requires the appropriate Level Assessment rule.

---

# 16. Learner State Architecture

Learner State represents the current state that DeutschHub establishes about a learner.

The responsibility includes:

```text
Competency
Current Level
```

Other learner-state concepts remain deferred unless concrete requirements establish them.

---

## 16.1 Learner State Is Not One Aggregate

Learner State is a business responsibility.

It is not automatically one Aggregate Root.

Different learner-state concepts may have different:

* identity;
* lifecycle;
* invariants;
* consistency requirements;
* update mechanisms.

Therefore:

```text
Learner State
    ≠
Single Aggregate
```

---

## 16.2 Competency

Competency represents demonstrated capability within a defined learning domain.

The current scope is represented conceptually by:

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

Competency has the business lifecycle:

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

The Aggregate boundary of Competency remains:

```text
OPEN / DEFERRED
```

---

## 16.3 Current Level

Current Level represents the CEFR proficiency classification established for the learner's Competency.

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

The value object should not be confused with the complete business concept of learner Current Level.

---

## 16.4 Current Level Establishment

A valid passed Level Assessment may establish Current Level.

Sequential progression is not required.

For example:

```text
UNKNOWN
    ↓
B2
```

is valid when supported by the appropriate assessment result.

A lower-level passed Assessment does not decrease an already established higher level.

A failed Assessment does not automatically downgrade Current Level.

---

# 17. Learning Direction Architecture

Learning Direction represents what the learner should do next.

Potential capabilities include:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

The conceptual relationship is:

```text
Learner State
      ↓
Learning Direction
      ↓
Next Learning Activity
```

The exact internal model and Aggregate boundaries remain OPEN / DEFERRED.

No generic:

```text
LearningDirection Aggregate
```

is introduced.

---

# 18. Certification

Certification remains conceptually distinct from Learner State.

The architecture must preserve:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

Certification is not currently established as a separate Bounded Context.

Its exact module and Aggregate boundaries remain open until its business rules are sufficiently understood.

The architecture therefore does not introduce a dedicated Certification context solely because certification concepts exist.

---

# 19. Aggregate and Module Relationship

The architecture explicitly distinguishes:

```text
Module
    ≠
Aggregate
```

The current established Aggregate Roots are:

```text
Course
Enrollment
Quiz
QuizAttempt
```

The current independent Learning Evidence Entity is:

```text
LessonCompletion
```

Other concepts remain open where their Aggregate boundary has not been justified.

Conceptually:

```text
Learning Structure
    └── Course Aggregate

Enrollment
    └── Enrollment Aggregate

Learning Evidence
    ├── LessonCompletion
    └── QuizAttempt
         └── separate Aggregate

Learner State
    └── Competency
         └── Current Level
              └── no independent Aggregate
```

---

# 20. Aggregate and Module Rules

The following rules apply to future implementation.

## Rule 1 — Do not create an Aggregate merely because a concept exists

A domain concept becomes an Aggregate only when its:

* identity;
* lifecycle;
* invariants;
* ownership;
* and consistency requirements

justify an independent boundary.

---

## Rule 2 — Do not merge Aggregates merely because they are related

For example:

```text
LessonCompletion
        ↓
Enrollment.Progress
```

does not imply:

```text
Enrollment
└── LessonCompletion
```

---

## Rule 3 — Do not split Aggregates without a concrete consistency problem

Existing established boundaries should remain stable unless a concrete business or consistency requirement requires change.

---

## Rule 4 — Application Transactions May Cross Aggregates

A single use case may coordinate:

```text
Course
Enrollment
LessonCompletion
Assessment
Learner State
```

without requiring those concepts to become one Aggregate.

---

# 21. Cross-Context Architecture

The system contains broader contexts such as:

```text
Identity
Learning
Content
Media
```

At a high level:

```text
Identity
    = who the user is

Learning
    = how the user learns

Content
    = learning resources

Media
    = media resources
```

Learning may use identity information such as a User identifier.

It should not become responsible for Identity management.

Similarly, Learning may consume Content or Media capabilities without owning those contexts' responsibilities.

---

## 21.1 Cross-Context Dependency

Cross-context dependencies should use appropriate abstractions.

The Learning Domain should not directly depend on:

```text
Identity Infrastructure
Content Infrastructure
Media Infrastructure
```

Concrete integrations belong outside the Domain.

---

# 22. Business-Oriented Code Organization

The target architecture allows business responsibilities to become visible in the code organization as implementation evolves.

The current structure is broadly:

```text
domain/learning/
application/learning/
infrastructure/learning/
```

The target direction is to make established responsibilities increasingly visible without prematurely forcing every responsibility into a separate technical module.

Conceptually:

```text
Learning
├── Learning Structure
├── Enrollment
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

This is an organizational direction, not a mandatory package tree.

---

## 22.1 Avoid Mechanical Package Mapping

The following should not be assumed:

```text
One Concept
    =
One Package
```

or:

```text
One Module
    =
One Aggregate
```

or:

```text
One Business Responsibility
    =
One Bounded Context
```

Technical organization should follow the established domain model and concrete implementation needs.

---

# 23. Domain State, Progress, and Evidence

The architecture explicitly separates:

```text
Evidence
Progress
Learner State
```

They answer different business questions.

### Evidence

```text
What happened?
```

Examples:

```text
LessonCompletion
QuizAttempt
AssessmentResult
```

### Progress

```text
How far has the learner advanced through a specific learning structure?
```

Example:

```text
Enrollment.Progress
```

### Learner State

```text
What does DeutschHub currently know about this learner?
```

Examples:

```text
Competency
Current Level
```

Therefore:

```text
Evidence
    ≠
Progress
```

```text
Progress
    ≠
Learner State
```

```text
Evidence
    ≠
Learner State
```

---

# 24. Assessment, Evidence, and Learner State Flow

The target architecture supports the following conceptual flow:

```text
Learning Activity
        ↓
Assessment
        ↓
Assessment Attempt
        ↓
Component Result(s)
        ↓
Assessment Result
        ↓
Learning Evidence
        ↓
Learner State
```

For a Level Assessment:

```text
Level Assessment
        ↓
PASSED Assessment Result
        ↓
Competency
        ↓
Current Level
```

This flow does not mean that every Assessment changes Learner State.

Only the appropriate business rules determine whether an Assessment Result can establish or update learner state.

---

# 25. Learning Flow

The broader Learning loop is:

```text
Learning Structure
        ↓
Learning Activity
        ↓
Learning Evidence
        ↓
Learner State
        ↓
Learning Direction
        ↓
Next Learning Activity
```

This is a business model rather than a strict technical dependency graph.

Not every use case must pass through every responsibility.

---

# 26. Architectural Constraints

The following constraints apply to future development.

### Constraint 1 — Domain independence

The Domain must remain independent of framework and infrastructure technologies.

### Constraint 2 — Dependency inversion

Application and Domain code must not depend on concrete Infrastructure implementations.

### Constraint 3 — Preserve established Aggregates

Existing Aggregate boundaries should not be changed without concrete justification.

### Constraint 4 — Business rules belong to the appropriate domain concept

Application Services should orchestrate rather than becoming the default location for business rules.

### Constraint 5 — Do not over-model unresolved concepts

OPEN domain decisions should remain open until concrete requirements justify implementation.

### Constraint 6 — Do not introduce architecture for hypothetical problems

Events, new Bounded Contexts, generic abstractions, additional layers, or new modules should only be introduced when a concrete problem or requirement justifies them.

### Constraint 7 — Current code is evidence, not absolute target truth

Existing V3 implementation may contain legacy or conceptually outdated structures.

The target architecture should follow established domain decisions where they intentionally differ from the current implementation.

### Constraint 8 — Do not rewrite working code without a concrete reason

Existing working architecture should be preserved unless:

* a documented architectural problem exists;
* a target domain decision requires a change;
* a new feature exposes a concrete limitation;
* or an established dependency rule is violated.

---

# 27. Implementation Evolution

The target architecture should be introduced incrementally.

The intended evolution is:

```text
Current Working Architecture
        ↓
Fix Concrete Architectural Problems
        ↓
Align New Implementation with Target Domain Boundaries
        ↓
Gradually Increase Business Boundary Visibility
        ↓
Preserve Stable Existing Foundations
```

This is not:

```text
Current Code
        ↓
Complete Rewrite
        ↓
New Architecture
```

The goal is controlled evolution.

---

# 28. Concrete Architectural Corrections

The current evidence establishes several concrete areas for correction or prevention.

## 28.1 MediaTypeResolver

Current location:

```text
src/main/java/com/deutschhub/domain/media/service/MediaTypeResolver.java
```

Problem:

```text
Domain
    ↓
Spring @Component
```

Target rule:

```text
Domain
    ✕
Spring dependency
```

The future implementation should preserve framework independence in the Domain.

---

## 28.2 UserProgress

Current location:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

Problem:

```text
UserProgress
        ↕
Enrollment.Progress
```

overlapping Course-scoped progress responsibilities.

Target direction:

```text
Enrollment
└── Progress
    → Course-scoped progress

Learner State
└── Competency
    └── Current Level
```

`UserProgress` should not become the foundation for the target Learner State model.

---

## 28.3 Application-Level Business Logic

Current example:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

The service coordinates multiple responsibilities.

This is acceptable as orchestration.

The architectural constraint is that domain invariants should not gradually accumulate in Application Services when an established Domain concept is responsible for them.

---

# 29. Target Architecture Summary

DeutschHub V3 retains:

```text
Modular Monolith
+
DDD
+
Hexagonal Architecture
```

The system remains a single deployable application with explicit internal business boundaries.

The Learning Context contains six major responsibilities:

```text
Learning Structure
Enrollment
Learning Activities
Learning Evidence
Learner State
Learning Direction
```

Assessment is a broader Learning capability that interacts with:

```text
Learning Activities
Learning Evidence
Learner State
```

The established Aggregate boundaries remain:

```text
Course
Enrollment
Quiz
QuizAttempt
```

with:

```text
LessonCompletion
    → independent Learning Evidence Entity
```

Competency is a confirmed Learner State concept whose Aggregate boundary remains deferred.

Current Level is a state/value belonging to Competency and is not an independent Aggregate.

The target architecture therefore preserves the current architectural foundation while making the domain model more explicit.

The central principle is:

> **Evolve the existing architecture according to concrete business and architectural problems; do not introduce structural change without justification.**

The target architecture is therefore a guide for controlled implementation evolution rather than a mandate for a wholesale rewrite.

## Application-Orchestrated Cross-Aggregate Actions

The following responsibilities have been intentionally placed at the Application layer
because they require coordination or validation across multiple Aggregates.

| Action / Responsibility | Application Layer | Domain Aggregate | Reason |
|---|---|---|---|
| Load QuizRevision for QuizAttempt | Yes | — | QuizAttempt must not access another Aggregate directly. |
| Resolve CompletionPolicy for QuizAttempt submission | Yes | — | CompletionPolicy belongs to QuizRevision. Application coordinates QuizRevision and QuizAttempt. |
| Validate that a Question belongs to the Attempt's Revision before answering | Yes | — | Question belongs to QuizRevision; QuizAttempt only keeps revisionId. |
| Load Published QuizRevision before evaluating an Attempt | Yes | — | QuizAttempt must not load QuizRevision or access its repository. |
| Provide Questions and CompletionPolicy to QuizAttempt.submit(...) | Yes | QuizAttempt | Application supplies data from the appropriate Aggregate; QuizAttempt performs its own business decision. |

### Domain Responsibility

Application orchestration does not replace domain business rules.

`QuizAttempt` remains responsible for:
- validating its own lifecycle and authorization;
- accepting or rejecting submission according to the provided `CompletionPolicy`;
- creating `QuestionResult` instances;
- calculating `totalScore`;
- transitioning its status to `SUBMITTED`.

`QuizRevision` remains responsible for:
- owning Questions;
- owning `CompletionPolicy`;
- maintaining Revision-specific assessment rules.

### Boundary Rule

`QuizAttempt` must not directly load, query, or navigate into `Quiz`,
`QuizRevision`, or their repositories.

Cross-Aggregate coordination is performed by the Application layer.