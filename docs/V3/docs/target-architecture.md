# Target Architecture

## 1. Purpose

This document defines the target architectural direction for DeutschHub V3.

The target architecture is not intended to replace the existing architecture wholesale. The current system already contains a working modular-monolith structure, Domain/Application/Infrastructure separation, and Ports & Adapters patterns.

The purpose of the target architecture is to:

- preserve architectural foundations that already work;
- make business boundaries more explicit;
- strengthen the separation between Domain, Application, and Infrastructure;
- align the code organization with the business model established through domain analysis;
- prevent identified architectural problems from being reproduced during future implementation;
- provide architectural constraints for future development without prematurely deciding unresolved domain concepts.

The target architecture therefore represents an **evolution of the existing architecture**, rather than a complete architectural rewrite.

---

## 2. Current Architectural Foundation

The current DeutschHub V3 implementation is a modular monolith organized around three primary layers:

```text
Domain
Application
Infrastructure
````

The system also applies Ports & Adapters principles.

At the application boundary, input ports represent use cases and output ports represent required external capabilities such as persistence.

Infrastructure implements those ports through concrete adapters.

The resulting dependency direction is:

```text
Infrastructure / Interface Adapters
                ↓
           Application
                ↓
             Domain
```

The target architecture retains this fundamental direction.

### 2.1 Existing Architectural Strengths

The current implementation already provides several sound architectural foundations.

#### Domain model

The Learning domain contains behavior-rich domain objects rather than being purely data-oriented.

For example:

* `Course` owns course-structure behavior and publication rules.
* `Enrollment` owns enrollment lifecycle and course-scoped progress.
* `Progress` represents progress as a value object.
* `Quiz` owns assessment-definition behavior.
* `QuizAttempt` owns attempt lifecycle and answer behavior.

#### Application layer

Application services generally orchestrate use cases rather than directly implementing all domain behavior.

For example, course publication is orchestrated by the application service while the publication rules are enforced by `Course`.

#### Ports and Adapters

Repository ports are defined at the application boundary and implemented by infrastructure adapters.

For example:

```text
CourseRepositoryPort
        ↑
JpaCourseRepositoryAdapter
```

The same pattern is used for enrollment and lesson-completion persistence.

#### Persistence isolation

JPA persistence entities are kept in Infrastructure rather than being used as Domain entities.

This keeps persistence concerns separate from the domain model.

These foundations should be preserved.

---

# 3. Identified Architectural Problems

The target architecture is driven by concrete observations from the current implementation.

## 3.1 Framework Leakage into the Domain

The current implementation contains a Domain component that directly depends on Spring.

The file:

```text
src/main/java/com/deutschhub/domain/media/service/MediaTypeResolver.java
```

uses Spring's `@Component`.

This creates the following dependency:

```text
Domain
  ↓
Spring Framework
```

The target architecture requires the Domain layer to remain independent of framework-specific infrastructure concerns.

The intended dependency rule is:

```text
Domain
  ✕ Spring
  ✕ JPA
  ✕ Infrastructure implementation
```

Framework dependencies belong outside the Domain.

This is a concrete architectural correction rather than a stylistic preference.

---

## 3.2 Business Boundaries Are Not Sufficiently Visible

The current Learning implementation is organized primarily around the Learning context and architectural layers:

```text
domain/learning/
application/learning/
infrastructure/learning/
```

However, domain analysis shows that Learning contains several distinct business responsibilities:

```text
Learning
├── Learning Structure
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

These responsibilities are not automatically separate Bounded Contexts.

They represent business responsibility groups that should become increasingly visible in the internal organization of the Learning module as the domain evolves.

The target architecture therefore favors **business-oriented organization inside the modular monolith**, while retaining Hexagonal dependency rules.

Business-oriented organization is an organizational principle, not a replacement for Hexagonal Architecture.

---

## 3.3 UserProgress Does Not Represent the Target Learner State

The current:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

contains state that overlaps substantially with:

```text
Enrollment
Progress
```

For example, both models represent course-scoped progress and study activity information.

The current `UserProgress` model is also scoped by:

```text
userId
courseId
enrollmentId
```

rather than representing a complete learner-level state.

The target architecture therefore does not treat `UserProgress` as the target representation of Learner State.

Learner State remains a broader business responsibility whose internal domain model is not yet fully decided.

---

## 3.4 Some Business Decisions Remain in Application Services

Application services generally perform appropriate orchestration, but some use-case-specific business decisions remain in the Application layer.

A concrete example is:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

which calculates the amount of study time that can be recorded and coordinates lesson completion with enrollment progress.

This does not mean that the service is architecturally invalid.

Application services are expected to coordinate multiple domain objects and boundaries.

However, the target architecture establishes a clearer responsibility rule:

```text
Application
    → orchestrates use cases

Domain
    → owns domain invariants and business rules
```

Future implementation should therefore avoid allowing Application Services to become the primary location for business rules that properly belong to domain concepts.

This rule does not require moving every calculation into the Domain.

Responsibility must be decided according to the domain model and the specific business rule.

---

# 4. Target Architectural Style

DeutschHub V3 targets the following architectural combination:

```text
Modular Monolith
        +
Domain-Driven Design
        +
Hexagonal Architecture
        +
Business-oriented organization
```

Each principle has a different responsibility.

### Domain-Driven Design

DDD provides the approach for modeling the business domain.

It defines and guides:

* bounded contexts;
* aggregates;
* entities;
* value objects;
* domain behavior;
* domain invariants;
* ubiquitous language;
* domain boundaries.

### Modular Monolith

The modular monolith defines the system-level deployment and module boundaries.

DeutschHub remains a single deployable application while maintaining explicit internal business boundaries.

### Hexagonal Architecture

Hexagonal Architecture defines dependency direction and isolation from external technologies.

The core domain and application logic should not depend on infrastructure implementations.

### Business-oriented organization

Business-oriented organization makes important business responsibilities visible in the internal code structure.

It should be used where it improves clarity of the domain boundaries.

It is not a requirement that every concept become a separate module, package, or Bounded Context.

---

# 5. Target Dependency Architecture

The target dependency direction is:

```text
                 Infrastructure
                /              \
               ↓                ↓
       Input Adapters      Output Adapters
               ↓                ↑
               ↓                │
           Application ─────────┘
               ↓
             Domain
```

A simplified dependency model is:

```text
Infrastructure
      ↓
Application
      ↓
Domain
```

Dependencies must not point inward from the Domain toward Infrastructure.

### 5.1 Domain

The Domain contains business concepts and business rules.

It must not depend on:

* Spring;
* JPA;
* database implementations;
* HTTP;
* controllers;
* infrastructure adapters;
* external framework implementations.

### 5.2 Application

The Application layer defines and executes application use cases.

It may depend on:

* Domain objects;
* input ports;
* output ports;
* application-level abstractions.

It must not depend on concrete infrastructure implementations.

### 5.3 Infrastructure

Infrastructure provides implementations for external concerns, including:

* persistence;
* JPA;
* database access;
* HTTP adapters;
* framework integration;
* external services.

Infrastructure may depend on Application and Domain abstractions where required by the adapter implementation.

---

# 6. Target Domain Model Direction

The target architecture preserves the domain decisions already established.

## 6.1 Course Aggregate

The Course Aggregate remains responsible for course structure.

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

The existing Course Aggregate boundary should not be split without a concrete business consistency requirement.

Course structure remains part of the Learning Structure responsibility.

---

## 6.2 Enrollment Aggregate

Enrollment remains an independent Aggregate Root.

```text
Enrollment
└── Progress
```

Enrollment is responsible for the learner's participation in a specific course and its course-scoped progress.

The target architecture does not treat course progress as the complete learner state.

---

## 6.3 Progress

`Progress` remains a Value Object.

It represents course-scoped progress information associated with Enrollment.

The following concepts remain distinct:

```text
Course Progress
≠
Learner State
```

---

## 6.4 LessonCompletion

Lesson completion is treated as independent Learning Evidence.

```text
LessonCompletion
```

It is not required to become a child entity of the Enrollment Aggregate merely because completing a lesson affects enrollment progress.

The relationship is conceptually:

```text
LessonCompletion
        ↓
Learning Evidence
        ↓
contributes to
        ↓
Enrollment.Progress
```

Application services may coordinate these changes within a use case.

---

## 6.5 Quiz and QuizAttempt

Quiz and QuizAttempt remain distinct concepts.

```text
Quiz
    = assessment definition

QuizAttempt
    = learner attempt / assessment evidence
```

QuizAttempt is treated as an independent Aggregate Root and may represent Learning Evidence generated through assessment.

No decision is made here to create a generic Assessment Bounded Context.

---

# 7. Learning Business Boundaries

The Learning Context is understood through five major business responsibilities:

```text
Learning
├── Learning Structure
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

These are business boundaries within the Learning domain.

They are not automatically five Bounded Contexts or five Aggregates.

---

## 7.1 Learning Structure

Learning Structure represents what can be learned and how learning content is organized.

The current Course Aggregate belongs here:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

Learning Structure may also contain or reference other learning resources as the domain evolves.

---

## 7.2 Learning Activities

Learning Activities represent actions or learning interactions performed by a learner for the purpose of:

* learning;
* practicing;
* reviewing;
* listening;
* speaking;
* reading;
* writing;
* demonstrating knowledge or skills.

The target architecture explicitly distinguishes:

```text
LessonItem
≠
Learning Activity
```

A LessonItem primarily represents content or a learning resource within a lesson.

A Learning Activity represents the learner's interaction or action.

The exact domain model for Learning Activities remains open.

---

## 7.3 Learning Evidence

Learning Evidence represents observable outcomes produced by learner activity or assessment.

Examples include:

```text
LessonCompletion
QuizAttempt
```

Evidence is distinct from learner state.

```text
Activity
    ↓
Evidence
    ↓
Learner State
```

This does not require every activity to generate persistent evidence.

The exact evidence model depends on the semantics and lifecycle of each learning capability.

---

## 7.4 Learner State

Learner State represents what the system knows about the learner's learning condition and demonstrated capability.

Potential concepts include:

```text
Competency
Current Level
Vocabulary State
Grammar State
Skill State
XP
Streak
Achievement
Statistics
```

These concepts have not all been established as domain objects.

The target architecture therefore treats Learner State as a **business responsibility**, not as a single Aggregate Root.

Further aggregate and persistence decisions must be made based on concrete domain rules.

---

## 7.5 Learning Direction

Learning Direction represents what the learner should do next.

Potential capabilities include:

```text
Daily Learning
Learning Plans
Recommendations
Review Due
Weakness-oriented Practice
Learning Goals
Exam Preparation
```

Learning Direction is distinct from Learner State.

Conceptually:

```text
Learner State
      ↓
Learning Direction
      ↓
Next Learning Activity
```

The target architecture does not yet determine whether these capabilities should be persisted, derived, or modeled as independent domain concepts.

---

# 8. Application Layer Responsibility

The Application layer is responsible for use-case orchestration.

Typical responsibilities include:

* receiving use-case commands or queries;
* loading required domain objects;
* coordinating multiple Aggregates;
* invoking domain behavior;
* coordinating persistence through output ports;
* defining transaction boundaries;
* returning application results.

The Application layer should not become the primary owner of domain invariants.

The desired distinction is:

```text
Application:
"How do we execute this use case?"

Domain:
"What must be true for this operation to be valid?"
```

When a use case crosses multiple domain boundaries, the Application layer may coordinate them.

For example:

```text
Complete Lesson
      │
      ├── Enrollment
      ├── Course Structure
      ├── LessonCompletion
      └── Progress
```

The fact that these concepts participate in one use case does not imply that they must belong to the same Aggregate.

---

# 9. Ports and Adapters

The target architecture retains the Ports & Adapters approach already present in the system.

## 9.1 Input Ports

Input ports represent application use cases.

Conceptually:

```text
Controller
    ↓
Input Port
    ↓
Application Service
```

External interfaces should depend on application use cases rather than directly manipulating domain persistence.

---

## 9.2 Output Ports

Output ports represent capabilities required by the Application layer.

For example:

```text
CourseRepositoryPort
EnrollmentRepositoryPort
LessonCompletionRepositoryPort
```

Infrastructure provides concrete implementations:

```text
JpaCourseRepositoryAdapter
JpaEnrollmentRepositoryAdapter
JpaLessonCompletionRepositoryAdapter
```

The target architecture preserves this dependency direction:

```text
Application defines abstraction
            ↑
Infrastructure implements abstraction
```

---

# 10. Business-Oriented Organization

The target code organization should make important business boundaries visible without abandoning the existing architectural layers.

The exact final package structure is intentionally not fixed by this document.

The guiding principle is:

```text
Business boundaries
        +
Architectural boundaries
```

rather than organizing the entire system solely around technical types.

For example, Learning should increasingly make responsibilities such as:

```text
Course / Structure
Enrollment
Evidence
Learner State
Learning Direction
```

visible when the implementation becomes sufficiently mature to justify such organization.

This does not mean that every responsibility must immediately become a separate package or module.

The structure should follow established domain boundaries rather than anticipating every possible future feature.

---

# 11. Aggregate Boundary Rules

Aggregates are consistency boundaries, not simply collections of related objects.

The target architecture follows these rules:

1. Each Aggregate has a clear Aggregate Root.
2. The Aggregate Root controls invariants within its boundary.
3. Relationships between concepts do not automatically imply ownership.
4. An application transaction may coordinate multiple Aggregates.
5. Aggregates should not be split or merged without a concrete consistency requirement.
6. Persistence relationships do not determine Aggregate boundaries automatically.

Currently established boundaries are:

```text
Course Aggregate
Enrollment Aggregate
Quiz Aggregate
QuizAttempt Aggregate
```

and:

```text
LessonCompletion
    = independent Learning Evidence Entity
```

The following remain open:

```text
Competency
Current Level
Learning Activity
Learning Plan
Recommendation
Review Due
```

No Aggregate boundary is established for these concepts until their business invariants and consistency requirements are understood.

---

# 12. Domain State and Evidence

The target architecture explicitly separates three concepts:

```text
Evidence
Progress
Learner State
```

They answer different questions.

### Evidence

"What happened?"

Examples:

```text
LessonCompletion
QuizAttempt
```

### Progress

"How much of a particular learning structure has been completed?"

Example:

```text
Enrollment.Progress
```

### Learner State

"What does the system currently know about the learner's learning condition and demonstrated capability?"

Examples may eventually include:

```text
Competency
Current Level
Vocabulary State
Skill State
```

These concepts must not be collapsed into a single generic progress model.

---

# 13. Level Semantics

The target architecture preserves the distinction between:

```text
Course Level
Learner Current Level
Certification Level
```

These concepts represent different business meanings.

In particular:

```text
Course completion
≠
Learner Current Level
```

and:

```text
Assessment score
≠
Learner Current Level
```

unless explicit domain rules establish how evidence contributes to level determination.

The current `CEFRLevel` value object represents level classification used by learning content, but a complete learner-level Current Level model has not yet been established.

Therefore, the target architecture does not prescribe a specific level-calculation mechanism.

---

# 14. Cross-Context Responsibilities

The system should maintain clear responsibility boundaries between major contexts.

At a high level:

```text
Identity
    = who the user is

Learning
    = how the user learns

Content
    = learning resources and content

Media
    = media resources
```

Learning may reference identity information such as a user identifier, but Learning should not become responsible for Identity management.

Similarly, learning activities may consume Content or Media resources without taking ownership of those contexts' responsibilities.

Cross-context relationships should be expressed through appropriate abstractions rather than direct coupling to another context's infrastructure.

---

# 15. Target Learning Flow

The target Learning model can be summarized as a continuous learning loop:

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
        ↓
...
```

This loop represents the intended business model.

It does not require every step to be implemented as a separate technical component.

For example, an activity may produce evidence, while some learner state may be derived from accumulated evidence.

The implementation mechanism remains dependent on future domain decisions.

---

# 16. Architectural Principles

The following principles guide future implementation.

### Principle 1 — Preserve working boundaries

Existing architectural boundaries should not be changed without a concrete problem or business requirement.

### Principle 2 — Domain independence

The Domain must remain independent of framework and infrastructure concerns.

### Principle 3 — Dependency inversion

Application and Domain code must not depend on concrete infrastructure implementations.

### Principle 4 — Business rules belong to the domain model

Business invariants should be expressed in the appropriate Domain concept whenever the concept and rule are sufficiently understood.

### Principle 5 — Application services orchestrate

Application Services coordinate use cases and multiple boundaries rather than becoming the default location for all business logic.

### Principle 6 — Aggregates are consistency boundaries

Aggregate boundaries are determined by business consistency requirements, not database relationships or package organization.

### Principle 7 — Do not over-model unresolved concepts

Unresolved concepts should remain open until their business semantics are sufficiently understood.

### Principle 8 — Business organization follows evidence

Code organization may become more business-oriented where current boundaries justify it.

It should not anticipate hypothetical future requirements.

### Principle 9 — Do not introduce architectural mechanisms without a problem

Events, additional abstraction layers, new Bounded Contexts, or other architectural mechanisms should only be introduced when a concrete requirement justifies them.

---

# 17. Confirmed Architectural Decisions

The following decisions are established for the target architecture.

| Decision                                                                     | Status    |
| ---------------------------------------------------------------------------- | --------- |
| Modular Monolith remains the system architecture                             | Confirmed |
| Hexagonal / Ports & Adapters remains the dependency architecture             | Confirmed |
| Domain/Application/Infrastructure separation remains                         | Confirmed |
| Course remains an Aggregate Root                                             | Confirmed |
| Enrollment remains an Aggregate Root                                         | Confirmed |
| Progress remains a Value Object                                              | Confirmed |
| Quiz remains an Aggregate Root                                               | Confirmed |
| QuizAttempt remains an independent Aggregate Root                            | Confirmed |
| LessonCompletion is independent Learning Evidence                            | Confirmed |
| Course Progress is distinct from Learner State                               | Confirmed |
| Evidence is distinct from Learner State                                      | Confirmed |
| Competency is distinct from Progress and Evidence                            | Confirmed |
| Course Level is distinct from Learner Current Level                          | Confirmed |
| UserProgress is not the target representation of Learner State               | Confirmed |
| Domain must not depend on Spring/JPA/Infrastructure                          | Confirmed |
| Application must not depend on concrete Infrastructure implementations       | Confirmed |
| Business-oriented organization may be used to make domain boundaries visible | Confirmed |

---

# 18. Open Architectural and Domain Decisions

The following decisions remain intentionally open.

### Learner State

* Whether Learner State requires one or multiple Aggregates.
* The Aggregate boundary of Competency.
* The Aggregate boundary of Current Level.
* Whether vocabulary, grammar, and skills require separate domain models.
* Which learner state is persisted and which state is derived.

### Learning Activities

* The exact representation of Learning Activity.
* Whether different activity types share a common abstraction.
* Which activities require persistence.
* Which activities generate Learning Evidence.

### Learning Direction

* The model of Learning Plan.
* The model of Recommendations.
* Review Due representation.
* Learning Goals.
* Exam Preparation.
* Whether direction is persisted or derived.

### Evidence

* Rules for transforming evidence into learner state.
* Rules for aggregating multiple evidence types.
* Whether additional evidence concepts are required.

### Learner progression

* XP semantics.
* Streak semantics.
* Achievement semantics.
* Statistics and derived metrics.
* Rules connecting evidence, competency, and level.

These decisions must not be assumed merely from the existence of similarly named features.

---

# 19. Implementation Constraints for Future Development

When implementation begins, new code should follow the target architecture rather than reproducing the identified problems.

In particular:

```text
Domain
    ↓
must remain framework-independent
```

```text
Application
    ↓
may orchestrate multiple domain boundaries
    ↓
but should not become a repository of business rules
```

```text
Infrastructure
    ↓
implements ports and external concerns
```

Existing working implementations should be changed only when:

* a documented architectural problem requires the change;
* a new domain decision requires a different boundary;
* an implementation violates an established target rule;
* or a concrete feature exposes a limitation in the current design.

The target architecture is therefore a constraint for **future evolution**, not a requirement to immediately rewrite all existing code.

---

# 20. Target Architecture Summary

DeutschHub V3 targets a modular monolith using DDD for domain modeling and Hexagonal Architecture for dependency isolation.

The architecture preserves the current working Domain/Application/Infrastructure and Ports & Adapters foundations while making business responsibilities more explicit.

The Learning Context evolves from a predominantly Course-centered implementation toward a broader learner-centered learning experience:

```text
Learning
├── Learning Structure
├── Learning Activities
├── Learning Evidence
├── Learner State
└── Learning Direction
```

The target model preserves established boundaries such as:

```text
Course
Enrollment
Quiz
QuizAttempt
LessonCompletion
```

while leaving unresolved concepts open until their business semantics are sufficiently understood.

The central architectural principle is:

> **Refine the existing architecture according to identified business and dependency problems; do not introduce structural change without concrete justification.**

The target architecture therefore provides direction without prematurely fixing every domain concept, Aggregate, Bounded Context, package, or persistence model.

