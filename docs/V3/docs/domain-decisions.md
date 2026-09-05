# Domain Decisions

## 1. Purpose

This document records the domain decisions established for the Learning Context in DeutschHub V3.

The decisions are derived from the current domain model, application flows, persistence structure, and the target domain model defined in the preceding analysis.

The purpose of this document is to:

- record confirmed domain decisions;
- distinguish domain concepts with different business responsibilities;
- identify concepts that should not be merged;
- explicitly preserve unresolved decisions;
- provide a stable domain baseline for subsequent Aggregate Boundary and architecture analysis.

This document does not define the final database schema, API design, package structure, or implementation details.

Where the available domain evidence is insufficient, the decision is intentionally left open rather than inferred.

---

## 2. Decision Principles

The following principles are used when evaluating the target domain model:

1. A domain concept is not automatically an Aggregate Root because it is important.
2. Entity, Value Object, Aggregate Root, Evidence, and Business Responsibility are different classifications.
3. Aggregate boundaries should be based on identity, lifecycle, invariants, and consistency requirements.
4. Learning Evidence must be distinguished from Learner State.
5. Course-scoped Progress must not be treated as the complete Learner State.
6. Current implementation structure is evidence for domain analysis, but it does not automatically determine the target model.
7. Concepts without sufficient domain evidence remain explicitly open.

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

The current lifecycle includes transitions such as:

```text
ENROLLED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

with alternative states such as:

```text
DROPPED
EXPIRED
```

**Conclusion:**

Enrollment represents participation in a specific Course and is not treated as the complete representation of Learner State.

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

It must not be interpreted as the complete representation of Learner State.

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

UserProgress should not be used as the target Learner State Aggregate.

This decision does not by itself determine whether the current implementation should immediately be removed or refactored. Implementation changes belong to a later stage.

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

## 3.6 Quiz and QuizAttempt Remain Distinct

**Decision:** Quiz and QuizAttempt represent different domain responsibilities.

**Evidence:**

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

The distinction is:

```text
Quiz
    = assessment definition

QuizAttempt
    = learner-specific assessment execution
```

QuizAttempt has learner-specific identity and assessment state.

**Conclusion:**

Quiz and QuizAttempt remain separate concepts.

QuizAttempt can serve as Learning Evidence.

However:

```text
QuizAttempt ≠ Competency
```

An assessment score does not automatically determine mastery without an explicit business rule.

---

## 3.7 Learning Evidence and Learner State Are Distinct

**Decision:** Learning Evidence and Learner State must remain separate domain concepts.

The target relationship is:

```text
Learning Activity
        ↓
Learning Evidence
        ↓
Learner State
```

Learning Evidence represents an observable outcome or record produced by an activity.

Learner State represents the current state the system knows about the learner.

For example:

```text
QuizAttempt
    ↓
Assessment Evidence
```

may contribute to:

```text
Competency
```

but the two concepts are not equivalent.

**Conclusion:**

Evidence may contribute to state, but evidence is not itself the complete learner state.

Therefore:

```text
Evidence ≠ Learner State
```

---

## 3.8 Course Progress Is Not the Complete Learner State

**Decision:** Course-scoped Progress must not be treated as the complete Learner State.

The current model provides Progress through Enrollment:

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

Course completion and learner capability must remain conceptually distinct.

---

## 3.9 Competency Is Distinct from Progress and Evidence

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

**Conclusion:**

Competency must not be derived from Progress or a single Evidence record without explicit domain rules.

Its exact Aggregate boundary remains open.

---

## 3.10 Course Level, Learner Current Level, and Certification Level Are Distinct

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

The following must remain distinct:

```text
Course Level
    ≠ Learner Current Level
    ≠ Certification Level
```

---

## 3.11 Course Completion Does Not Automatically Determine Learner Level

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

Learner Current Level must be treated as an independent learner-state concept until a valid domain rule defines how it is determined.

---

## 3.12 Learner State Is a Business Responsibility, Not Automatically an Aggregate Root

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

## 3.13 LessonItem Is Not the Generic Learning Activity

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

## 3.14 Learning Direction Is Distinct from Learner State

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

# 4. Open Domain Decisions

The following decisions remain intentionally open.

## 4.1 Competency Aggregate Boundary

It remains open whether Competency should be:

* an independent Aggregate Root;
* an Entity within another Aggregate;
* part of another learner-state boundary;
* or represented through another domain structure.

Current evidence is sufficient to establish Competency as a distinct concept, but not its final Aggregate boundary.

---

## 4.2 Current Level Aggregate Boundary

The target model establishes Learner Current Level as a learner-state concept.

Its exact identity, lifecycle, and Aggregate boundary remain open.

---

## 4.3 Learning Activity Structure

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
* or capabilities over existing concepts.

---

## 4.4 Learning Plan

Learning Plans are identified as a potential Learning Direction concept.

It remains open whether a Learning Plan requires:

* persistent domain state;
* an independent Aggregate Root;
* an Entity within another boundary;
* or a derived representation.

This depends on future business rules and lifecycle requirements.

---

## 4.5 Recommendations

Recommendations are identified as part of Learning Direction.

It remains open whether recommendations are:

* persisted domain objects;
* derived decisions;
* application-level results;
* or another domain representation.

No persistence or lifecycle requirement is currently sufficient to establish an Aggregate.

---

## 4.6 Review Due

Review Due is a recognized Learning Direction capability.

Its exact domain representation and scheduling rules remain open.

The current code does not provide sufficient evidence for a target Aggregate or Entity structure.

---

## 4.7 Vocabulary, Grammar, and Skills

Vocabulary, Grammar, and language skills are recognized as relevant parts of the target Learning domain.

Their exact representation within Learner State remains open.

They should not automatically become independent Bounded Contexts, Aggregates, or modules without additional domain evidence.

---

## 4.8 XP, Streak, Achievements, and Statistics

These concepts were identified during Learning discovery.

Their exact classification remains open.

They may represent:

* genuine domain state;
* derived state;
* historical records;
* gamification concepts;
* read-model/statistical information.

No final Aggregate decision is made at this stage.

---

## 4.9 Evidence-to-Competency Rules

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

# 5. Decision Summary

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
    → Assessment concept

QuizAttempt
    → Aggregate Root / Learning Evidence

Competency
    → Entity candidate

Current Level
    → Learner State concept

Learner State
    → Business responsibility, not a single Aggregate Root

Learning Activity
    → Domain concept

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
```

---

# 6. Boundary for Further Design

This document intentionally stops before defining final Aggregate Boundaries.

The next design stage should determine Aggregate boundaries using the established decisions and the following criteria:

* identity;
* lifecycle;
* invariants;
* transactional consistency;
* business ownership;
* dependency between domain concepts.

The resulting Aggregate Boundaries should then provide the basis for the target architecture and implementation structure.

The target model should remain independent of the current package structure until those business boundaries have been established.


