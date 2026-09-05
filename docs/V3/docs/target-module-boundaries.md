# Target Module Boundaries

## 1. Purpose

This document defines the target business module boundaries for DeutschHub V3 within the Modular Monolith.

The purpose is to make major business responsibilities explicit while preserving the architectural foundations established in `target-architecture.md`.

This document does not define:

- the final package structure;
- database boundaries;
- API boundaries;
- Aggregate implementations;
- deployment boundaries;
- every future domain object.

Instead, it defines which business responsibilities belong together and where their boundaries should be maintained.

The module boundaries are derived from the domain analysis and target domain model rather than from existing database tables or technical package structures.

---

## 2. Boundary Principles

The target module structure follows several principles.

### 2.1 Business Responsibility Defines Module Boundaries

A module represents a coherent business responsibility.

A module should not exist merely because a set of classes shares a technical type.

For example:

```text
Course
Section
Lesson
LessonItem
````

belong to Learning Structure because they collectively represent the organization of learning content.

---

### 2.2 Module Does Not Equal Aggregate

A business module may contain multiple Aggregates.

For example:

```text
Learning Evidence
├── LessonCompletion
└── QuizAttempt
```

The existence of a shared business responsibility does not require these concepts to belong to the same Aggregate.

Similarly:

```text
Learning Structure
└── Course Aggregate
```

does not imply that every future learning-structure concept must become part of the Course Aggregate.

---

### 2.3 Module Does Not Equal Bounded Context

The business responsibilities defined in this document are internal boundaries within the Learning domain.

They do not automatically represent separate Bounded Contexts.

The target Learning Context remains:

```text
Learning
```

with multiple internal business responsibilities.

A responsibility should become a separate Bounded Context only when its language, model, ownership, and interaction boundaries justify such separation.

No such decision is made by this document.

---

### 2.4 Module Does Not Equal Database Structure

Database tables and foreign-key relationships must not determine module boundaries automatically.

For example:

```text
lesson_completions
```

does not imply that LessonCompletion must become an independent technical module.

Likewise, the fact that Enrollment references Course does not imply that Course and Enrollment belong to the same business boundary.

Business responsibility and consistency requirements remain the primary criteria.

---

# 3. High-Level Target Structure

The Learning Context is organized around the following business responsibilities:

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

These responsibilities form the target internal boundary model of Learning.

They should not be interpreted as six independent Bounded Contexts.

---

# 4. Learning Structure

## 4.1 Responsibility

Learning Structure is responsible for representing what can be learned and how learning content is organized.

It defines the structural organization through which learning resources are presented to learners.

The currently established structure is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

---

## 4.2 Current Domain Representation

The current Course Aggregate is located at:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

with supporting entities:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The current implementation therefore provides a concrete foundation for the Learning Structure responsibility.

---

## 4.3 Aggregate Boundary

The established Course Aggregate remains:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

This boundary should not be split without a concrete business consistency requirement.

The module boundary therefore contains the Course Aggregate but is not defined exclusively by the Course Aggregate.

---

## 4.4 Future Scope

Domain discovery indicates that Learning Structure may eventually include additional structures such as:

* vocabulary learning structures;
* grammar learning structures;
* language skill structures;
* levels;
* topics;
* specialized learning areas.

These concepts are target capabilities or areas of responsibility.

They are not automatically separate Aggregates or modules.

---

# 5. Enrollment

## 5.1 Responsibility

Enrollment is responsible for representing a learner's participation in a learning structure, particularly course participation.

Its responsibility includes:

* enrollment lifecycle;
* participation status;
* course-scoped progress;
* completion state associated with enrollment.

---

## 5.2 Current Domain Representation

The current Aggregate Root is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

The Aggregate contains:

```text
Enrollment
└── Progress
```

where `Progress` is represented as a Value Object.

---

## 5.3 Boundary

Enrollment is intentionally separated from Learning Structure.

The distinction is:

```text
Learning Structure
    = What can be learned?

Enrollment
    = Who is participating in what?
```

Course and Enrollment are related, but their business responsibilities are different.

The fact that Enrollment references a Course does not make Course and Enrollment a single Aggregate or module.

---

## 5.4 Progress

Course-scoped Progress belongs to the Enrollment responsibility.

It should not be interpreted as the complete state of the learner.

The target distinction is:

```text
Enrollment.Progress
    ≠
Learner State
```

---

# 6. Learning Activities

## 6.1 Responsibility

Learning Activities represent actions or learning interactions performed by a learner.

They exist for purposes such as:

* learning;
* practicing;
* reviewing;
* listening;
* speaking;
* reading;
* writing;
* demonstrating knowledge or skills.

---

## 6.2 Distinction from Learning Structure

The target architecture explicitly distinguishes:

```text
LessonItem
≠
Learning Activity
```

A LessonItem primarily represents content or a learning resource within a Lesson.

A Learning Activity represents what the learner does with or through that learning resource.

For example:

```text
LessonItem
    = vocabulary exercise content

Learning Activity
    = learner performs the vocabulary exercise
```

The exact implementation relationship depends on the future domain model.

---

## 6.3 Current State

The current Learning implementation does not contain a complete generic Learning Activity model.

Existing concepts include:

```text
LessonItem
Quiz
QuizAttempt
CompleteLessonService
```

These provide partial foundations but do not establish a generic Learning Activity Aggregate.

Therefore, the target boundary is established at the business-responsibility level while its internal domain model remains open.

---

## 6.4 Aggregate Decision

No generic `LearningActivity` Aggregate is established.

Different activity types may have different lifecycles and consistency requirements.

The implementation should therefore avoid creating a generic Aggregate merely to unify activity terminology.

---

# 7. Learning Evidence

## 7.1 Responsibility

Learning Evidence represents observable outcomes produced by learner activities or assessment interactions.

The central question answered by Evidence is:

```text
What happened?
```

Examples include:

```text
LessonCompletion
QuizAttempt
```

---

## 7.2 LessonCompletion

The current entity is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

It represents observable evidence that a learner completed a specific lesson.

It is treated as an independent Evidence entity rather than a child entity of Enrollment.

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

## 7.3 Assessment Evidence

The current assessment model includes:

```text
Quiz
QuizAttempt
```

`Quiz` represents the assessment definition.

`QuizAttempt` represents a learner's attempt and may serve as assessment-derived Learning Evidence.

The target relationship is:

```text
Quiz
    = assessment definition

QuizAttempt
    = learner attempt
    = assessment evidence
```

Quiz and QuizAttempt remain distinct Aggregates.

---

## 7.4 Evidence Does Not Equal Learner State

Learning Evidence must remain distinct from Learner State.

```text
Evidence
    = observable event or outcome

Learner State
    = current representation of what is known about the learner
```

Conceptually:

```text
Learning Activity
        ↓
Learning Evidence
        ↓
Learner State
```

The exact rules for transforming Evidence into Learner State remain open.

---

# 8. Learner State

## 8.1 Responsibility

Learner State represents what the system knows about the learner's learning condition, progress beyond a specific course, and demonstrated capabilities.

The business question is:

```text
What do we currently know about this learner?
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

These concepts are not required to share one Aggregate.

---

## 8.2 Relationship with Enrollment

Learner State is intentionally separated from Enrollment.

```text
Enrollment
    = participation in a specific learning structure

Learner State
    = broader state of the learner across learning experiences
```

Therefore:

```text
Course Progress
    ≠
Learner State
```

---

## 8.3 UserProgress

The current:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

does not represent the target Learner State boundary.

Its current scope overlaps with:

```text
Enrollment
Progress
```

and is strongly associated with a specific course and enrollment.

The target architecture therefore does not use `UserProgress` as the canonical Learner State model.

Future implementation should introduce learner-state concepts based on explicit domain decisions rather than extending the current `UserProgress` model indefinitely.

---

## 8.4 Competency

Competency represents demonstrated capability or knowledge.

It is intentionally distinct from:

```text
Progress
Evidence
Assessment Score
```

Conceptually:

```text
Evidence
    ↓
demonstrated capability
    ↓
Competency
```

The exact Aggregate boundary of Competency remains open.

---

## 8.5 Current Level

Current Level represents the learner's current language level.

It is distinct from:

```text
Course Level
Certification Level
```

The target model does not assume that:

```text
Course Completion
        =
Current Level
```

or that:

```text
Assessment Score
        =
Current Level
```

without explicit domain rules.

The existing `CEFRLevel` value object provides level classification, but the complete learner-level state model remains open.

---

# 9. Learning Direction

## 9.1 Responsibility

Learning Direction represents what the learner should do next.

The business question is:

```text
What should this learner do next?
```

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

---

## 9.2 Relationship with Learner State

Learning Direction consumes information about Learner State.

The conceptual relationship is:

```text
Learner State
      ↓
Learning Direction
      ↓
Next Learning Activity
```

Learning Direction does not own the learner's state.

---

## 9.3 Persistence and Aggregate Boundary

No generic `LearningDirection` Aggregate is established.

Different capabilities may have different business semantics.

For example:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
```

may eventually require different models or persistence strategies.

The target module boundary is therefore established without prematurely deciding the internal Aggregate structure.

---

# 10. Assessment Within Learning

Assessment is an important Learning capability but is not currently established as a separate Bounded Context.

The current domain model contains:

```text
Quiz
QuizAttempt
```

with the following distinction:

```text
Quiz
    = defines an assessment

QuizAttempt
    = represents a learner's assessment attempt
```

Assessment interacts strongly with Learning Evidence and Learner State:

```text
Quiz
   ↓
QuizAttempt
   ↓
Learning Evidence
   ↓
Learner State
```

The target architecture therefore keeps Assessment within the broader Learning boundary unless future business requirements justify a separate boundary.

---

# 11. Certification Within Learning

Certification is conceptually distinct from learner state.

Certification may eventually represent:

```text
certificates
certification requirements
certification status
certification levels
```

The current implementation contains domain concepts related to certification, but the complete application and persistence capability is not established.

Therefore:

* Certification remains a recognized Learning capability.
* It is not currently established as an independent Bounded Context.
* It must not be used as the definition of Learner Current Level.
* Its future module boundary remains open until its business rules are sufficiently understood.

---

# 12. Relationships Between Learning Modules

The target business relationships can be represented as:

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

This represents a business flow, not a direct technical dependency graph.

The technical dependency direction remains governed by `target-architecture.md`.

---

# 13. Module Interaction Principles

## 13.1 Structure Does Not Own Learner State

Learning Structure defines what can be learned.

It does not own:

```text
Competency
Current Level
Streak
XP
```

---

## 13.2 Enrollment Does Not Own Global Learner State

Enrollment owns participation in a particular learning structure and its associated course-scoped progress.

It does not become the owner of the learner's complete state.

---

## 13.3 Evidence Does Not Automatically Own State

Evidence records what happened.

It may contribute to learner state, but Evidence and Learner State remain separate responsibilities.

---

## 13.4 Direction Does Not Own State

Learning Direction uses learner information to determine appropriate next actions.

It does not become the canonical storage location for learner state.

---

## 13.5 Activity Does Not Automatically Equal Evidence

An activity represents an interaction.

Evidence represents an observable outcome.

One activity may produce evidence, but the concepts should not be collapsed by default.

---

# 14. Target Module Model

The current target model can therefore be summarized as:

```text
Learning Context
│
├── Learning Structure
│   └── Course Aggregate
│
├── Enrollment
│   └── Enrollment Aggregate
│       └── Progress
│
├── Learning Activities
│
├── Learning Evidence
│   ├── LessonCompletion
│   └── Assessment Evidence
│       └── QuizAttempt
│
├── Learner State
│   ├── Competency
│   ├── Current Level
│   ├── Vocabulary State
│   ├── Grammar State
│   └── Skill State
│
└── Learning Direction
    ├── Learning Plan
    ├── Review
    ├── Recommendation
    ├── Learning Goal
    └── Exam Preparation
```

This is a **business boundary model**, not a final package tree.

---

# 15. Confirmed Module Boundaries

The following boundaries are established at the business-responsibility level:

| Boundary            | Responsibility                                   | Status                             |
| ------------------- | ------------------------------------------------ | ---------------------------------- |
| Learning Structure  | What can be learned and how it is organized      | Confirmed                          |
| Enrollment          | Learner participation in learning structures     | Confirmed                          |
| Learning Activities | Learner interactions and learning actions        | Confirmed as target responsibility |
| Learning Evidence   | Observable learning outcomes                     | Confirmed                          |
| Learner State       | What is known about learner state and capability | Confirmed as target responsibility |
| Learning Direction  | What the learner should do next                  | Confirmed as target responsibility |

These boundaries do not imply independent Bounded Contexts.

---

# 16. Open Internal Modeling Decisions

The following concepts belong to established target responsibilities but do not yet have final internal boundaries.

### Learning Activities

* Generic activity model or activity-specific models.
* Persistence requirements.
* Relationship between activity and learning content.
* Activity lifecycle.

### Learner State

* Competency Aggregate boundary.
* Current Level Aggregate boundary.
* Vocabulary State model.
* Grammar State model.
* Skill State model.
* Persisted versus derived state.
* XP, Streak, Achievement, and Statistics models.

### Learning Direction

* Learning Plan model.
* Recommendation model.
* Review Due model.
* Learning Goal model.
* Exam Preparation model.
* Persisted versus derived direction.

### Evidence

* Evidence aggregation rules.
* Evidence-to-Competency rules.
* Evidence-to-Level rules.
* Additional evidence types.

These decisions should be made when the relevant business rules are sufficiently understood.

---

# 17. Relationship to Existing Architecture

The target module boundaries do not require an immediate replacement of the existing:

```text
Domain
Application
Infrastructure
```

structure.

The architectural layers remain governed by Hexagonal principles.

Business boundaries should become increasingly visible within the existing modular structure as implementation evolves.

The desired relationship is:

```text
Business Module
        +
Hexagonal Layers
```

rather than choosing one over the other.

---

# 18. Implementation Guidance

When implementation begins, module boundaries should be introduced incrementally.

Existing working components should remain where their current responsibility is already clear.

Changes should be made when:

* an existing component crosses a documented business boundary;
* a domain concept has been assigned a new responsibility;
* an existing model creates concrete duplication;
* a framework dependency violates the target dependency rules;
* or a new feature requires a boundary that is already supported by the domain model.

No module should be created solely to satisfy a naming convention.

No Aggregate should be created solely because a module exists.

No Bounded Context should be created solely because a business responsibility has been identified.

---

# 19. Summary

DeutschHub V3 retains Learning as a major business context within the Modular Monolith.

Inside Learning, the target architecture recognizes six major business responsibilities:

```text
Learning Structure
Enrollment
Learning Activities
Learning Evidence
Learner State
Learning Direction
```

These responsibilities provide a clearer model of Learning than the current predominantly Course-centered organization.

The target structure preserves established domain boundaries:

```text
Course
Enrollment
Quiz
QuizAttempt
LessonCompletion
```

while recognizing that additional learner-centered capabilities will be introduced over time.

The central principle is:

> **Business responsibilities define module boundaries, while Aggregate boundaries are defined by consistency requirements and Bounded Contexts are defined by larger domain-language and responsibility boundaries.**

The target module structure therefore provides direction without prematurely fixing every Aggregate, entity, value object, package, persistence model, or Bounded Context.

