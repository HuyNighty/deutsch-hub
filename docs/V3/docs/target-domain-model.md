# Target Domain Model

## 1. Purpose

This document defines the target domain model for the Learning Context in DeutschHub V3.

It is derived from:

- the current implementation;
- the current application and persistence flows;
- previous domain analysis;
- established domain decisions;
- and subsequent business analysis of Learning Evidence, Assessment, Competency, and Current Level.

This document describes the target business concepts, responsibilities, relationships, and domain boundaries of the Learning Context.

It is **not** a final database schema, API contract, or package structure.

The model is intended to provide a stable domain foundation for subsequent implementation decisions while explicitly preserving unresolved areas as **OPEN** or **DEFERRED** rather than introducing unsupported assumptions.

The target Learning Context covers the following major business responsibilities:

1. Learning Structure
2. Enrollment
3. Learning Activities
4. Learning Evidence
5. Learner State
6. Learning Direction

Assessment is a distinct domain concept within the Learning Context.

It provides structured evaluation of learner performance and may produce Learning Evidence that contributes to Learner State.

The boundaries described in this document are business/domain boundaries.

They do not automatically imply:

- one Aggregate Root per responsibility;
- one database table per concept;
- one bounded context per responsibility;
- or one Java module/package per concept.

---

# 2. Learning Context

The target Learning Context is broader than Course Management.

It represents the domain responsibility for supporting the learner's learning journey.

Conceptually:

```text
Learning Context
│
├── Learning Structure
│
├── Enrollment
│
├── Learning Activities
│
├── Learning Evidence
│
├── Learner State
│
└── Learning Direction
````

The target learning loop is:

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

This loop describes the relationship between the major responsibilities.

It does not imply that every Activity must produce Evidence, that every Evidence record changes Learner State, or that every Learner State change immediately produces a Learning Direction.

---

# 3. Learning Structure

## 3.1 Responsibility

Learning Structure defines the organized learning content through which a learner can progress.

The currently established structure is:

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

---

## 3.2 Course

**Status: Confirmed Aggregate Root**

`Course` represents the structured learning course.

The target boundary is:

```text
Course Aggregate
└── Course
    └── Section
        └── Lesson
            └── LessonItem
```

`Course` remains the Aggregate Root responsible for the consistency of its course structure.

---

## 3.3 Section

**Status: Confirmed Entity**

A `Section` belongs to a Course and organizes Lessons within the Course.

It does not have an independent Aggregate boundary.

---

## 3.4 Lesson

**Status: Confirmed Entity**

A `Lesson` belongs to a Section and organizes LessonItems.

It does not have an independent Aggregate boundary.

---

## 3.5 LessonItem

**Status: Confirmed Entity**

A `LessonItem` belongs to a Lesson.

The current implementation contains LessonItem types such as:

```text
TEXT
MEDIA
QUIZ
```

`LessonItem` represents a structured content element within a Lesson.

It must not automatically be treated as the generic representation of every Learning Activity.

Therefore:

```text
LessonItem
    ≠
Learning Activity
```

The exact relationship between LessonItem and future Learning Activities remains a separate domain concern.

---

## 3.6 Course Level

Course Level describes the level associated with course content.

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

This is a content/classification concept.

It must not be used to represent a learner's Current Level.

Therefore:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

---

# 4. Enrollment

## 4.1 Responsibility

Enrollment represents a learner's participation in a specific Course.

**Status: Confirmed Aggregate Root**

Current implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

The target boundary is:

```text
Enrollment Aggregate
└── Enrollment
    └── Progress
```

---

## 4.2 Enrollment Lifecycle

The established lifecycle is:

```text
ENROLLED
    ↓
ACTIVE
    ↓
COMPLETED
```

Additional lifecycle states may exist where already established by the domain model, but no new lifecycle state is introduced here without supporting business requirements.

---

## 4.3 Enrollment and Course

Enrollment represents participation in a Course.

Conceptually:

```text
Course
    ↑
    │
Enrollment
    │
    ↓
Learner
```

Enrollment does not become a child entity of the Course Aggregate.

A Course can have many Enrollments.

---

# 5. Progress

## 5.1 Responsibility

Progress represents the learner's advancement within a specific enrolled Course.

**Status: Confirmed Value Object**

Current implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

The target relationship is:

```text
Enrollment
    └── Progress
```

---

## 5.2 Scope

Progress is Course-scoped.

It answers questions such as:

```text
How far has this learner progressed through this Course?
```

It does not represent the learner's complete learning state.

Therefore:

```text
Course Progress
    ≠
Learner State
```

and:

```text
Course Progress
    ≠
Competency
```

---

## 5.3 Progress and Evidence

Learning Evidence may cause or support a change in Course Progress.

For example:

```text
LessonCompletion
        ↓
Enrollment.Progress
```

However, the two concepts remain distinct.

```text
Evidence
    ≠
Progress
```

---

# 6. Learning Activities

## 6.1 Responsibility

Learning Activity represents a learner-facing learning action.

A Learning Activity is the action through which the learner performs, practices, studies, or is evaluated.

It is not automatically equivalent to:

* LessonItem;
* content;
* UI component;
* Quiz;
* or any single current implementation class.

Therefore:

```text
Learning Activity
    ≠
LessonItem
```

---

## 6.2 Relationship to Learning Structure

A Course or Lesson may provide or organize Learning Activities.

Conceptually:

```text
Course Structure
      ↓
Learning Activity
```

However, a Learning Activity is not necessarily owned by the Course Aggregate.

A Learning Activity may also exist outside a Course-based flow.

---

## 6.3 Activity and Evidence

A Learning Activity may produce Learning Evidence.

Conceptually:

```text
Learning Activity
        ↓
Learning Evidence
```

However, not every Activity must produce Evidence.

Evidence should be produced when the Activity creates meaningful, domain-relevant information about:

* learner performance;
* completion;
* assessment outcome;
* or another observable learning result.

---

## 6.4 Boundary Status

**Status: Domain Concept — Structural Boundary OPEN / DEFERRED**

The current implementation does not provide sufficient evidence to establish a generic `LearningActivity` Aggregate.

The exact structural relationship between:

```text
LessonItem
Quiz
Assessment
Practice
Review
```

and Learning Activity remains open.

No generic LearningActivity Aggregate is introduced at this stage.

---

# 7. Assessment

## 7.1 Responsibility

Assessment is a structured evaluation of learner performance.

Assessment is broader than Quiz.

An Assessment may evaluate one or more skill dimensions according to its purpose.

Examples of skill dimensions include:

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

## 7.2 Assessment Structure

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

A Component is tied to one Skill Dimension.

For example:

```text
Assessment
│
├── Listening Component
│     ├── Task 1
│     └── Task 2
│
├── Reading Component
│     ├── Task 1
│     ├── Task 2
│     └── Task 3
│
└── Speaking Component
      └── Task 1
```

An Assessment does not need to contain all possible dimensions.

---

## 7.3 Assessment Component

An Assessment Component groups Tasks belonging to one Skill Dimension.

**Confirmed rule:**

```text
One Component
    ↓
One Skill Dimension
```

A Component may contain multiple Tasks.

A Component Result may be produced when evaluation of the Component is complete.

---

## 7.4 Assessment Task

A Task is an executable part of an Assessment Component.

One Component may contain multiple Tasks.

Different Tasks may use different evaluation mechanisms.

The exact evaluation mechanism is intentionally not fixed at the Target Domain Model level.

Examples may include:

```text
automatic evaluation
manual evaluation
structured response evaluation
```

but no specific mechanism is assumed unless required by the Assessment implementation.

---

## 7.5 Evaluation Mechanism

The Evaluation Mechanism defines how a Task or Component is evaluated.

Its exact domain representation remains OPEN.

The target model only establishes that:

```text
Task
    ↓
Evaluation Mechanism
    ↓
Component Result
```

Different Tasks may use different mechanisms.

---

## 7.6 Component Result

A Component Result represents the result of evaluating an Assessment Component.

It is historical information belonging to a specific Assessment Attempt.

Conceptually:

```text
Assessment Attempt
        ↓
Component Result(s)
```

A Component Result does not automatically constitute the official Assessment Result.

---

## 7.7 Completion Policy

Completion Policy determines when an Assessment Attempt is considered complete and when an official Assessment Result may be created.

The policy belongs to the Assessment.

A learner may submit an Assessment even when some Components or Tasks have not been performed.

Unperformed Components are not treated as an invalid execution merely because no response was provided.

They are processed according to the Assessment's Completion Policy and Evaluation Rules.

---

## 7.8 Time Limit

An Assessment may define a Time Limit.

The Time Limit determines when an Assessment Attempt must end because the allowed execution time has elapsed.

Timeout itself does not determine whether an individual Component passes or fails.

When timeout occurs:

```text
Incomplete Components / Tasks
        ↓
Processed according to
Completion Policy + Evaluation Rules
        ↓
Assessment Result
```

Assessment timeout rules are analogous to the general assessment principle already established for Quiz, but Quiz-specific edge cases must not automatically be copied into the broader Assessment model.

---

## 7.9 Assessment Attempt

Each execution of an Assessment by a learner is a separate Assessment Attempt.

Conceptually:

```text
Assessment
    ├── Attempt A
    ├── Attempt B
    └── Attempt C
```

Each Attempt has its own identity and lifecycle.

---

## 7.10 Active Attempt

For a given:

```text
User + Assessment
```

there may be at most one active Assessment Attempt.

Multiple devices or browser sessions continuing the same execution still represent one Attempt.

Opening another device/session does not automatically create another Attempt.

---

## 7.11 Attempt Lifecycle

The target Assessment Attempt lifecycle is:

```text
IN_PROGRESS
     │
     ├── Submit
     │
     └── Timeout
           ↓
       COMPLETED
```

An IN_PROGRESS Attempt may be paused and resumed.

Closing a browser, losing a connection, or changing device does not automatically terminate the Attempt.

---

## 7.12 Stable Assessment Definition

An Assessment Attempt must bind to a stable Assessment definition/version for the duration of that Attempt.

Therefore:

```text
Assessment changes
        ↓
do not change
        ↓
an already-running Attempt
```

The exact Assessment Revision lifecycle and structural model remain OPEN.

The business requirement that an Attempt must remain bound to a stable definition/version is confirmed.

---

## 7.13 Assessment Result

Assessment Result is the official historical result of one Assessment Attempt.

Conceptually:

```text
Assessment Attempt
        ↓
Component Result(s)
        ↓
Assessment Result
```

The Assessment Result is created only when the Attempt satisfies the Assessment's completion conditions.

An Assessment Result belongs historically to the Attempt that produced it.

A later Attempt does not modify a previous Assessment Result.

---

## 7.14 Assessment Outcome

Assessment Result may have different outcome types depending on the purpose of the Assessment.

Therefore, Assessment Result must not be universally modeled as:

```text
PASS / FAIL
```

For a Level Assessment, for example:

```text
PASS
    +
Established Level
```

may be appropriate.

Other Assessment purposes may require different outcome semantics.

---

## 7.15 Assessment Attempt Limit

Assessment Attempt Limit is optional.

An Assessment may:

```text
limit attempts
```

or:

```text
allow unlimited attempts
```

The exact policies for:

* cooldown;
* paid retakes;
* attempt quotas;
* reset policies;

remain OPEN / DEFERRED unless required by a concrete V3 capability.

This is distinct from the already established Quiz-specific `maxAttempts` rules.

---

## 7.16 Quiz and Assessment

Quiz is an existing assessment-related domain concept.

The broader target model is:

```text
Assessment
    ↑
    │
Quiz may serve as
an assessment mechanism/activity/task
```

However, the exact structural relationship between:

```text
Quiz
Assessment
Task
Evaluation Mechanism
```

is not fully closed.

Therefore, the target model does **not** make the unsupported assertion:

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

The confirmed decision is only that Assessment is broader than the existing Quiz concept.

---

# 8. Learning Evidence

## 8.1 Responsibility

Learning Evidence represents an observable historical fact about a learner's learning activity, performance, completion, or assessment outcome.

The target relationship is:

```text
Learning Activity
        ↓
Learning Evidence
```

Evidence is historical.

It describes what happened.

---

## 8.2 Evidence and Learner State

Learning Evidence may contribute to Learner State.

Conceptually:

```text
Learning Evidence
        ↓
Evaluation / Interpretation
        ↓
Learner State
```

However:

```text
Learning Evidence
    ≠
Learner State
```

Evidence is an observable record.

Learner State represents the current state established by the system.

---

## 8.3 LessonCompletion

**Status: Confirmed Evidence Entity**

Current implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

The current application flow includes:

```text
Complete Lesson
      ↓
Create LessonCompletion
      ↓
Persist Completion
      ↓
Update Enrollment Progress
```

Current application service:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

The target conceptual relationship is:

```text
LessonCompletion
    = Lesson completion evidence

Progress
    = Course-scoped advancement state
```

Therefore:

```text
LessonCompletion
    ≠
Progress
```

---

## 8.4 QuizAttempt as Evidence

QuizAttempt represents a learner-specific assessment execution.

Current implementation:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

The target distinction is:

```text
Quiz
    = assessment definition

QuizAttempt
    = learner-specific assessment execution
```

QuizAttempt may also serve as Learning Evidence.

These are two different classifications:

```text
Aggregate Root
    → describes consistency ownership

Learning Evidence
    → describes business meaning
```

Therefore:

```text
QuizAttempt
    ≠
Competency
```

and:

```text
QuizAttempt Score
    ≠
Current Level
```

unless an explicit domain rule establishes such a relationship.

---

## 8.5 Question Result and Assessment Result

Question Result represents historical evaluation evidence for an individual question within an assessment execution.

Assessment Result represents the official result of the entire Assessment Attempt.

Conceptually:

```text
Assessment Attempt
    │
    ├── Question Result(s)
    │
    ├── Component Result(s)
    │
    └── Assessment Result
```

These concepts must remain distinct.

```text
Question Result
    ≠
Component Result
    ≠
Assessment Result
```

---

## 8.6 Evidence Immutability

Historical evidence must preserve what actually happened.

A later assessment attempt must not rewrite an earlier assessment result.

For example:

```text
Attempt 1 → PASSED
Attempt 2 → FAILED
```

does not change:

```text
Attempt 1 Result
```

Similarly, historical completion evidence must not be deleted merely because the learner's current state changes.

---

## 8.7 Evidence Boundary

**Status: Conceptual Category**

Learning Evidence is a business category rather than automatically one Aggregate Root.

Current evidence concepts have different boundaries:

```text
LessonCompletion
    → independent Evidence Entity

QuizAttempt
    → separate Aggregate Root
    → may serve as Evidence

Assessment Result
    → historical result of an Assessment Attempt
```

No generic `LearningEvidence` Aggregate is introduced.

---

# 9. Learner State

## 9.1 Responsibility

Learner State represents the current state that DeutschHub establishes about a learner.

It is broader than Course Progress.

Conceptually:

```text
Learner State
│
├── Competency
├── Current Level
└── Other learner-state concepts
```

Other concepts such as XP, streaks, achievements, statistics, vocabulary state, grammar state, or skill state may belong to this responsibility, but their V3 implementation status remains deferred unless required.

---

## 9.2 Learner State Is Not One Aggregate

**Status: Confirmed**

Learner State is a business responsibility/conceptual area.

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
Single Aggregate Root
```

---

# 10. Competency

## 10.1 Business Meaning

Competency represents a learner's demonstrated capability within a defined learning domain.

For DeutschHub, the current confirmed scope is:

```text
User
+
Competency Scope
```

For the current product, German Language is the relevant competency scope.

The model is intentionally broad enough to distinguish the concept from a specific CEFR level.

---

## 10.2 Competency and Scope

For a given:

```text
User + Competency Scope
```

there is at most one Competency.

Different scopes represent different Competencies.

Conceptually:

```text
User
│
├── Competency: German Language
│
└── Other Competency Scopes
```

The V3 implementation does not need to introduce multiple language competencies unless product requirements require them.

---

## 10.3 Competency Lifecycle

A Competency is created in an unassessed state.

Conceptually:

```text
UNASSESSED
    ↓
ASSESSED
```

The initial Current Level is:

```text
UNKNOWN
```

A valid passed Level Assessment establishes the corresponding proficiency level.

---

## 10.4 Competency and Level Assessment

A Level Assessment is authoritative for establishing the learner's Current Level within the relevant Competency Scope.

Conceptually:

```text
Level Assessment
        ↓
PASSED
        ↓
Established Level
        ↓
Competency.Current Level
```

A Level Assessment does not require Course completion as a prerequisite.

A learner may establish a level directly through a valid passed Level Assessment.

---

## 10.5 Non-Sequential Level Establishment

The learner does not have to pass every lower CEFR level first.

For example:

```text
UNKNOWN
   ↓
B2
```

is valid if the learner passes a valid Level Assessment establishing B2.

The domain does not require:

```text
A1 → A2 → B1 → B2
```

as a sequential progression.

---

## 10.6 Updating Competency

A higher valid passed Level Assessment may update the Competency's Current Level.

A lower-level passed Assessment does not decrease an already established higher level.

A failed Assessment does not automatically downgrade the Competency.

Therefore:

```text
Current Level
    never decreases
    solely because of
    a lower or failed assessment
```

---

## 10.7 Retaking Assessments

A learner may retake the same Level Assessment multiple times.

All attempts remain historical.

Any valid passed attempt is sufficient to establish the corresponding level.

A later failed attempt does not revoke or downgrade an already established level.

Conceptually:

```text
Attempt 1 → FAILED
Attempt 2 → PASSED B2
Attempt 3 → FAILED

Current Level = B2
```

---

## 10.8 Competency Invariants

The established invariants are:

1. For a given User + Competency Scope, at most one Competency exists.
2. An UNASSESSED Competency must not have an established Current Level.
3. UNKNOWN represents an unestablished Current Level.
4. A valid Assessment must not decrease an established Current Level.
5. An established Current Level must be supported by valid passed Level Assessment evidence for that level or a higher level.
6. Publishing a new Assessment Revision does not invalidate historical evidence.
7. Competency changes from UNASSESSED to ASSESSED only through a valid passed Level Assessment.

---

## 10.9 Competency and Evidence

Competency is distinct from Learning Evidence.

```text
Evidence
    = historical observable fact

Competency
    = demonstrated capability
```

Therefore:

```text
Assessment Result
    ≠
Competency
```

and:

```text
Assessment Score
    ≠
Competency
```

A valid passed Level Assessment may establish Competency according to explicit business rules.

Ordinary Course Quizzes, practice activities, or external quizzes do not automatically establish or change Competency.

---

## 10.10 Competency Aggregate Boundary

**Status: OPEN / DEFERRED**

Competency is a learner-state domain concept with identity and lifecycle.

However, the exact Aggregate boundary is not required to be fixed at this stage.

No independent Competency Aggregate Root is introduced until its transactional consistency requirements require one.

---

# 11. Current Level

## 11.1 Business Meaning

Current Level represents the current CEFR proficiency classification that DeutschHub has established for a learner's Competency based on valid passed Level Assessment evidence.

Conceptually:

```text
Competency
    └── Current Level
```

---

## 11.2 Current Level Is Not an Independent Entity

Current Level has no independent identity.

It does not require:

```text
CurrentLevelId
```

or an independent lifecycle.

It is a value/state belonging to Competency.

---

## 11.3 CEFR Representation

The existing:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java
```

provides the CEFR classification values:

```text
A1
A2
B1
B2
C1
C2
```

This is a useful representation baseline.

However:

```text
CEFRLevel
    ≠
Learner Current Level business concept
```

`CEFRLevel` represents the classification value.

Current Level represents that classification as the learner's currently established proficiency state.

---

## 11.4 Current Level Lifecycle

The conceptual state is:

```text
UNKNOWN
   ↓
A1 / A2 / B1 / B2 / C1 / C2
```

A learner may move directly from UNKNOWN to a higher level when supported by a valid passed Level Assessment.

---

## 11.5 Current Level Does Not Decrease

Current Level does not decrease because:

* the learner fails a later assessment;
* the learner passes a lower-level assessment;
* a newer Assessment Revision is published.

An explicit future reset/revoke rule may be introduced only if a concrete business requirement requires it.

No such mechanism is part of the current V3 target model.

---

## 11.6 Current Level and Certification

The following concepts remain distinct:

```text
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

Course completion does not automatically determine Current Level.

Certification does not automatically become Current Level unless an explicit domain rule establishes that relationship.

---

# 12. Learning Direction

## 12.1 Responsibility

Learning Direction represents the responsibility for determining or guiding what the learner should do next.

Conceptually:

```text
Learner State
      ↓
Learning Direction
      ↓
Next Learning Activity
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

## 12.2 Boundary Status

**Status: OPEN / DEFERRED**

The current implementation does not provide sufficient evidence to establish final Aggregate boundaries for Learning Direction.

No generic Learning Direction Aggregate is introduced.

---

## 12.3 Deferred Concepts

The following concepts remain deferred:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

Their exact classification may later be:

* persisted domain state;
* derived decision;
* application-level result;
* Entity;
* Aggregate;
* or another domain representation.

No final decision is made without a concrete business requirement.

---

# 13. UserProgress

## 13.1 Current Implementation

The current implementation contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

Its responsibilities overlap with existing Course-scoped learning state.

In particular:

```text
Enrollment.Progress
        vs
UserProgress.currentProgress
```

represent overlapping categories of Course progress.

---

## 13.2 Target Decision

`UserProgress` is **not retained as the target learner-state Aggregate**.

The target responsibilities are separated as:

```text
Enrollment + Progress
    → Course-scoped progress

LessonCompletion
    → Lesson completion evidence

Assessment / QuizAttempt
    → Assessment execution and evidence

Competency
    → Learner capability

Current Level
    → Established CEFR proficiency state
```

`UserProgress` should therefore not be treated as the source of truth for the target Learner State.

---

## 13.3 Migration / Refactoring

The exact migration or removal strategy for `UserProgress` is an implementation concern to be handled after the target domain boundaries are established.

This document does not prescribe a technical rewrite.

---

# 14. Important Domain Distinctions

The following distinctions are fundamental to the target model.

```text
Course
    ≠
Enrollment
```

```text
Enrollment.Progress
    ≠
Learner State
```

```text
Progress
    ≠
Learning Evidence
```

```text
Learning Evidence
    ≠
Learner State
```

```text
Progress
    ≠
Competency
```

```text
Competency
    ≠
Current Level
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
Course Level
    ≠
Learner Current Level
    ≠
Certification Level
```

```text
LessonItem
    ≠
Learning Activity
```

```text
Quiz
    ≠
QuizAttempt
```

```text
QuizAttempt
    ≠
Competency
```

```text
Historical Evidence
    ≠
Current Learner State
```

```text
Learning Direction
    ≠
Learner State
```

---

# 15. Target Aggregate Overview

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
│ └── UserAnswer(s)                │
│ └── QuestionResult(s)            │
│ └── Assessment Result            │
└──────────────────────────────────┘
```

```text
┌──────────────────────────────────┐
│ Learning Evidence                │
│                                  │
│ LessonCompletion                 │
└──────────────────────────────────┘
```

The following concepts do not yet have a finalized independent Aggregate boundary:

```text
Competency
Learning Activity
Learning Direction
```

Current Level is not treated as an independent Aggregate or Entity.

It belongs to Competency as learner-state value/state.

---

# 16. Target Domain Relationships

The target model can be represented conceptually as:

```text
                    LEARNING CONTEXT
                           │
          ┌────────────────┼─────────────────┐
          │                │                 │
          ↓                ↓                 ↓
 Learning Structure   Enrollment       Learning Activities
          │                │                 │
          │                ↓                 │
          │             Progress             │
          │                                  │
          └───────────────┬──────────────────┘
                          ↓
                  Learning Evidence
                    ┌─────┴─────┐
                    │           │
                    ↓           ↓
           LessonCompletion   Assessment
                                  │
                                  ↓
                           Assessment Attempt
                                  │
                           ┌──────┴──────┐
                           ↓             ↓
                    Component       Assessment
                     Result           Result
                                         │
                                         ↓
                                  Learner State
                                  ┌─────┴─────┐
                                  │           │
                                  ↓           ↓
                             Competency   Other State
                                  │
                                  ↓
                            Current Level
                                  │
                                  ↓
                         Learning Direction
                                  │
                                  ↓
                         Next Activity
```

This diagram describes business relationships.

It does not prescribe technical dependencies or Aggregate ownership.

---

# 17. Assessment and Learner State

The important target relationship is:

```text
Assessment
    ↓
Assessment Attempt
    ↓
Assessment Result
    ↓
Learning Evidence
    ↓
Competency
    ↓
Current Level
```

This does **not** mean every Assessment updates Competency.

Only a valid Level Assessment with the required outcome can establish or update Current Level.

For ordinary learning activities:

```text
Practice
Quiz
Lesson Completion
External Quiz
        ↓
Learning Evidence
        ↓
may support learning
```

but they do not automatically establish Competency or Current Level.

---

# 18. Historical Integrity

Historical learning facts must remain distinguishable from current learner state.

Examples:

```text
LessonCompletion
AssessmentAttempt
QuestionResult
ComponentResult
AssessmentResult
```

represent historical records.

Current learner state includes:

```text
Competency
Current Level
```

Therefore:

```text
Historical Evidence
        ≠
Current State
```

A later event may change current state without rewriting historical evidence.

For example:

```text
Level Assessment 1 → PASSED B2
Level Assessment 2 → FAILED

Historical:
Assessment 1 = PASSED B2
Assessment 2 = FAILED

Current:
Competency.CurrentLevel = B2
```

---

# 19. Target Model Classification

| Concept              | Target Classification              | Status                                 |
| -------------------- | ---------------------------------- | -------------------------------------- |
| Course               | Aggregate Root                     | Confirmed                              |
| Section              | Entity inside Course               | Confirmed                              |
| Lesson               | Entity inside Course               | Confirmed                              |
| LessonItem           | Entity inside Course               | Confirmed                              |
| Enrollment           | Aggregate Root                     | Confirmed                              |
| Progress             | Value Object                       | Confirmed                              |
| Learning Activity    | Domain Concept                     | OPEN / DEFERRED                        |
| LessonCompletion     | Evidence Entity                    | Confirmed                              |
| Quiz                 | Assessment-related Aggregate Root  | Confirmed                              |
| QuizRevision         | Entity inside Quiz                 | Confirmed                              |
| Question             | Entity inside QuizRevision         | Confirmed                              |
| Answer               | Entity inside Question             | Confirmed                              |
| QuizAttempt          | Aggregate Root / Learning Evidence | Confirmed                              |
| QuestionResult       | Assessment Evidence                | Confirmed concept                      |
| Component Result     | Assessment Evidence                | Confirmed concept                      |
| Assessment Result    | Historical Assessment Result       | Confirmed concept                      |
| Assessment           | Assessment Domain Concept          | Confirmed                              |
| Assessment Component | Assessment Concept                 | Confirmed                              |
| Assessment Task      | Assessment Concept                 | Confirmed                              |
| Competency           | Learner State Domain Concept       | Confirmed; Aggregate boundary deferred |
| Current Level        | State/Value of Competency          | Confirmed                              |
| Learner State        | Business Responsibility            | Confirmed; not a single Aggregate Root |
| CEFRLevel            | Value / classification             | Confirmed baseline                     |
| UserProgress         | Not retained as target aggregate   | Confirmed direction                    |
| Learning Direction   | Business Responsibility            | OPEN / DEFERRED                        |
| Learning Plan        | Learning Direction Concept         | Deferred                               |
| Recommendation       | Learning Direction Concept         | Deferred                               |
| Review Due           | Learning Direction Concept         | Deferred                               |
| Learning Goal        | Learning Direction Concept         | Deferred                               |
| XP                   | Learner State Concept              | Deferred                               |
| Streak               | Learner State Concept              | Deferred                               |
| Achievement          | Learner State Concept              | Deferred                               |
| Statistics           | Learner State / Derived Concept    | Deferred                               |

---

# 20. Confirmed V3 Decisions

The following decisions are currently considered established for the target model.

### Learning Structure

```text
Course
    → Aggregate Root

Section
Lesson
LessonItem
    → Entities inside Course Aggregate
```

### Enrollment

```text
Enrollment
    → Aggregate Root
```

### Progress

```text
Progress
    → Course-scoped Value Object
```

### Learning Evidence

```text
LessonCompletion
    → independent Evidence Entity

QuizAttempt
    → separate Aggregate Root
    → may serve as Learning Evidence
```

### Assessment

```text
Assessment
    → broader than Quiz

Assessment
    ├── Components
    │      └── Tasks
    ├── Completion Policy
    ├── Time Limit
    └── Attempt Rules
```

### Assessment Attempt

```text
One User + One Assessment
    → at most one active Attempt
```

An Attempt may be resumed across devices and sessions.

An Attempt remains bound to a stable Assessment definition/version.

### Assessment Result

```text
Assessment Attempt
    ↓
Assessment Result
```

The result is historical and belongs to that Attempt.

### Competency

```text
User + Competency Scope
    → one Competency
```

A passed Level Assessment can establish the corresponding Current Level.

A higher passed Level Assessment may increase Current Level.

A lower or failed Assessment does not automatically decrease Current Level.

### Current Level

```text
Competency
    └── Current Level
```

Current Level uses the applicable CEFR classification.

It has no independent identity or lifecycle.

### Fundamental Distinctions

```text
Evidence ≠ Learner State

Progress ≠ Competency

Assessment Result ≠ Competency

Course Level ≠ Current Level

Current Level ≠ Certification Level
```

---

# 21. OPEN / DEFERRED Decisions

The following decisions remain intentionally unresolved.

## 21.1 Assessment Structure

The exact relationship between:

```text
Assessment
Quiz
Task
Evaluation Mechanism
Learning Activity
```

remains OPEN at structural level.

The business distinction that Assessment is broader than Quiz is confirmed.

---

## 21.2 Assessment Revision

The Attempt must bind to a stable Assessment definition/version.

The exact Revision model and lifecycle remain OPEN.

---

## 21.3 Assessment Evaluation Mechanism

The exact domain model for:

```text
Evaluation Mechanism
```

remains OPEN.

Different Tasks may use different mechanisms.

---

## 21.4 Unanswered Component Result

The exact representation of a Component Result when a learner does not perform a Component remains OPEN.

The established rule is that an unperformed Component is processed according to the Assessment's Completion Policy and Evaluation Rules rather than being treated as an invalid Attempt.

---

## 21.5 Competency Aggregate Boundary

Competency is a confirmed learner-state concept.

Its final Aggregate boundary is deferred until transactional consistency and update requirements require the decision.

---

## 21.6 Evidence-to-Competency Interpretation

The target relationship is:

```text
Evidence
    ↓
may contribute to
    ↓
Competency
```

However, there is no generic rule that all evidence directly changes Competency.

Level Assessment is authoritative for establishing Current Level.

Other evidence may support learning without establishing Current Level.

---

## 21.7 Learning Activity Boundary

The exact Entity/Aggregate structure of Learning Activity remains deferred.

No generic LearningActivity Aggregate is introduced without a concrete business need.

---

## 21.8 Learning Direction

The following remain deferred:

```text
Learning Plan
Recommendation
Review Due
Learning Goal
Exam Preparation
```

Their persistence, lifecycle, ownership, and Aggregate boundaries will be determined only when required by a concrete V3 capability.

---

## 21.9 Other Learner State

The following concepts remain deferred:

```text
Vocabulary State
Grammar State
Skill State
XP
Streak
Achievement
Statistics
```

Their existence as domain state does not imply that they must become Aggregates or independent modules.

---

# 22. Target Model Principle

The target Learning Context must not be modeled as a larger version of the current Course-centered structure.

Instead, it separates:

```text
What can be learned
        ↓
Learning Structure

What the learner does
        ↓
Learning Activity

What happened
        ↓
Learning Evidence

What the system currently knows about the learner
        ↓
Learner State

What the learner should do next
        ↓
Learning Direction
```

The resulting conceptual loop is:

```text
Structure
    ↓
Activity
    ↓
Evidence
    ↓
Learner State
    ↓
Direction
    ↓
Next Activity
```

Course remains an important Aggregate Root within Learning Structure.

Enrollment remains the representation of learner participation in a Course.

Progress remains Course-scoped.

Competency and Current Level represent learner-centered state.

Assessment provides structured evaluation and may produce evidence that establishes or contributes to learner state.

Learning Direction remains intentionally deferred until concrete business requirements require its implementation.

---

# 23. Boundary Principle

The target model deliberately distinguishes:

```text
Domain Concept
      ≠
Entity

Entity
      ≠
Aggregate

Aggregate
      ≠
Module

Module
      ≠
Bounded Context
```

Therefore, the existence of a domain concept does not automatically require:

* an Aggregate Root;
* a repository;
* a database table;
* a package;
* or a separate bounded context.

Aggregate boundaries must be determined from:

* identity;
* lifecycle;
* invariants;
* business ownership;
* transactional consistency;
* and actual domain behavior.

---

# 24. Final Target Model

The current target Learning Context can therefore be summarized as:

```text
Learning Context
│
├── Learning Structure
│   └── Course Aggregate
│       └── Section
│           └── Lesson
│               └── LessonItem
│
├── Enrollment
│   └── Enrollment Aggregate
│       └── Progress
│
├── Learning Activities
│   └── Domain Concept
│       └── Structural boundary deferred
│
├── Learning Evidence
│   ├── LessonCompletion
│   ├── QuizAttempt
│   ├── QuestionResult
│   ├── ComponentResult
│   └── AssessmentResult
│
├── Assessment
│   ├── Component
│   │   └── Task(s)
│   ├── Evaluation Mechanism
│   ├── Completion Policy
│   ├── Time Limit
│   └── Attempt Rules
│
├── Learner State
│   ├── Competency
│   │   └── Current Level
│   └── Other learner-state concepts
│       └── Deferred
│
└── Learning Direction
    └── Deferred
```

This model is the current target domain baseline for V3.

It is sufficiently defined to guide subsequent Aggregate Boundary, Module Boundary, Application, and implementation decisions without prematurely fixing unresolved technical structures.
