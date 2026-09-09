# Implementation Plan

## 1. Purpose

This document defines how the current DeutschHub backend is evolved toward the V3 target model.

The implementation strategy is intentionally incremental.

The goal is not to rewrite the existing backend at once.

Instead, implementation proceeds through small, verifiable slices that:

- preserve working behavior where possible;
- fix concrete problems before introducing structural changes;
- implement established domain decisions;
- avoid prematurely implementing unresolved concepts;
- keep changes reviewable and reversible;
- synchronize target documents only when accumulated changes justify it.

The implementation plan therefore acts as the bridge between:

```text
Domain Decisions
        ↓
Target Model
        ↓
Implementation
````

---

# 2. Implementation Principles

## 2.1. Incremental Evolution

The existing backend is treated as a working baseline and source of implementation evidence.

It is not treated as the final source of truth for the V3 domain model.

The implementation should therefore follow:

```text
Current Code
     ↓
Understand
     ↓
Preserve what is valid
     ↓
Fix concrete problems
     ↓
Implement established target decisions
```

A complete rewrite is not the default strategy.

---

## 2.2. Small Implementation Slices

Implementation should be divided into small slices.

A slice should normally have:

* one clear objective;
* a limited set of affected files;
* an identifiable business or architectural reason;
* a clear verification method;
* a separate commit when appropriate.

Examples:

```text
Fix Course lesson ordering
```

```text
Protect published Course from modification
```

```text
Stabilize Enrollment progress update
```

```text
Implement Quiz Attempt creation
```

A slice should not combine unrelated refactoring merely because the affected code is nearby.

---

## 2.3. Domain Decision Before Domain Code

Before implementing a new business concept, the following must be sufficiently understood:

```text
Business meaning
       ↓
Responsibility
       ↓
Invariants
       ↓
Boundary
       ↓
Implementation
```

The implementation should not invent business rules simply because the code needs an answer.

If a decision is required to implement the slice and has not been established, that decision becomes an implementation blocker and must return to domain analysis.

---

## 2.4. Current Code Is Evidence, Not Target Truth

Existing code may contain:

* valid behavior;
* incomplete behavior;
* legacy concepts;
* duplicated responsibilities;
* implementation shortcuts;
* domain assumptions that are no longer correct.

Therefore:

```text
Current Code
    ≠
Target Domain Model
```

The target model is used when a deliberate V3 decision differs from the legacy implementation.

However, legacy code should not be changed merely because the target model looks different.

A concrete implementation reason is required.

---

# 3. Implementation Readiness

A domain area is ready for implementation when:

* its business responsibility is understood;
* required business rules are known;
* required Aggregate boundaries are sufficiently established;
* required application behavior is understood;
* unresolved decisions do not block the intended slice;
* persistence requirements are sufficiently clear;
* verification can be defined.

Not every OPEN decision must be closed before implementation.

The relevant distinction is:

```text
OPEN BUT REQUIRED
        →
must be decided before implementation

OPEN BUT DEFERRED
        →
does not block the current slice
```

---

# 4. Implementation Workflow

Every implementation slice follows this workflow:

```text
1. Select Slice
       ↓
2. Read Existing Code
       ↓
3. Identify Concrete Problem / Requirement
       ↓
4. Check Relevant Domain Decision
       ↓
5. Define Minimal Code Impact
       ↓
6. Implement
       ↓
7. Verify
       ↓
8. Review Boundary / Dependencies
       ↓
9. Commit
```

The workflow does not require updating every target document after every slice.

---

# 5. Documentation Workflow

## 5.1. Domain Documents

Domain-specific documents are the primary working documents during analysis.

Examples:

```text
course/
enrollment/
quiz/
assessment/
learner-state/
```

When a local domain decision changes, the relevant domain document should be updated first.

---

## 5.2. Target Documents

The following are target-level synthesis documents:

```text
target-domain-model.md
target-aggregate-boundaries.md
target-module-boundaries.md
target-architecture.md
```

They should not be updated automatically after every local decision.

Instead:

```text
Local Decision
      ↓
Does it affect target architecture/boundaries?
      ↓
No ─────────────→ Keep target documents unchanged
      │
      Yes
      ↓
Record impact
      ↓
Synchronize at milestone
```

---

## 5.3. Target Synchronization

Target documents are synchronized at a **milestone boundary**, not after every small implementation decision.

A milestone is reached when a coherent group of implementation slices is complete.

For example:

```text
Course stabilization
        ↓
Verification
        ↓
Milestone
        ↓
Target synchronization
```

At the milestone, review:

```text
Target Domain Model
Target Aggregate Boundaries
Target Module Boundaries
Target Architecture
```

Only documents affected by the accumulated decisions are updated.

---

# 6. Target Synchronization Rules

## Rule 1

A new domain concept does not automatically require target-document changes.

---

## Rule 2

A local business rule does not automatically require architecture changes.

---

## Rule 3

A change in Aggregate boundary requires target Aggregate review.

---

## Rule 4

A change in business responsibility requires target Module review.

---

## Rule 5

A change in dependency direction, layer responsibility, or architectural style requires target Architecture review.

---

## Rule 6

If no target-level impact exists, target documents remain unchanged.

---

## Rule 7

Target documents should describe the stabilized target state, not every intermediate design discussion.

---

# 7. Implementation Classification

Every existing code area should be classified before modification.

Use four categories:

```text
PRESERVE
FIX
REFINE
BUILD
```

---

## 7.1. PRESERVE

Use when the existing implementation already satisfies the target requirement.

Example:

```text
Course
```

may remain the Aggregate Root if its existing boundary and behavior satisfy the target model.

No refactor is required merely to make the code look different.

---

## 7.2. FIX

Use when a concrete defect or invariant violation exists.

Examples identified during the current audit include:

```text
Lesson.changeOrderIndex
```

where validation exists but the new order is not correctly assigned.

Another example is:

```text
Section.update
```

where validation currently checks the existing values rather than the incoming values.

These are concrete implementation defects and can be fixed independently.

---

## 7.3. REFINE

Use when the existing concept is valid but its implementation does not yet match the established target responsibility.

Examples:

```text
Course
Enrollment
Quiz
QuizAttempt
```

may require refinement as their V3 business rules become implemented.

Refinement must remain scoped to the established responsibility.

---

## 7.4. BUILD

Use when the target capability does not currently exist.

Examples may include:

```text
Assessment
Competency
Learner State
```

but only when the corresponding business model is sufficiently defined for the intended implementation slice.

---

# 8. Phase 1 — Stabilize Existing Architectural Foundation

## Objective

Resolve concrete architectural problems that are independent of larger domain redesign.

---

## 8.1. Domain Framework Dependency

Current source:

```text
src/main/java/com/deutschhub/domain/media/service/MediaTypeResolver.java
```

The Domain currently has a dependency on Spring through `@Component`.

Target rule:

```text
Domain
    ✕
Spring
```

The implementation should remove the framework dependency from the Domain without introducing an unnecessary architectural abstraction.

---

## 8.2. Verification

After the change:

* Domain code should not require Spring for this behavior;
* application behavior should remain unchanged;
* tests should verify the resolver behavior.

---

# 9. Phase 2 — Stabilize Course

## Objective

Preserve the established Course Aggregate while fixing concrete implementation defects.

Target boundary:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

Current source locations:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java

src/main/java/com/deutschhub/domain/learning/model/entity/Section.java

src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java

src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

---

## 9.1. Lesson Ordering

Current issue:

```text
Lesson.changeOrderIndex(...)
```

validates the incoming order but does not correctly update the stored order.

Classification:

```text
FIX
```

Scope:

```text
Lesson
```

Do not redesign the Course Aggregate because of this defect.

---

## 9.2. Section Update Validation

Current issue:

```text
Section.update(...)
```

does not consistently validate the incoming values against the intended business constraints.

Classification:

```text
FIX
```

Scope:

```text
Section
```

Again, this is a local domain correction.

---

## 9.3. Other Course Decisions

The following remain subject to their established domain decisions:

* Course publication;
* Course modification after publication;
* Course deletion;
* LessonItem duration;
* LessonItem type mutability;
* authorization placement.

Do not resolve deferred questions merely because the related code exists.

---

# 10. Phase 3 — Stabilize Enrollment and Progress

## Objective

Preserve:

```text
Enrollment
    └── Progress
```

as the Course-scoped participation and progress model.

Current sources:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java

src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

---

## 10.1. Enrollment

The implementation should preserve Enrollment as an independent Aggregate Root.

Responsibilities include:

* learner participation;
* enrollment lifecycle;
* Course-scoped progress;
* completion state.

---

## 10.2. Progress

`Progress` remains a Value Object belonging to Enrollment.

It should not be expanded into a generic learner state.

---

## 10.3. UserProgress

Current source:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

Current analysis indicates overlap with:

```text
Enrollment.Progress
```

Therefore:

```text
UserProgress
    ≠
canonical Learner State
```

Do not immediately delete or rewrite it.

First identify all actual usages.

Classify each usage:

```text
Active business behavior
Persistence only
Read model
Legacy
Unused
```

Then determine the smallest safe change.

---

# 11. Phase 4 — Stabilize Learning Evidence

## Objective

Make the existing evidence concepts consistent without creating a generic evidence framework prematurely.

Established concepts include:

```text
LessonCompletion
QuizAttempt
QuestionResult
AssessmentResult
```

---

## 11.1. LessonCompletion

Current source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

Target responsibility:

```text
LessonCompletion
    =
Lesson completion evidence
```

It remains separate from:

```text
Enrollment.Progress
```

A use case may update both within one application transaction.

This does not require merging them into one Aggregate.

---

## 11.2. Assessment Evidence

Assessment execution may produce historical evidence.

For example:

```text
QuizAttempt
      ↓
QuestionResult
      ↓
Assessment Result
```

Evidence should not automatically update learner state unless an explicit business rule allows it.

---

## 11.3. Avoid Generic Evidence Infrastructure

Do not introduce:

```text
GenericEvidenceRepository
GenericEvidencePipeline
GenericEvidenceEventBus
```

unless a concrete requirement requires such mechanisms.

The first goal is to stabilize the actual evidence concepts already required by the product.

---

# 12. Phase 5 — Stabilize Quiz and QuizAttempt

## Objective

Implement the established Quiz model without expanding it into a generic Assessment framework prematurely.

Established boundary:

```text
Quiz
 └── QuizRevision
      └── Question
           └── Answer
```

Separate execution boundary:

```text
QuizAttempt
```

Current sources include:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java

src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java

src/main/java/com/deutschhub/domain/learning/model/entity/Question.java

src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java

src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java
```

---

## 12.1. Quiz

Implement the established Quiz lifecycle and revision rules.

The Quiz Aggregate owns:

```text
Quiz
 └── QuizRevision
      └── Question
           └── Answer
```

---

## 12.2. QuizAttempt

`QuizAttempt` remains a separate Aggregate Root.

It binds to the exact published QuizRevision used for the attempt.

This preserves historical integrity.

Later Quiz revisions must not change the meaning of an existing attempt.

---

## 12.3. Attempt Rules

The established Quiz-specific rules should be implemented only within Quiz.

Examples include:

* attempt limits;
* one active attempt;
* revision binding;
* timeout;
* submission;
* expiration;
* unanswered questions;
* result creation.

These rules should not automatically be generalized to all future Assessment types.

---

# 13. Phase 6 — Implement Assessment

## Objective

Introduce Assessment only to the extent required by established V3 requirements.

Assessment is broader than Quiz.

Conceptually:

```text
Assessment
 ├── Component
 │    └── Task(s)
 ├── Completion Policy
 ├── Time Limit
 ├── Attempt Rules
 └── Result
```

A Component belongs to one Skill Dimension.

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

## 13.1. Assessment Scope

The following are established:

* Assessment is broader than Quiz;
* Assessment may contain multiple Components;
* each Component belongs to one Skill Dimension;
* each Component may contain multiple Tasks;
* Component Results may be produced;
* Assessment Result is the official historical result;
* each execution is a separate Assessment Attempt;
* an active Attempt may be resumed;
* Attempt binds a stable Assessment definition/version;
* timeout ends the Attempt and evaluates according to the Assessment rules;
* Assessment may have an attempt limit;
* Level Assessment may establish learner level when passed.

---

## 13.2. Deferred Assessment Decisions

The following remain open unless required by the implementation slice:

```text
Assessment Aggregate boundary
Assessment Revision lifecycle
Assessment ↔ Quiz structural relationship
Evaluation Mechanism implementation
Exact Component Result behavior for unanswered Components
```

If one of these becomes necessary to implement a concrete feature, it becomes:

```text
OPEN BUT REQUIRED
```

and must be decided before proceeding.

Otherwise it remains:

```text
OPEN BUT DEFERRED
```

---

# 14. Phase 7 — Implement Learner State

## Objective

Introduce learner-level state only after the business model is sufficiently defined.

The target responsibility includes:

```text
Learner State
├── Competency
└── Current Level
```

Other learner-state concepts remain deferred unless required by V3 implementation scope.

---

## 14.1. Competency

Competency represents demonstrated capability within a defined learning scope.

Established rules include:

```text
User + Scope
    →
one Competency
```

and:

```text
UNASSESSED
    ↓
ASSESSED
```

A valid passed Level Assessment may establish or increase Current Level.

A failed Assessment does not automatically downgrade an established level.

The final Aggregate boundary remains deferred until implementation requires it.

---

## 14.2. Current Level

Current Level is the CEFR proficiency classification established for a Competency.

It is not:

```text
Course Level
```

and not:

```text
Certification Level
```

The existing:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

provides the CEFR value baseline.

Current Level does not require an independent identity or repository.

---

## 14.3. Evidence to Learner State

Do not implement a generic:

```text
Evidence
    ↓
Learner State
```

pipeline.

For the first implementation, only explicit business rules should update learner state.

For example:

```text
Passed Level Assessment
        ↓
Assessment Result
        ↓
Competency
        ↓
Current Level
```

Ordinary Course completion or ordinary Quiz scores should not automatically establish Current Level.

---

# 15. Phase 8 — Learning Direction

Learning Direction is recognized as a business responsibility but remains deferred unless required by the current V3 implementation scope.

Potential concepts include:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

No implementation should be created merely to complete the conceptual model.

Implementation begins only when a concrete product requirement requires one of these capabilities.

---

# 16. Application Layer Implementation

Application Services should coordinate use cases.

The general pattern is:

```text
Input
  ↓
Application Service
  ↓
Load Aggregate(s)
  ↓
Invoke Domain Behavior
  ↓
Persist through Ports
  ↓
Return Result
```

Application Services may coordinate multiple Aggregates.

For example:

```text
Complete Lesson
```

may coordinate:

```text
LessonCompletion
+
Enrollment.Progress
```

without merging their boundaries.

---

## 16.1. Application Business Rules

Application Services should not become the default location for domain invariants.

Use the following distinction:

```text
Domain rule
    →
Domain model

Use-case orchestration
    →
Application Service
```

When ownership is unclear, resolve the business responsibility before moving code.

---

# 17. Persistence Implementation

Persistence should follow the established Ports & Adapters structure.

The intended dependency is:

```text
Application
    ↓
Repository Port
    ↑
Infrastructure Adapter
    ↓
Database
```

Infrastructure-specific details should remain outside the Domain.

---

## 17.1. Repository Creation Rule

Do not create repositories for every domain object automatically.

A repository is justified when the object:

* has independent lifecycle;
* is an Aggregate Root;
* or has a concrete application/persistence requirement.

For example:

```text
Course
Enrollment
Quiz
QuizAttempt
```

have clear Aggregate Root responsibilities.

An internal Entity such as:

```text
Lesson
Question
Answer
```

does not automatically require its own repository.

---

# 18. Testing and Verification

Every implementation slice must define how correctness will be verified.

Verification should occur at the smallest useful level.

Possible levels:

```text
Domain unit test
Application test
Integration test
API test
```

The goal is not to maximize test quantity.

The goal is to verify:

* business invariants;
* state transitions;
* application behavior;
* persistence behavior where necessary;
* architectural dependency constraints.

---

## 18.1. Domain Verification

Domain tests should focus on business rules.

Examples:

```text
Published Course cannot be modified
Invalid Lesson order is rejected
Enrollment lifecycle is respected
Invalid Progress is rejected
Quiz revision lifecycle is respected
QuizAttempt binds the correct revision
Current Level cannot decrease
```

---

## 18.2. Application Verification

Application tests should verify orchestration.

Examples:

```text
Complete Lesson
Submit Quiz Attempt
Start Assessment Attempt
Submit Assessment
Establish Competency
```

Only implement tests for use cases that actually exist.

---

# 19. Commit Strategy

Each meaningful implementation slice should normally produce a focused commit.

Examples:

```text
fix(course): correct lesson order update
```

```text
fix(course): validate incoming section update values
```

```text
refactor(architecture): remove spring dependency from domain resolver
```

```text
feat(quiz): implement quiz attempt submission
```

The commit should describe the actual completed change.

Avoid combining:

```text
unrelated fixes
architecture cleanup
new feature
formatting
```

into one implementation slice.

---

# 20. Milestone Strategy

A milestone is a coherent group of completed implementation slices.

Example:

```text
Course Stabilization Milestone
```

may contain:

```text
Slice 1 — Lesson order
Slice 2 — Section update validation
Slice 3 — Course publication rules
Slice 4 — Course verification
```

After the milestone:

```text
Review accumulated decisions
        ↓
Check target impact
        ↓
Update affected target documents
```

This is the main mechanism for preventing continuous target-document churn.

---

# 21. Target Document Update Policy

Target documents are updated only when one of the following changes:

### Domain Model

A confirmed business concept or relationship changes.

Update:

```text
target-domain-model.md
```

---

### Aggregate Boundary

An Aggregate Root, child boundary, or consistency boundary changes.

Update:

```text
target-aggregate-boundaries.md
```

---

### Module Boundary

A business responsibility or internal module boundary changes.

Update:

```text
target-module-boundaries.md
```

---

### Architecture

A dependency rule, layer responsibility, architectural style, or context boundary changes.

Update:

```text
target-architecture.md
```

---

### No Impact

If the change is only:

* a bug fix;
* implementation detail;
* test improvement;
* adapter correction;
* naming correction;
* internal optimization;

and does not alter the target model:

```text
Do not update target documents.
```

---

# 22. Handling New Domain Concepts

When implementation reveals a new concept, use this sequence:

```text
New Concept
    ↓
Does it represent a real business responsibility?
    │
    ├── No
    │    ↓
    │  Do not model it as a domain concept
    │
    └── Yes
         ↓
Does the current slice require it?
         │
         ├── No
         │    ↓
         │  Record as deferred
         │
         └── Yes
              ↓
          Domain Analysis
              ↓
          Business Decision
              ↓
          Implementation
```

The existence of a new class requirement does not automatically justify:

```text
new Aggregate
new module
new bounded context
new repository
new architecture layer
```

---

# 23. Handling Unexpected Findings During Implementation

Implementation is allowed to challenge the target model.

For example:

```text
Target Model
      ↓
Implementation
      ↓
Concrete inconsistency discovered
```

The correct response is:

```text
Stop affected slice
      ↓
Document concrete finding
      ↓
Re-evaluate domain decision
      ↓
Update only affected model
      ↓
Continue implementation
```

Do not perform broad refactoring simply because one assumption was wrong.

---

# 24. Definition of Done for an Implementation Slice

A slice is complete when:

```text
[ ] Business objective is clear

[ ] Relevant domain decision is known

[ ] Code change is limited to the required scope

[ ] Domain invariants are preserved

[ ] Dependency direction remains valid

[ ] Tests / verification pass

[ ] No unrelated refactoring was introduced

[ ] Target documents were not changed unless target impact exists

[ ] Commit represents the completed slice
```

---

# 25. Definition of Done for a Milestone

A milestone is complete when:

```text
[ ] All planned slices are implemented

[ ] Relevant behavior is verified

[ ] No known implementation blocker remains

[ ] Domain decisions discovered during implementation are recorded

[ ] Target impact has been reviewed

[ ] Only affected target documents are synchronized

[ ] Implementation plan is updated
```

---

# 26. Current V3 Implementation Order

Based on the current backend and established domain decisions, the implementation order is:

```text
1. Architectural concrete fixes
        ↓
2. Course stabilization
        ↓
3. Enrollment / Progress stabilization
        ↓
4. Learning Evidence stabilization
        ↓
5. Quiz / QuizAttempt implementation
        ↓
6. Assessment implementation
        ↓
7. Learner State implementation
        ↓
8. Learning Direction when required
```

This order is not a requirement to implement every concept in each phase completely.

Each phase is divided into small slices.

---

# 27. Current Implementation Priorities

The immediate priorities are:

## Priority 1 — Concrete Architecture Issue

```text
MediaTypeResolver
```

Remove the confirmed framework dependency from the Domain.

---

## Priority 2 — Existing Course Defects

Fix confirmed issues in:

```text
Lesson.changeOrderIndex
Section.update
```

without redesigning the Course Aggregate.

---

## Priority 3 — Enrollment / Progress

Verify the existing:

```text
Enrollment
    └── Progress
```

flow and identify the actual runtime usage of:

```text
UserProgress
```

before deciding its final fate.

---

## Priority 4 — Quiz

Implement only the established Quiz / QuizAttempt behavior required by V3.

Do not generalize Quiz rules to Assessment unless explicitly decided.

---

## Priority 5 — Assessment

Implement Assessment incrementally after the required business decisions are closed.

---

## Priority 6 — Learner State

Implement:

```text
Competency
Current Level
```

only after the required evidence-to-state rules are sufficiently defined.

---

# 28. What Is Explicitly Not an Immediate Implementation Target

The following are not implementation targets merely because they appear in the broader domain model:

```text
XP
Streak
Achievement
Recommendation
Learning Plan
Review Due
Learning Goal
Exam Preparation
```

They remain deferred until concrete V3 requirements require them.

Similarly, unresolved structural questions such as:

```text
Assessment Aggregate boundary
Competency Aggregate boundary
Learning Activity Aggregate boundary
Assessment ↔ Quiz structural relationship
```

remain deferred unless they block an implementation slice.

---

# 29. Final Implementation Model

The overall development loop is:

```text
             DOMAIN
                │
                ▼
       Business Decision
                │
                ▼
       Implementation Slice
                │
                ▼
             CODE
                │
                ▼
          TEST / VERIFY
                │
                ▼
             COMMIT
                │
                ▼
           NEXT SLICE
                │
                ▼
            MILESTONE
                │
                ▼
         TARGET IMPACT AUDIT
                │
        ┌───────┴────────┐
        │                │
      No Impact       Impact
        │                │
        ↓                ↓
   Keep target       Sync affected
     unchanged       target docs
        │                │
        └───────┬────────┘
                ↓
          NEXT MILESTONE
```

The key principle is:

> **Local domain decisions may evolve continuously; target architecture and boundary documents are synchronized at milestones when their accumulated impact is known.**

The implementation process therefore avoids both extremes:

```text
Extreme 1:
Never update target documents

Extreme 2:
Update every target document after every small decision
```

The intended approach is:

```text
Local First
     ↓
Implement
     ↓
Verify
     ↓
Milestone
     ↓
Target Synchronization
```

---

# 30. Final Rule

The project should always prefer:

```text
Small justified change
```

over:

```text
Large speculative refactor
```

and:

```text
Concrete evidence
```

over:

```text
Architectural preference
```

and:

```text
Explicit business decision
```

over:

```text
Assumption
```

The target architecture is a guide.

The implementation is the validation mechanism.

Neither should become an excuse to redesign parts of the system that have no concrete problem.

````
