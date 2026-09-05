# Target Domain Model

## 1. Purpose

This document defines the target domain model for the Learning Context in DeutschHub V3.

The target model is derived from the current domain implementation, application flows, persistence model, and the previously identified Learning responsibilities.

The purpose is to establish:

- the major target domain concepts;
- their business responsibilities;
- important relationships between concepts;
- confirmed classifications where sufficient evidence exists;
- concepts that remain candidates or open for further domain validation.

This document does not define the final database schema, API design, package structure, or implementation architecture.

The target domain model intentionally distinguishes confirmed domain structures from concepts that remain open for further domain validation. Not every domain concept is required to become an Aggregate Root, Entity, or Value Object.

---

## 2. Target Learning Model

The target Learning Context is broader than Course Management.

Its responsibilities can be understood through five related areas:

1. Learning Structure
2. Learning Activities
3. Learning Evidence
4. Learner State
5. Learning Direction

These areas describe different business responsibilities and do not automatically represent five Bounded Contexts or five Aggregate Roots.

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
````

This model represents a learner-centered Learning Context rather than a Course-centered model.

---

## 3. Learning Structure

### 3.1 Course

**Classification:** Aggregate Root
**Status:** Confirmed

Course is the established root of the current course structure.

The existing Course Aggregate is already defined in:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

The current structure is:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

with the corresponding domain entities:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

Course owns structural behavior such as adding, updating, and removing sections, lessons, and lesson items, as well as publishing and unpublishing the course.

The Course Aggregate is therefore retained as an established part of the target model.

The target model does not redefine the internal Course hierarchy.

---

### 3.2 Course Level

Course Level describes the level associated with a course.

It must remain distinct from:

* the learner's current level;
* certification level.

The existing `CEFRLevel` concept provides evidence for representing CEFR levels as a value/classification rather than treating a level itself as an independent Aggregate.

---

## 4. Enrollment and Learning Participation

### 4.1 Enrollment

**Classification:** Aggregate Root
**Status:** Confirmed

Enrollment represents a learner's participation in a specific Course.

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

Enrollment has its own lifecycle, including states and transitions such as:

```text
ENROLLED
    ↓
IN_PROGRESS
    ↓
COMPLETED
```

and terminal or alternative states such as:

```text
DROPPED
EXPIRED
```

Enrollment also owns course-scoped progress through its `Progress` value.

The target model therefore keeps Enrollment as a distinct Aggregate Root.

Enrollment represents participation in a learning structure; it is not the complete representation of the learner's learning state.

---

## 5. Progress

### 5.1 Progress

**Classification:** Value Object
**Status:** Confirmed

The existing implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

Progress currently represents:

* completed lessons;
* total lessons;
* completion percentage;
* total study minutes;
* last update time.

It also enforces invariants such as:

```text
completedLessons >= 0
completedLessons <= totalLessons
totalLessons > 0
studyMinutes >= 0
```

Progress has no independent identity and is immutable.

Therefore:

> Progress is a Value Object representing advancement within a defined learning scope.

Progress must not be interpreted as the complete Learner State.

For example:

```text
Course Progress ≠ Learner Competency
```

A learner completing a large percentage of a course does not automatically imply an equivalent level of mastery.

---

## 6. Learning Activities

### 6.1 Learning Activity

**Classification:** Domain Concept
**Status:** Target concept; exact Entity/Aggregate classification remains open

A Learning Activity represents an action or learning interaction performed by a learner for the purpose of:

* learning;
* practicing;
* reviewing;
* demonstrating knowledge or skills.

The current model does not contain a generic `LearningActivity` concept.

The existing:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

should not automatically be treated as the generic Learning Activity.

`LessonItem` currently represents content within a Lesson and supports types such as:

```text
TEXT
MEDIA
QUIZ
```

Its primary responsibility is therefore closer to learning content/resource organization than to representing the learner's action itself.

---

### 6.2 Activity and Content

Learning Activity and learning content are distinct concepts.

Conceptually:

```text
Learning Structure
        ↓
Learning Content
        ↓
Learning Activity
```

A Course or Lesson can provide content, while an Activity represents what the learner does with or through that content.

Examples of potential activity forms include:

* practice;
* review;
* assessment;
* listening;
* speaking;
* reading;
* writing.

The exact classification of these concepts remains open because the current implementation does not provide sufficient domain evidence to establish a universal Activity hierarchy.

---

## 7. Learning Evidence

### 7.1 Learning Evidence

**Classification:** Conceptual domain category
**Status:** Confirmed as a useful distinction; exact aggregate structure remains open

Learning Evidence represents an observable outcome or record produced by a learner's learning activity.

The target distinction is:

```text
Learning Activity
        ↓
Learning Evidence
```

Evidence is not the same as learner state.

For example:

```text
Quiz Attempt
      ↓
assessment evidence
```

and:

```text
Lesson Completion
      ↓
completion evidence
```

Evidence can contribute to learner state, but it does not automatically define learner state.

Similarly:

```text
Evidence ≠ Competency
```

A completed lesson or a particular quiz score does not automatically prove mastery without an explicit business rule.

---

### 7.2 LessonCompletion

**Classification:** Entity / Learning Evidence
**Status:** Confirmed candidate

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

It records that a learner completed a specific lesson within an enrollment.

The current application flow in:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

is approximately:

```text
Complete Lesson
      ↓
Create LessonCompletion
      ↓
Persist completion
      ↓
Update Enrollment Progress
```

This provides direct evidence that LessonCompletion can be understood as learning evidence that contributes to course-scoped progress.

---

### 7.3 QuizAttempt

**Classification:** Aggregate Root / Learning Evidence
**Status:** Confirmed candidate

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

A Quiz defines assessment content, while QuizAttempt represents a learner's individual attempt.

QuizAttempt has its own identity and assessment-related lifecycle/state and is therefore distinct from Quiz.

Conceptually:

```text
Quiz
 ↓
Assessment definition

QuizAttempt
 ↓
Learner's assessment execution
 ↓
Assessment Evidence
```

QuizAttempt should not automatically be treated as Competency or Learner State.

---

## 8. Learner State

### 8.1 Learner State

**Classification:** Business responsibility / conceptual area
**Status:** Confirmed target concept; not an Aggregate Root

Learner State represents what the system currently knows about the learner's learning condition and development.

Potential state includes:

* progress;
* competency;
* current level;
* learning history;
* XP;
* streak;
* achievements;
* statistics.

Learner State is therefore broader than course completion.

It should not be modeled as a single Aggregate Root without further business evidence.

The target model instead treats Learner State as a conceptual area containing several potentially independent domain concepts.

---

## 9. Competency

### 9.1 Competency

**Classification:** Entity candidate
**Status:** Open for further domain validation

No explicit `Competency` concept currently exists in the Learning domain.

This is therefore a target-domain gap rather than a current implementation.

Competency represents the learner's demonstrated capability or mastery in a learning dimension.

It must remain distinct from progress:

```text
Progress
    = advancement through a learning scope

Competency
    = demonstrated capability or mastery
```

For example:

```text
Course Progress = 80%
```

does not necessarily imply:

```text
German Competency = 80%
```

A learner may progress through a course while remaining weak in specific areas such as vocabulary, grammar, listening, or speaking.

The exact structure of Competency remains open.

It may require identity and lifecycle as an Entity, but there is not enough current evidence to establish its Aggregate boundary.

---

## 10. Current Level

### 10.1 Learner Current Level

**Classification:** Learner State concept
**Status:** Target concept; exact Entity/Aggregate classification remains open

Learner Current Level represents the learner's current estimated or established level.

It must remain distinct from:

```text
Course Level
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

The existing CEFR level representation provides a level classification/value, but the current Learning implementation does not provide a complete learner-level Current Level state.

---

### 10.2 CEFR Level

**Classification:** Value / classification
**Status:** Confirmed baseline

The current domain already contains a `CEFRLevel` value concept under:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/
```

This supports treating CEFR levels such as A1–C2 as a classification/value rather than as an independent Aggregate.

The target model does not assume that a learner's current level is automatically determined from course completion.

For example:

```text
Complete A2 Course
        ≠
Automatically become B1
```

Such a transition would require an explicit business rule or assessment policy.

---

## 11. UserProgress

### 11.1 Current UserProgress

The current implementation is:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

The current concept contains:

* user;
* course;
* enrollment;
* current progress;
* completed sections;
* completed lessons;
* total study minutes;
* activity timestamps;
* completion state.

Its actual scope is therefore largely:

```text
User + Course + Enrollment
```

rather than learner-wide state.

It also overlaps substantially with:

```text
Enrollment
    └── Progress
```

The current implementation additionally contains an inconsistency where `UserProgress` can initialize:

```text
Progress.createInitial(0)
```

while `Progress` requires a positive total lesson count.

### Target conclusion

`UserProgress` is not retained as the target representation of Learner State.

The target model does not introduce a replacement `LearnerState` Aggregate merely to preserve the existing structure.

The underlying business concepts should instead be separated according to their actual responsibilities.

---

## 12. Learning Direction

### 12.1 Learning Direction

**Classification:** Business responsibility / conceptual area
**Status:** Target concept; exact aggregate structure remains open

Learning Direction represents the responsibility of determining, organizing, or recommending what a learner should do next based on:

* learner state;
* learning goals;
* available learning opportunities;
* review requirements;
* learning progress;
* demonstrated weaknesses.

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

Learning Direction therefore consumes learner state rather than owning or replacing it.

---

### 12.2 Potential Learning Direction Concepts

The discovery of the Learning domain identified several possible direction-related capabilities:

* Daily Learning;
* Learning Plans;
* Recommendations;
* Review Due;
* Weakness-oriented Practice;
* Learning Goals;
* Exam Preparation.

These are currently treated as business concepts/capabilities rather than automatically becoming separate Aggregates.

Their final classification depends on future business rules, lifecycle requirements, and invariants.

---

## 13. Target Domain Relationships

The target model can be summarized as:

```text
                         Learning Structure
                                │
                                ↓
                         Learning Content
                                │
                                ↓
                         Learning Activity
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

More specifically:

```text
Course
  │
  └── Enrollment
         │
         └── Progress

Learning Activity
         │
         ├── LessonCompletion
         │
         └── QuizAttempt
                    │
                    ↓
              Learning Evidence
                    │
                    ↓
              Learner State
               ├── Competency
               ├── Current Level
               └── Other learner state
                    │
                    ↓
             Learning Direction
```

The diagram is conceptual and does not imply direct Aggregate ownership between all concepts.

---

## 14. Important Domain Distinctions

The following distinctions are fundamental to the target model.

### 14.1 Course Progress vs. Competency

```text
Course Progress
    = advancement within a learning scope

Competency
    = demonstrated capability
```

They must not be treated as equivalent.

---

### 14.2 Activity vs. Evidence

```text
Learning Activity
    = what the learner does

Learning Evidence
    = observable result or record produced by the activity
```

An activity may generate evidence.

---

### 14.3 Evidence vs. Learner State

```text
Evidence
    = individual observable outcome

Learner State
    = current representation of what is known about the learner
```

Evidence may contribute to state but is not itself the complete state.

---

### 14.4 Course Level vs. Learner Current Level

```text
Course Level
    = level associated with learning content

Learner Current Level
    = learner's current level

Certification Level
    = level established by certification
```

These concepts must remain separate unless an explicit business rule connects them.

---

### 14.5 Enrollment vs. Learner State

```text
Enrollment
    = participation in a specific Course

Learner State
    = broader representation of the learner's learning condition
```

Enrollment may contain course-scoped Progress, but it should not become the complete representation of Learner State.

---

## 15. Target Classification Summary

| Concept            | Target Classification              | Status                   |
| ------------------ | ---------------------------------- | ------------------------ |
| Course             | Aggregate Root                     | Confirmed                |
| Section            | Entity within Course               | Confirmed                |
| Lesson             | Entity within Course               | Confirmed                |
| LessonItem         | Entity within Course               | Confirmed                |
| Enrollment         | Aggregate Root                     | Confirmed                |
| Progress           | Value Object                       | Confirmed                |
| LessonCompletion   | Entity / Learning Evidence         | Confirmed candidate      |
| Quiz               | Assessment domain concept          | Existing                 |
| QuizAttempt        | Aggregate Root / Learning Evidence | Confirmed candidate      |
| Learning Activity  | Domain Concept                     | Open classification      |
| Learning Evidence  | Conceptual category                | Open aggregate structure |
| Learner State      | Business responsibility            | Not an Aggregate Root    |
| Competency         | Entity candidate                   | Open                     |
| Current Level      | Learner State concept              | Open                     |
| CEFRLevel          | Value / classification             | Confirmed baseline       |
| UserProgress       | Not retained as target aggregate   | Confirmed direction      |
| Learning Direction | Business responsibility            | Open aggregate structure |
| Learning Plan      | Learning Direction concept         | Open                     |
| Recommendation     | Learning Direction concept         | Open                     |
| Review Due         | Learning Direction concept         | Open                     |
| Learning Goal      | Learning Direction concept         | Open                     |

---

## 16. Open Questions

The following decisions are intentionally left open because the current code and domain evidence are not sufficient to establish them conclusively:

1. Whether Competency should become an independent Aggregate Root or remain inside another learner-state boundary.
2. Whether Current Level requires independent identity/lifecycle or can remain part of a larger learner-state model.
3. Whether specific Learning Activities require independent Aggregates or can be modeled through specialized domain concepts.
4. Whether Learning Plans require persistence and lifecycle as an Aggregate.
5. Whether Recommendations are persisted domain objects or derived decisions.
6. How Review Due should be represented and what rules determine when learning becomes due.
7. How assessment evidence contributes to Competency and Current Level.
8. How vocabulary, grammar, and language skills should be represented within Learner State.
9. Whether XP, streaks, achievements, and statistics represent independent domain state or derived/read-model information.

These open questions should be resolved through further business/domain validation rather than assumed from the current implementation.

---

## 17. Target Model Principle

The target Learning Context should not be modeled as a larger version of the current Course-centered structure.

Instead, it should support a learner-centered learning loop:

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

Course remains an important Aggregate Root within Learning Structure, but Course is not the definition of the entire Learning Context.

The target model therefore separates:

* what can be learned;
* what the learner does;
* what evidence is produced;
* what the system knows about the learner;
* what the learner should do next.

This separation provides the conceptual foundation for later domain and architectural decisions without prematurely determining the final Aggregate boundaries, persistence model, API structure, or package organization.

