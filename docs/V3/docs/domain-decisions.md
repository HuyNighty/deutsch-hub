# Domain Decisions

## 1. Purpose

This document records the domain decisions established for the Learning Context in DeutschHub V3.

The decisions are derived from the current domain model, application flows, persistence structure, and the target domain model defined during the domain analysis.

The purpose of this document is to:

- record confirmed domain decisions;
- distinguish domain concepts with different business responsibilities;
- establish confirmed Aggregate boundaries where sufficient evidence exists;
- identify concepts that should not be merged;
- explicitly preserve unresolved decisions;
- provide a stable domain baseline for subsequent architecture and implementation analysis.

This document does not define:

- REST endpoints;
- final database schema;
- package structure;
- infrastructure implementation;
- application service structure.

Where the available domain evidence is insufficient, the decision is intentionally left open rather than inferred.

---

# 2. Decision Principles

The following principles are used when evaluating the target domain model:

1. A domain concept is not automatically an Aggregate Root because it is important.
2. Entity, Value Object, Aggregate Root, Evidence, and Business Responsibility are different classifications.
3. Aggregate boundaries should be based on identity, lifecycle, invariants, and consistency requirements.
4. Learning Evidence must be distinguished from Learner State.
5. Course-scoped Progress must not be treated as the complete Learner State.
6. Current implementation structure is evidence for domain analysis, but it does not automatically determine the target model.
7. Historical domain facts must not be rewritten merely because newer definitions or states exist.
8. Concepts without sufficient domain evidence remain explicitly open.
9. A new Aggregate or abstraction should not be introduced solely because a similar pattern exists elsewhere in the system.

---

# 3. Confirmed Domain Decisions

## 3.1 Course Remains an Aggregate Root

**Decision:** Course remains an Aggregate Root responsible for the integrity of the course structure.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
````

The current Course model owns the course hierarchy:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

The Course Aggregate exposes behavior for managing sections, lessons, and lesson items.

Application services such as:

```text
src/main/java/com/deutschhub/application/learning/service/AddSectionToCourseService.java
src/main/java/com/deutschhub/application/learning/service/AddLessonToSectionService.java
src/main/java/com/deutschhub/application/learning/service/AddLessonItemService.java
```

operate through the Course Aggregate.

**Conclusion:**

The established Course Aggregate is retained as the target representation of Learning Structure.

The internal Course hierarchy is not redefined by this document.

---

## 3.2 Enrollment Remains an Independent Aggregate Root

**Decision:** Enrollment remains an independent Aggregate Root representing a learner's participation in a Course.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

Enrollment has:

* its own identity;
* lifecycle state;
* status transitions;
* completion behavior;
* drop and expiration behavior;
* course-scoped Progress.

The current lifecycle includes states such as:

```text
ENROLLED
IN_PROGRESS
COMPLETED
DROPPED
EXPIRED
```

**Conclusion:**

Enrollment represents participation in a specific Course.

It is not treated as the complete representation of Learner State.

Therefore:

```text
Enrollment ≠ Learner State
```

---

## 3.3 Progress Is a Value Object

**Decision:** Progress is a Value Object representing advancement within a defined learning scope.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

The current Progress model is immutable and contains values such as:

* completed lessons;
* total lessons;
* completion percentage;
* total study minutes;
* last update time.

It also enforces internal invariants including:

```text
completedLessons >= 0
completedLessons <= totalLessons
totalLessons > 0
studyMinutes >= 0
```

**Conclusion:**

Progress is retained as a Value Object.

It represents advancement within a learning scope and must not be interpreted as the complete Learner State.

Therefore:

```text
Progress ≠ Competency
```

---

## 3.4 UserProgress Is Not Retained as a Target Aggregate

**Decision:** The current UserProgress concept is not retained as the target representation of Learner State or as a target Aggregate Root.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

The current concept contains several values that overlap with Enrollment and Progress, including:

* course;
* enrollment;
* current progress;
* completed lessons;
* study time;
* completion state.

Its actual scope is largely:

```text
User + Course + Enrollment
```

rather than a learner-wide state.

It overlaps with:

```text
Enrollment
    └── Progress
```

The current implementation also contains an inconsistency where `UserProgress` can initialize:

```text
Progress.createInitial(0)
```

while the `Progress` Value Object requires a positive total lesson count.

**Conclusion:**

UserProgress is not used as the target Learner State Aggregate.

This decision does not by itself determine whether the current implementation should immediately be removed or refactored.

Implementation changes belong to a later stage.

---

## 3.5 LessonCompletion Is Learning Evidence

**Decision:** LessonCompletion represents learning evidence produced by a lesson-completion outcome.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

and:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

The current application flow is conceptually:

```text
Complete Lesson
      ↓
Create LessonCompletion
      ↓
Persist Completion
      ↓
Update Enrollment Progress
```

**Conclusion:**

LessonCompletion is treated as an Entity representing Learning Evidence.

It is distinct from Progress.

The relationship is conceptually:

```text
Lesson Completion Evidence
        ↓
Course Progress
```

---

# 4. Quiz Domain Decisions

## 4.1 Quiz Is an Aggregate Root

**Decision:** Quiz is an Aggregate Root responsible for the stable identity, ownership, governance, and Revision lifecycle of an assessment.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
```

The target Quiz model establishes a distinction between:

```text
Quiz
    = stable assessment identity and governance

QuizRevision
    = concrete assessment definition
```

Quiz-level concerns include:

* identity;
* ownership;
* author;
* visibility;
* availability;
* Quiz lifecycle;
* Revision management.

**Conclusion:**

Quiz remains an Aggregate Root.

The Quiz Aggregate owns its QuizRevisions.

---

## 4.2 QuizRevision Is an Entity Inside the Quiz Aggregate

**Decision:** QuizRevision is an Entity inside the Quiz Aggregate and represents one concrete version of an assessment definition.

The target structure is:

```text
Quiz
 └── QuizRevision
      └── Question
           └── Answer
```

A QuizRevision contains the definition that is used when a learner performs an assessment, including:

* title;
* description;
* difficulty;
* time limit;
* passing percentage;
* maximum attempts;
* completion policy;
* Questions;
* Answer configuration.

The maximum score is derived from the Questions:

```text
maxScore
=
sum(Question.score)
```

A Published Revision is immutable.

When a new Revision is published, it becomes a new historical definition rather than modifying the previous Published Revision.

**Conclusion:**

QuizRevision is not an independent Aggregate Root.

It remains inside the Quiz Aggregate.

---

## 4.3 Question Is an Entity Inside QuizRevision

**Decision:** Question is an Entity inside QuizRevision.

A Question does not have an independent Aggregate boundary.

Its identity and lifecycle are meaningful within the Revision that owns it.

The supported Question Types are:

```text
SINGLE_CHOICE
MULTIPLE_CHOICE
TRUE_FALSE
```

Question Type determines the structural and evaluation rules of the Question.

A Draft Question may temporarily be incomplete.

Publication requires the Question to satisfy the rules associated with its Question Type.

**Conclusion:**

Question remains an Entity nested within QuizRevision.

---

## 4.4 Answer Is an Entity Inside Question

**Decision:** Answer is an Entity inside Question.

The target model uses:

```text
Question
    └── Answer
```

An Answer has stable identity and presentation order within its Question.

The `isCorrect` property belongs to Answer.

The Question enforces the collection rules required by its Question Type.

For a Published Revision, Answer ordering is part of the historical assessment definition.

**Conclusion:**

Answer is not an Aggregate Root and is not managed independently from its Question.

---

## 4.5 Quiz and QuizAttempt Remain Separate Aggregate Roots

**Decision:** Quiz and QuizAttempt represent different domain responsibilities and remain separate Aggregate Roots.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

The distinction is:

```text
Quiz
    = assessment definition and governance

QuizAttempt
    = learner-specific assessment execution
```

The Quiz Aggregate owns the assessment definition.

The QuizAttempt Aggregate owns the lifecycle and state of one learner's execution.

**Conclusion:**

```text
Quiz Aggregate
    ≠
QuizAttempt Aggregate
```

QuizAttempt must not be nested inside the Quiz Aggregate.

---

## 4.6 QuizAttempt Is Bound to an Exact QuizRevision

**Decision:** Every QuizAttempt is permanently bound to the exact Published QuizRevision used when the Attempt starts.

Conceptually:

```text
Quiz
 ├── Revision A
 │     └── Historical
 │
 └── Revision B
       └── Published

Attempt 1
    └── Revision A

Attempt 2
    └── Revision B
```

Publishing a newer Revision does not migrate existing Attempts.

An `IN_PROGRESS` Attempt continues against its original Revision.

This preserves the exact assessment definition used for the learner's execution and evaluation.

**Conclusion:**

QuizAttempt references a specific QuizRevision rather than merely referencing the current Quiz definition.

---

## 4.7 QuizAttempt Represents Assessment Execution and Evidence

**Decision:** QuizAttempt represents a learner-specific assessment execution and preserves assessment evidence.

A QuizAttempt contains learner-specific information such as:

* User identity;
* bound QuizRevision;
* lifecycle state;
* current responses;
* final evaluation;
* score;
* assessment result.

QuizAttempt may therefore serve as Learning Evidence.

However:

```text
QuizAttempt ≠ Competency
```

and:

```text
Assessment Result ≠ Learner State
```

An assessment result does not automatically establish mastery or competency without an explicit business rule.

**Conclusion:**

QuizAttempt is an Aggregate Root that preserves an assessment execution and its historical outcome.

---

## 4.8 QuestionResult Is Assessment Evidence

**Decision:** QuestionResult represents historical evaluation evidence for one Question within a QuizAttempt.

A QuestionResult records facts such as:

* Question identity;
* selected Answer identities;
* response status;
* correctness when applicable;
* earned score.

For every Question in the bound Published Revision, evaluation produces exactly one QuestionResult.

For an unanswered Question:

```text
responseStatus = UNANSWERED
isCorrect = null
earnedScore = 0
```

QuestionResult records the result of evaluating the learner's response against the bound Revision.

It does not rewrite the historical Question or Answer definition.

**Conclusion:**

QuestionResult is assessment evidence associated with the QuizAttempt.

It is not an independent Aggregate Root.

---

# 5. Learning Evidence and Learner State

## 5.1 Learning Evidence and Learner State Are Distinct

**Decision:** Learning Evidence and Learner State must remain separate domain concepts.

The target relationship is:

```text
Learning Activity
        ↓
Learning Evidence
        ↓
Learner State
```

Learning Evidence represents an observable outcome or historical record produced by a learning activity.

Learner State represents the current state the system knows about the learner.

Examples of Learning Evidence include:

```text
LessonCompletion
QuizAttempt
QuestionResult
```

Evidence may contribute to Learner State, but evidence is not itself the complete Learner State.

Therefore:

```text
Evidence ≠ Learner State
```

---

## 5.2 Course Progress Is Not the Complete Learner State

**Decision:** Course-scoped Progress must not be treated as the complete Learner State.

The current model provides:

```text
Enrollment
    └── Progress
```

This is appropriate for representing advancement within a Course.

However, Learner State may include broader concepts such as:

* Competency;
* Current Level;
* learning history;
* vocabulary development;
* grammar development;
* skills;
* XP;
* streaks;
* achievements;
* statistics.

Therefore:

```text
Enrollment.Progress
        ≠
Learner State
```

Course completion and learner capability remain conceptually distinct.

---

## 5.3 Competency Is Distinct from Progress and Evidence

**Decision:** Competency is a separate target domain concept representing demonstrated learner capability or mastery.

No explicit Competency implementation was found in the current Learning domain.

Therefore this is a target-domain gap rather than an existing implementation.

The distinction is:

```text
Progress
    = advancement through a learning scope

Evidence
    = observable learning outcome

Competency
    = demonstrated capability or mastery
```

For example:

```text
Course Progress = 80%
```

does not imply:

```text
Learner Competency = 80%
```

Competency must not be derived from Progress or from a single Evidence record without explicit domain rules.

---

## 5.4 Course Level, Learner Current Level, and Certification Level Are Distinct

**Decision:** Course Level, Learner Current Level, and Certification Level represent different concepts.

The distinction is:

```text
Course Level
    = level associated with learning content

Learner Current Level
    = learner's current level

Certification Level
    = level established through certification
```

The existing CEFR level concept provides a classification/value used within the Learning domain.

However, the current implementation does not establish a complete learner-level Current Level state.

**Conclusion:**

```text
Course Level
    ≠ Learner Current Level
    ≠ Certification Level
```

---

## 5.5 Course Completion Does Not Automatically Determine Learner Level

**Decision:** Course completion or Course Progress must not automatically determine Learner Current Level.

For example:

```text
Complete A2 Course
        ≠
Automatically become B1
```

Such a transition requires an explicit business rule, assessment policy, or other domain evidence.

The current code does not establish such a rule.

**Conclusion:**

Learner Current Level remains an independent learner-state concept until a valid domain rule defines how it is determined.

---

## 5.6 Learner State Is a Business Responsibility, Not Automatically an Aggregate Root

**Decision:** Learner State is treated as a business responsibility rather than a single Aggregate Root.

Learner State may contain or expose concepts such as:

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

There is insufficient evidence to conclude that these concepts must share one consistency boundary.

**Conclusion:**

No `LearnerState` Aggregate Root is introduced at this stage.

Aggregate boundaries will be determined later based on:

* identity;
* lifecycle;
* invariants;
* transactional consistency;
* business ownership.

---

# 6. Learning Activity and Learning Direction

## 6.1 LessonItem Is Not the Generic Learning Activity

**Decision:** LessonItem must not automatically be treated as the generic Learning Activity abstraction.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

LessonItem currently represents content within a Lesson and supports types including:

```text
TEXT
MEDIA
QUIZ
```

Its responsibility is primarily related to organizing learning content/resources.

A Learning Activity instead represents an action or interaction performed by the learner.

Therefore:

```text
LessonItem ≠ Generic Learning Activity
```

**Conclusion:**

Learning Activity remains a target domain concept whose exact classification is open.

---

## 6.2 Learning Direction Is Distinct from Learner State

**Decision:** Learning Direction consumes learner state and other learning inputs to determine or recommend what the learner should do next.

Conceptually:

```text
Learner State
      +
Learning Goals
      +
Available Learning Opportunities
      ↓
Learning Direction
      ↓
Next Learning Activity
```

Learner State answers:

> What is the learner's current learning condition?

Learning Direction answers:

> What should the learner do next?

Therefore:

```text
Learner State ≠ Learning Direction
```

---

# 7. Open Domain Decisions

The following decisions remain intentionally open.

## 7.1 Competency Aggregate Boundary

It remains open whether Competency should be:

* an independent Aggregate Root;
* an Entity within another Aggregate;
* part of another learner-state boundary;
* or represented through another domain structure.

Current evidence is sufficient to establish Competency as a distinct concept, but not its final Aggregate boundary.

---

## 7.2 Current Level Aggregate Boundary

The target model establishes Learner Current Level as a learner-state concept.

Its exact identity, lifecycle, and Aggregate boundary remain open.

---

## 7.3 Learning Activity Structure

The target model establishes Learning Activity as a distinct responsibility from learning content.

However, the exact structure of activity types remains open.

Potential activity forms identified during discovery include:

* Practice;
* Review;
* Assessment;
* Listening;
* Speaking;
* Reading;
* Writing.

The current code does not provide enough evidence to determine whether these should be:

* separate domain concepts;
* entities;
* aggregates;
* activity types;
* capabilities over existing concepts.

---

## 7.4 Learning Activity Versioning

It remains open whether Learning Activities require explicit versioning.

Versioning should only be introduced if the business requires historical activity definitions to remain stable when an activity is reused or modified.

The existence of versioning in another domain concept does not by itself require Learning Activity versioning.

---

## 7.5 Learning Plan

Learning Plans are identified as a potential Learning Direction concept.

It remains open whether a Learning Plan requires:

* persistent domain state;
* an independent Aggregate Root;
* an Entity within another boundary;
* or a derived representation.

This depends on future business rules and lifecycle requirements.

---

## 7.6 Recommendations

Recommendations are identified as part of Learning Direction.

It remains open whether recommendations are:

* persisted domain objects;
* derived decisions;
* application-level results;
* or another domain representation.

No persistence or lifecycle requirement is currently sufficient to establish an Aggregate.

---

## 7.7 Review Due

Review Due is a recognized Learning Direction capability.

Its exact domain representation and scheduling rules remain open.

The current code does not provide sufficient evidence for a target Aggregate or Entity structure.

---

## 7.8 Vocabulary, Grammar, and Skills

Vocabulary, Grammar, and language skills are recognized as relevant parts of the target Learning domain.

Their exact representation within Learner State remains open.

They should not automatically become independent Bounded Contexts, Aggregates, or modules without additional domain evidence.

---

## 7.9 XP, Streak, Achievements, and Statistics

These concepts were identified during Learning discovery.

Their exact classification remains open.

They may represent:

* genuine domain state;
* derived state;
* historical records;
* gamification concepts;
* statistical or read-model information.

No final Aggregate decision is made at this stage.

---

## 7.10 Evidence-to-Competency Rules

The target model establishes:

```text
Evidence
    ↓
may contribute to
    ↓
Competency
```

However, the actual rules are not yet defined.

For example, the model does not currently establish:

```text
Quiz Score
    ↓
Competency Increase
```

or:

```text
Lesson Completion
    ↓
Mastery
```

Such rules require explicit business validation.

---

# 8. Decision Summary

The current target baseline is:

```text
Course
    → Aggregate Root

Enrollment
    → Aggregate Root

Progress
    → Value Object

LessonCompletion
    → Entity / Learning Evidence

Quiz
    → Aggregate Root
       └── QuizRevision
             → Entity
             └── Question
                   → Entity
                   └── Answer
                         → Entity

QuizAttempt
    → Separate Aggregate Root
       → Assessment Execution / Evidence

QuestionResult
    → Assessment Evidence

Competency
    → Target domain concept
      → Aggregate boundary open

Current Level
    → Learner State concept
      → Aggregate boundary open

Learner State
    → Business responsibility
      → Not a single Aggregate Root

Learning Activity
    → Domain concept
      → Structure open

Learning Evidence
    → Conceptual domain category

Learning Direction
    → Business responsibility
```

The following distinctions are considered fundamental:

```text
Course Progress ≠ Competency

Evidence ≠ Learner State

Progress ≠ Competency

Enrollment ≠ Learner State

LessonItem ≠ Generic Learning Activity

Course Level ≠ Learner Current Level

Learner Current Level ≠ Certification Level

Learning Direction ≠ Learner State

Quiz ≠ QuizAttempt

QuizRevision ≠ QuizAttempt

Question ≠ QuestionResult

Answer ≠ UserAnswer

Assessment Result ≠ Competency
```

---

# 9. Boundary for Further Design

The established decisions provide the domain baseline for further Aggregate Boundary and architecture analysis.

Confirmed Aggregate boundaries are:

```text
Course Aggregate
    └── Section
         └── Lesson
              └── LessonItem
```

```text
Enrollment Aggregate
    └── Progress
```

```text
Quiz Aggregate
    └── QuizRevision
         └── Question
              └── Answer
```

```text
QuizAttempt Aggregate
```

```text
LessonCompletion
    → independent Learning Evidence
```

No generic `LearnerState` Aggregate is introduced.

No generic `LearningActivity` Aggregate is introduced.

No Competency Aggregate is introduced until sufficient business evidence exists.

The next design stage should use these decisions to evaluate the target module boundaries and architecture.

Aggregate boundaries should continue to be determined using:

* identity;
* lifecycle;
* invariants;
* transactional consistency;
* business ownership;
* domain dependencies.

The target model should remain independent of the current package structure until the corresponding business boundaries have been established.

````
