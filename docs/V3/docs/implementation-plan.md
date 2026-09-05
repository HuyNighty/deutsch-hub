# Implementation Plan

## 1. Purpose

This document defines the implementation strategy for evolving DeutschHub V3 from its current architecture and domain model toward the target architecture established in the preceding design documents.

The implementation plan is intentionally incremental.

The target architecture does not require an immediate rewrite of the existing system. Existing components that already satisfy the target architectural direction should be preserved.

Implementation work should be driven by:

- established domain decisions;
- identified architectural problems;
- concrete business requirements;
- dependency and consistency boundaries;
- the target module boundaries.

The implementation plan therefore distinguishes between:

```text
Preserve
Fix
Refine
Build
````

rather than treating all existing code as requiring replacement.

---

# 2. Implementation Principles

## 2.1 Design Before Structural Change

Implementation must follow the domain and architectural decisions already established in:

* `target-learning-boundaries.md`
* `target-domain-model.md`
* `domain-decisions.md`
* `target-aggregate-boundaries.md`
* `target-architecture.md`
* `target-module-boundaries.md`

Code structure must not be changed merely to anticipate unresolved domain decisions.

---

## 2.2 Preserve Working Architecture

The current system already provides:

* Modular Monolith structure;
* Domain/Application/Infrastructure separation;
* Input Ports;
* Output Ports;
* Persistence Adapters;
* Domain behavior within several Aggregates;
* separated JPA persistence models.

These foundations should be preserved unless a concrete problem requires change.

---

## 2.3 Fix Concrete Problems First

Known architectural inconsistencies should be resolved before introducing new domain capabilities.

Examples include:

```text
Domain → Spring dependency
UserProgress duplication
unclear business responsibility in selected Application Services
```

This prevents new features from being built on top of known inconsistencies.

---

## 2.4 Do Not Introduce Unresolved Domain Models Prematurely

The following concepts are part of the target domain direction but do not yet have final internal models:

```text
Competency
Current Level
Learning Activity
Vocabulary State
Grammar State
Skill State
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

They should not be implemented as arbitrary Aggregates or entities simply because their names have already been identified.

Their implementation should follow explicit domain rules.

---

## 2.5 One Boundary at a Time

Implementation should proceed in small architectural increments.

Each increment should leave the system in a coherent state.

The preferred flow is:

```text
Analyze
   ↓
Decide
   ↓
Implement
   ↓
Verify
   ↓
Commit
   ↓
Continue
```

A phase should not depend on undocumented assumptions from a future phase.

---

# 3. Implementation Classification

Each existing or future component should be classified into one of four categories.

## 3.1 Preserve

The implementation already satisfies the target architecture sufficiently.

Examples:

```text
Course Aggregate
Enrollment Aggregate
Progress Value Object
Repository Ports
Jpa Repository Adapters
```

These should not be rewritten without a concrete reason.

---

## 3.2 Fix

The implementation contains a concrete problem that violates an established architectural or domain rule.

Current examples include:

```text
MediaTypeResolver
UserProgress
```

Fixes should remain focused on the identified problem.

---

## 3.3 Refine

The implementation is valid but its responsibility or boundary should become clearer as the target model evolves.

Examples may include:

```text
CompleteLessonService
Learning module organization
Evidence handling
```

Refinement must be based on an established target responsibility rather than stylistic preference.

---

## 3.4 Build

The capability does not yet have a complete implementation and must be introduced as a new capability.

Examples include:

```text
Learner State
Competency
Current Level
Learning Activities
Learning Direction
```

Build work should occur only after the relevant domain decisions are sufficiently established.

---

# 4. Phase 0 — Establish the Architectural Baseline

## Objective

Ensure that implementation begins from a known and documented architectural baseline.

The baseline consists of:

```text
Current architecture
Current Learning boundaries
Target domain model
Aggregate boundaries
Target architecture
Target module boundaries
```

No production-code restructuring is required in this phase.

## Output

A stable implementation reference consisting of the completed design documents.

## Completion Criteria

The following are understood before implementation begins:

* current architectural boundaries;
* target architectural direction;
* established Aggregate boundaries;
* established business module boundaries;
* unresolved domain decisions;
* known architectural problems.

---

# 5. Phase 1 — Protect Architectural Boundaries

## Objective

Resolve concrete violations of the target dependency rules without changing business behavior unnecessarily.

The first confirmed issue is framework leakage into the Domain.

Current location:

```text
src/main/java/com/deutschhub/domain/media/service/MediaTypeResolver.java
```

The current implementation uses Spring's component mechanism inside the Domain.

The target rule is:

```text
Domain
    ✕ Spring
    ✕ JPA
    ✕ Infrastructure
```

## Scope

The implementation should:

1. identify the actual reason `MediaTypeResolver` requires framework registration;
2. determine whether it is a pure domain service;
3. remove the unnecessary framework dependency if confirmed;
4. preserve its existing domain behavior;
5. verify that no infrastructure dependency is introduced as a replacement.

The implementation should not introduce a new abstraction unless the concrete dependency requires one.

## Completion Criteria

The Domain no longer depends on Spring for this capability.

The existing media-type resolution behavior remains unchanged.

---

# 6. Phase 2 — Stabilize Existing Learning Domain

## Objective

Ensure that existing Learning Aggregates and Value Objects conform to the domain decisions already established.

The primary established model is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

and:

```text
Enrollment
└── Progress
```

Existing Aggregates such as `Course`, `Enrollment`, `Quiz`, and `QuizAttempt` should be preserved.

## Scope

The implementation should verify:

* Aggregate Root ownership;
* domain invariants;
* lifecycle transitions;
* Value Object invariants;
* persistence mapping;
* application orchestration.

No Aggregate should be split or merged without a documented consistency requirement.

---

## 6.1 UserProgress

The current:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

should be treated as a known model inconsistency.

The target architecture does not define `UserProgress` as the canonical Learner State model.

Before changing it, implementation must determine:

* which current use cases depend on it;
* whether it is persisted;
* whether it is referenced by application code;
* whether it has active API exposure;
* whether its data is duplicated by Enrollment and Progress.

Only after this usage is understood should the implementation determine whether it should be removed, replaced, or temporarily retained.

The target decision is:

```text
UserProgress
    ✕
canonical Learner State
```

The implementation mechanism remains dependent on actual usage.

---

## 6.2 Existing Invariant Inconsistency

The current `UserProgress` implementation contains an inconsistency involving:

```text
Progress.createInitial(0)
```

while `Progress` requires a positive total lesson count.

This should be resolved as part of stabilizing the existing model if `UserProgress` remains active during implementation.

The fix must preserve the invariant established by `Progress`.

---

# 7. Phase 3 — Clarify Learning Evidence

## Objective

Stabilize Learning Evidence as a distinct responsibility.

The target evidence concepts include:

```text
LessonCompletion
QuizAttempt
```

with the distinction:

```text
LessonCompletion
    = lesson completion evidence

QuizAttempt
    = assessment attempt / assessment evidence
```

## Scope

The implementation should ensure that:

* evidence is not confused with progress;
* evidence does not become a child entity solely because another Aggregate consumes it;
* existing persistence behavior remains consistent;
* application services can coordinate evidence and Aggregate updates without collapsing their boundaries.

---

## 7.1 LessonCompletion

The established boundary is:

```text
LessonCompletion
    = independent Evidence Entity
```

The implementation should preserve this boundary.

The current use case may continue to coordinate:

```text
LessonCompletion
        +
Enrollment.Progress
```

within one application transaction.

A transaction spanning multiple domain boundaries does not require merging those boundaries.

---

## 7.2 Evidence-to-State Rules

The implementation should not yet introduce a generic mechanism for:

```text
Evidence
    ↓
Learner State
```

until the rules determining how evidence changes learner state have been explicitly defined.

This prevents premature event processing, generic evidence pipelines, or state-calculation abstractions.

---

# 8. Phase 4 — Establish Learner State

## Objective

Introduce learner-centered state only after its domain semantics are sufficiently defined.

The target responsibility is:

```text
Learner State
```

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

Not all concepts are required to be implemented at the same time.

---

## 8.1 Competency

Competency should be introduced only after defining:

* what constitutes a competency;
* how competency is identified;
* how competency changes;
* what evidence contributes to it;
* whether competency requires an Aggregate boundary.

The implementation must not infer competency directly from:

```text
course completion
```

or:

```text
raw assessment score
```

without explicit domain rules.

---

## 8.2 Current Level

Current Level should be introduced only after defining:

* what determines a learner's level;
* what evidence contributes to level;
* whether level changes are automatic or explicit;
* how CEFR classification is used;
* how current level differs from Course Level and Certification Level.

The target distinction remains:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

---

## 8.3 Derived Versus Persisted State

For each learner-state concept, implementation should determine whether it is:

```text
Persisted domain state
```

or:

```text
Derived state
```

This decision should be based on business requirements and consistency needs.

The existence of a database table is not itself sufficient justification for a domain Aggregate.

---

# 9. Phase 5 — Establish Learning Activities

## Objective

Introduce Learning Activities as a distinct business responsibility from Learning Structure.

The target distinction is:

```text
LessonItem
    ≠
Learning Activity
```

A Learning Activity represents a learner interaction.

Potential activity categories include:

```text
Practice
Review
Listening
Speaking
Reading
Writing
Assessment interaction
```

---

## Scope

Before implementation, each activity type should be analyzed for:

* lifecycle;
* required state;
* learner interaction;
* persistence needs;
* evidence produced;
* relationship to learning content;
* consistency requirements.

Only then should the appropriate Entity, Aggregate, or other domain representation be selected.

---

## Generic Activity Model

A generic `LearningActivity` Aggregate should not be introduced unless multiple activity types demonstrably share:

* meaningful common behavior;
* lifecycle;
* invariants;
* consistency requirements.

Different activities may require different models.

---

# 10. Phase 6 — Establish Learning Direction

## Objective

Introduce mechanisms that determine or recommend what the learner should do next.

The target responsibility includes potential capabilities such as:

```text
Daily Learning
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
Weakness-oriented Practice
```

The conceptual relationship is:

```text
Learner State
      ↓
Learning Direction
      ↓
Next Learning Activity
```

---

## Scope

Each direction capability should first establish:

* its business purpose;
* required learner information;
* decision rules;
* persistence requirements;
* relationship to activities;
* relationship to goals;
* whether the result is deterministic, configurable, or derived.

No generic `LearningDirection` Aggregate should be introduced without concrete domain requirements.

---

# 11. Phase 7 — Refine Module Organization

## Objective

Make established business boundaries visible in code organization when implementation evidence justifies doing so.

The target business responsibilities are:

```text
Learning Structure
Enrollment
Learning Activities
Learning Evidence
Learner State
Learning Direction
```

The exact package structure is intentionally not fixed by the target architecture documents.

---

## Rules

Module organization should:

* make business responsibility visible;
* preserve Hexagonal dependency rules;
* avoid unnecessary duplication;
* avoid creating packages without meaningful responsibility;
* avoid reorganizing stable code merely for aesthetic consistency.

Business-oriented organization should be introduced incrementally.

For example, existing Course and Enrollment structures may remain stable if their current organization already provides sufficient clarity.

---

# 12. Phase 8 — Strengthen Application and Domain Responsibilities

## Objective

Review Application Services after the relevant domain boundaries have been established.

The target responsibility distinction is:

```text
Application
    = use-case orchestration

Domain
    = business rules and invariants
```

The review should focus on concrete cases rather than performing a blanket migration of logic.

---

## 12.1 CompleteLessonService

The current:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

coordinates:

```text
Enrollment
Course
LessonCompletion
Progress
```

This orchestration is valid.

However, business calculations such as study-time constraints should be reviewed against the final domain model.

The implementation should ask:

```text
Is this rule specific to the use case?

or

Is this a business invariant of a domain concept?
```

Only the second category should necessarily move into the Domain.

---

## 12.2 No Blanket Service Refactoring

Application Services should not be rewritten merely to make them smaller.

A service can legitimately coordinate:

* multiple Aggregates;
* repositories;
* Evidence;
* transactions;
* external operations.

The target architecture does not require every operation to be moved into a Domain method.

---

# 13. Phase 9 — Verification

Each implementation phase must be verified before moving to the next phase.

Verification should include, where applicable:

```text
Compile
    ↓
Unit Tests
    ↓
Integration Tests
    ↓
Architecture Checks
    ↓
Manual Verification
```

The exact verification mechanism depends on the capability being changed.

---

## 13.1 Domain Verification

Verify:

* invariants;
* lifecycle transitions;
* invalid state rejection;
* Aggregate behavior;
* Value Object behavior.

---

## 13.2 Application Verification

Verify:

* use-case orchestration;
* transaction behavior;
* correct port usage;
* error handling;
* coordination across boundaries.

---

## 13.3 Infrastructure Verification

Verify:

* persistence mapping;
* adapter behavior;
* repository queries;
* serialization;
* external integration behavior.

---

# 14. Phase 10 — Commit Boundaries

Implementation should use small, meaningful commits.

A commit should ideally represent one coherent architectural or business change.

Examples:

```text
fix(domain): remove framework dependency from media resolver

fix(learning): stabilize progress invariant

refactor(learning): clarify evidence boundary

feat(learning): introduce competency state

feat(learning): add learning activity model
```

Commit naming should describe the actual change rather than the entire architectural vision.

---

# 15. Implementation Order

The recommended implementation order is:

```text
Phase 0
Architectural Baseline
        ↓
Phase 1
Protect Architectural Boundaries
        ↓
Phase 2
Stabilize Existing Learning Domain
        ↓
Phase 3
Clarify Learning Evidence
        ↓
Phase 4
Establish Learner State
        ↓
Phase 5
Establish Learning Activities
        ↓
Phase 6
Establish Learning Direction
        ↓
Phase 7
Refine Module Organization
        ↓
Phase 8
Refine Application / Domain Responsibilities
        ↓
Verification throughout
```

This is a recommended dependency order, not a requirement that every phase must contain a large implementation.

A phase may remain small if the existing implementation already satisfies the target decision.

---

# 16. What Must Not Happen

The implementation must avoid the following patterns.

## 16.1 Full Rewrite

Do not replace the current architecture wholesale.

The existing Hexagonal and Modular Monolith foundations are retained.

---

## 16.2 Pattern-Driven Refactoring

Do not introduce architectural mechanisms solely because they are commonly associated with DDD.

Examples include:

* generic domain events;
* event buses;
* repositories for every entity;
* generic aggregate roots;
* additional abstraction layers;
* separate modules for every business concept.

Each mechanism requires a concrete reason.

---

## 16.3 Premature Aggregate Creation

Do not create Aggregates for:

```text
Competency
Current Level
Learning Activity
Learning Plan
Recommendation
Review Due
```

until their consistency requirements are understood.

---

## 16.4 Course-Centric Expansion

Do not continue extending Course as the owner of concepts that belong to broader learner behavior.

Course should remain responsible for Learning Structure.

It should not become the owner of:

```text
Competency
Current Level
Learner State
Learning Direction
```

---

## 16.5 UserProgress Expansion

Do not continue expanding `UserProgress` as a general-purpose learner state container.

Its current overlap with Enrollment and Progress is a known domain-model problem.

---

# 17. Implementation Decision Gate

Before implementing a new domain concept, answer:

### Business

```text
What business responsibility does this concept represent?
```

### Boundary

```text
Which module owns that responsibility?
```

### Consistency

```text
What must remain consistent together?
```

### Domain Model

```text
Does this require an Aggregate, Entity, Value Object,
or another domain representation?
```

### Application

```text
Which use cases coordinate it?
```

### Infrastructure

```text
Does it require persistence or an external adapter?
```

### Evidence

```text
What existing requirement or domain rule justifies this design?
```

If these questions cannot be answered sufficiently, the implementation should remain open rather than guessing.

---

# 18. Definition of Done for Architectural Changes

An architectural change is considered complete when:

* the relevant domain decision is documented;
* the implementation follows the established boundary;
* dependency direction remains valid;
* business invariants remain inside appropriate domain concepts;
* Application Services remain orchestration-focused;
* Infrastructure remains outside the core;
* tests or verification cover the affected behavior;
* no unrelated structural changes were introduced.

---

# 19. Target State

The implementation should gradually move the system toward:

```text
Modular Monolith
        +
DDD
        +
Hexagonal Architecture
        +
Business-oriented organization
```

with Learning organized around:

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

while preserving established Aggregate boundaries:

```text
Course
Enrollment
Quiz
QuizAttempt
```

and the independent Evidence boundary:

```text
LessonCompletion
```

The final implementation does not need to realize every target capability simultaneously.

The target architecture defines the direction; implementation should introduce each capability when its domain model and business requirements are sufficiently established.

---

# 20. Summary

The implementation strategy for DeutschHub V3 is incremental and evidence-driven.

The current architecture provides a strong foundation and should not be replaced wholesale.

Implementation should proceed by:

```text
Protect
    ↓
Stabilize
    ↓
Clarify
    ↓
Build
    ↓
Refine
```

The most important implementation constraint is:

> **Do not change architecture because a pattern suggests doing so. Change architecture when a concrete business or dependency problem requires it.**

The implementation plan therefore treats the target architecture as a set of constraints for future development rather than as a mandate for immediate large-scale refactoring.

````
1. discovery
        ↓
2. current state
        ↓
3. domain analysis
        ↓
4. architecture analysis
        ↓
5. current boundaries
        ↓
6. target boundaries
        ↓
7. target domain model
        ↓
8. domain decisions
        ↓
9. aggregate boundaries
        ↓
10. target architecture
        ↓
11. target module boundaries
        ↓
12. implementation plan
