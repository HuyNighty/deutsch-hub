# Domain Analysis

## 1. Purpose

This document analyzes the business responsibilities of the **Learning Context** in DeutschHub based on:

- the current product direction;
- the existing Learning Context implementation;
- the domain models currently present in the codebase;
- the relationships between Course, Enrollment, Progress, Assessment, and learner state.

The purpose is not to redesign the system immediately.

Instead, this analysis aims to identify:

- what the Learning Context currently represents;
- which domain concepts have clear business responsibilities;
- where responsibilities overlap;
- which learner capabilities are currently missing;
- and which domain decisions should be carried forward into the next design stages.

---

# 2. Business Understanding

## 2.1. What Does Learning Mean?

The current DeutschHub implementation primarily represents Learning through a course structure:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
````

A learner participates in a Course through an `Enrollment`, completes Lessons through `LessonCompletion`, and tracks progress through `Progress`.

This represents a clear **course-based learning model**.

However, from a product perspective, Learning is not necessarily limited to:

> "How much of a Course has the learner completed?"

Learning may also involve:

* the learner's current level;
* what the learner has learned;
* areas where the learner is weak;
* skills currently being practiced;
* assessment performance;
* and what the learner should do next.

Therefore, two different scopes need to be distinguished:

```text
Course Learning
=
Learning through a specific Course

Learner Learning
=
The broader learning state of a learner
```

This distinction is one of the main domain questions addressed by V3.

---

## 2.2. Course Learning vs Learner Learning

### Course Learning

The current implementation provides a relatively clear course-based flow:

```text
Course
   ↓
Enrollment
   ↓
LessonCompletion
   ↓
Progress
```

Each concept answers a different question:

* **Course** — What Course is the learner studying?
* **Enrollment** — How is the learner participating in that Course?
* **Progress** — How far has the learner progressed within that Course?
* **LessonCompletion** — Which Lessons has the learner completed?

These responsibilities are reflected in the current implementation.

For example:

* `src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java` manages the Course hierarchy and publication lifecycle.
* `src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java` manages learner participation and owns `Progress`.
* `src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java` represents course-scoped progress.
* `src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java` records Lesson completion.

### Learner Learning

Questions such as:

> "What is my current level?"

> "What have I learned?"

> "Where am I weak?"

> "What should I learn next?"

cannot be fully answered by `Enrollment.progress`.

Therefore:

> **Course Progress should not be treated as the complete Learner Learning State.**

---

## 2.3. Learner-level State

If DeutschHub needs a learner-level learning state, it should be able to answer meaningful learner questions such as:

```text
Where am I?
What am I doing?
How far have I progressed?
What have I learned?
How well can I perform?
What should I do next?
```

This is broader than the current `UserProgress` implementation.

A useful distinction is:

```text
Source State
    ↓
Evidence
    ↓
Derived Learning State
    ↓
Learning Decision
```

For example:

```text
LessonCompletion ─────┐
QuizAttempt ──────────┤
Vocabulary activity ──┤
Skill assessment ─────┤
                      ↓
              Learner Learning State
                      ↓
              Learning Direction
```

This is a domain direction for further investigation, not a final target model.

A learner-level state should also not automatically become a large aggregate containing every Course, Quiz, Vocabulary, Grammar, and Skill object.

The domain responsibility must be determined before deciding its final structural representation.

---

## 2.4. Current Level and CEFR

An important distinction is that **Course/Lesson Level is not the learner's Current Level**.

The current `CEFRLevel` value object is used in the Learning domain for content classification. It is currently associated with concepts such as Course and Lesson.

Therefore:

```text
Course Level
≠
Learner Current Level
```

Learner Current Level should also not be interpreted as an official certification.

DeutschHub may provide an **estimated or assessed current level** to help learners understand their current ability and learning direction.

Official CEFR certification remains the responsibility of external examinations and certification systems such as TELC or Goethe.

### Evidence for Current Level

Current Level should not be determined from a single source.

The product direction identifies several potential sources of evidence:

```text
Course learning
Vocabulary
Grammar
Reading
Listening
Speaking
Writing
Quizzes / Assessments
Mock exams
```

Therefore:

> **Current Level is a different domain concept from Course Progress.**

It should also not be calculated simply by summing or averaging Course Progress values.

---

## 2.5. Learning Direction

Learner state becomes more meaningful if it can influence future learning behavior.

The product direction indicates that learner state may eventually influence:

* recommendations;
* next steps;
* learning direction;
* remediation;
* progression.

For example:

```text
Learner is A2
+
Listening is weaker
+
Recent assessment shows difficulty
+
Course progress is high
        ↓
System may recommend
more listening practice
```

This leads to an important distinction:

```text
Learner State
=
What is true about the learner?

Learning Direction
=
What should the learner do next?
```

A learner-level state may therefore become an input to Learning Direction.

However, these should not automatically be represented by the same domain object.

No complete Learning Direction model is currently implemented in the Learning Context.

---

# 3. Current Domain Responsibilities

## 3.1. Course

Source:

`src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java`

`Course` currently acts as the aggregate responsible for the structure of a learning course:

```text
Course
 ├── Section
 │    └── Lesson
 │         └── LessonItem
```

Its responsibilities include:

* course metadata;
* ownership;
* publication;
* Sections;
* Lessons;
* LessonItems;
* hierarchy management;
* lifecycle management;
* estimated learning hours.

`Course` also protects business invariants such as preventing modification of published Courses and validating the course structure before publication.

### Domain Conclusion

**Course has a clear responsibility and should be kept.**

However:

> Course represents a structured learning offering, not the entire Learning Context.

---

## 3.2. Enrollment

Source:

`src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java`

`Enrollment` represents a learner's participation in a specific Course.

It contains:

```text
userId
courseId
status
Progress
timestamps
```

and manages a participation lifecycle:

```text
ENROLLED
    ↓
IN_PROGRESS
    ↓
COMPLETED

or

DROPPED
EXPIRED
```

`Enrollment` also owns `Progress`.

This means that Enrollment is more than a simple User-Course relationship.

It represents:

> **The learner's participation in a specific Course.**

### Domain Conclusion

**Enrollment should be kept.**

It has a clear and meaningful business responsibility.

---

## 3.3. Course Progress

Source:

`src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java`

`Progress` currently represents:

* completed lessons;
* total lessons;
* completion percentage;
* total study minutes;
* last update time.

`Enrollment` owns this value object, and `CompleteLessonService` updates it when a learner completes a Lesson.

The current flow is:

```text
LessonCompletion
       ↓
Count completed lessons
       ↓
Enrollment.updateProgress(...)
       ↓
Progress
       ↓
Enrollment.status
```

The frontend also uses this course-scoped progress:

* `src/features/my-learning/`
* `src/features/my-course-detail/`

to display:

* completed lessons;
* total lessons;
* completion percentage;
* study time.

### Domain Conclusion

`Progress` has a clear meaning when interpreted as:

> **Progress within an Enrollment / Course.**

It should not be treated as the complete progress state of the learner.

---

## 3.4. Lesson Completion

Source:

`src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java`

`LessonCompletion` records that a learner completed a specific Lesson.

The current completion flow in:

`src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java`

is:

```text
Validate Enrollment
        ↓
Find Course
        ↓
Find Lesson
        ↓
Check completion state
        ↓
Create LessonCompletion
        ↓
Update Enrollment Progress
```

This shows that `LessonCompletion` acts as a **completion record / learning evidence**.

However:

```text
Lesson completed
≠
Learner mastered the topic
```

Completion indicates that a learning activity was completed. It does not directly represent competency.

### Domain Conclusion

**LessonCompletion should be kept.**

It is a meaningful completion record and a source of evidence for Course Progress.

It may also become one source of evidence for learner-level state in the future.

---

## 3.5. Assessment

The current Learning domain contains:

```text
Quiz
Question
AnswerQuestion
QuizAttempt
UserAnswer
```

Relevant sources include:

* `src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java`
* `src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java`
* `src/main/java/com/deutschhub/domain/learning/model/entity/Question.java`
* `src/main/java/com/deutschhub/domain/learning/model/entity/AnswerQuestion.java`
* `src/main/java/com/deutschhub/domain/learning/model/entity/UserAnswer.java`

### Quiz

`Quiz` represents an assessment definition.

It manages concepts such as:

* Questions;
* scoring;
* passing score;
* difficulty;
* visibility;
* lifecycle;
* publication validation.

### QuizAttempt

`QuizAttempt` represents one learner's attempt at a Quiz.

It contains:

* `quizId`;
* `userId`;
* attempt status;
* answers;
* score;
* timestamps.

### UserAnswer

`UserAnswer` represents the learner's response to a Question.

Therefore:

```text
Quiz
=
What is being assessed?

QuizAttempt
=
One learner's attempt

UserAnswer
=
Detailed response evidence

Score
=
Result of one assessment attempt
```

A score is not the same as competency.

For example:

```text
Quiz Score = 90%
```

does not by itself establish:

```text
Learner's German ability = B1
```

### Current Implementation Limitation

Although the assessment domain model exists, a complete application and persistence flow for Quiz and QuizAttempt has not been found.

In particular, no complete implementation has been found for:

* Quiz repository ports;
* QuizAttempt repository ports;
* corresponding learner-facing use cases;
* complete persistence adapters for these aggregates.

Therefore:

```text
Assessment Domain Model
        exists

Assessment Capability
        incomplete
```

### Domain Conclusion

**Quiz and QuizAttempt should remain in the Learning Context for now.**

There is not enough evidence to justify separating Assessment into another bounded context at this stage.

---

## 3.6. UserProgress

Source:

`src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java`

`UserProgress` is the most important domain concept requiring further evaluation.

Its name suggests a learner-level concept:

> Progress of a user.

However, the current implementation contains:

```text
userId
courseId
enrollmentId
```

and manages:

```text
currentProgress
completedSections
completedLessons
totalStudyMinutes
startedAt
lastActivityAt
completedAt
status
```

This means that its current identity and responsibilities remain strongly **course-scoped**.

It does not currently represent a complete learner-level state.

---

# 4. Domain Findings

## 4.1. Course Progress vs Learner-level State

This is the most important distinction identified in the analysis.

### Course Progress

```text
How far have I gone in this Course?
```

The current source is:

```text
Enrollment
    └── Progress
```

### Learner-level State

```text
Where am I as a learner?
What have I learned?
How well can I perform?
Where am I weak?
What should I do next?
```

A learner-level state may require evidence from multiple learning areas:

```text
Courses
Lessons
Quizzes
Vocabulary
Grammar
Skills
Assessments
Mock Exams
```

Therefore:

> **Course Progress is one component of Learning State, not the complete Learning State.**

---

## 4.2. UserProgress Scope Mismatch

The current `UserProgress` implementation has a semantic mismatch:

```text
Name:
UserProgress

Actual identity:
User + Course + Enrollment
```

It also overlaps significantly with:

```text
Enrollment
    └── Progress
```

Both models contain course progress information such as:

* completed lessons;
* study time;
* progress;
* completion state.

This creates two competing representations:

```text
Model A

Enrollment
    └── Progress
```

and:

```text
Model B

UserProgress
    └── Progress
```

However, the current application flow uses Model A.

For example, `GetMyCourseProgressService` reads progress from `Enrollment`.

No complete application or persistence flow using `UserProgress` has been found.

### Domain Conclusion

`UserProgress` in its current form **should not be treated as a complete learner-level state**.

It needs to be redefined or consolidated after the target learner-state model is understood.

It should not simply be extended with additional fields without first resolving its domain responsibility.

---

## 4.3. Progress vs Competency

Another important distinction is:

```text
Progress
≠
Competency
```

For example:

```text
100% Course Progress
```

does not mean:

```text
100% German Competency
```

Similarly:

```text
Quiz Score = 90%
```

does not automatically mean:

```text
Learner Level = B1
```

Therefore, the Learning Context needs to distinguish between:

```text
Completion
Progress
Assessment
Competency
Level
```

Each concept answers a different business question.

---

## 4.4. Assessment Evidence vs Learner State

Assessment produces evidence.

For example:

```text
QuizAttempt
    ↓
Score
    ↓
Assessment Evidence
```

Turning that evidence into learner state requires interpretation:

```text
Evidence
    ↓
Evaluation / Interpretation
    ↓
Competency
    ↓
Current Level
```

The current implementation does not contain a complete model for this interpretation process.

Therefore:

> `QuizAttempt.score` should not be treated as the learner's Current Level.

---

## 4.5. Learning Direction Gap

The product direction indicates that learner state should eventually influence:

* next steps;
* recommendations;
* practice;
* progression.

However, no complete Learning Context implementation has been found for concepts such as:

```text
Recommendation
LearningPlan
NextActivity
AdaptiveLearning
LearningGoal
```

This represents a **domain capability gap**.

It does not, by itself, justify creating all of these concepts immediately.

The first question is:

> What business rules does Learning Direction actually need?

---

# 5. UserProgress Analysis

## 5.1. Current Responsibility

Based on the current implementation, `UserProgress` attempts to manage:

* course progress;
* completed Lessons;
* completed Sections;
* study time;
* learner activity timestamps;
* completion state.

However, most of these responsibilities already exist through:

```text
Enrollment
Progress
LessonCompletion
```

---

## 5.2. Responsibility Overlap

The current responsibilities can be summarized as:

| Concept          | Responsibility                       |
| ---------------- | ------------------------------------ |
| Enrollment       | Learner participation in a Course    |
| Progress         | Course-scoped progress state         |
| LessonCompletion | Lesson completion record             |
| UserProgress     | Another course-scoped progress state |

The clearest overlap is:

```text
Enrollment.Progress
        vs
UserProgress.currentProgress
```

Both represent essentially the same category of course progress.

---

## 5.3. Missing Learner-level Responsibilities

If `UserProgress` were intended to represent learner-level state, the current implementation would still lack important concepts such as:

```text
Current Level
Competency
Skill State
Learning Evidence
Learning Direction
Current Learning Focus
```

More importantly, these responsibilities should not automatically be placed into a single `UserProgress` aggregate.

The first design question should be:

> **Is learner-level state a genuine domain state, or is it a derived/read model aggregated from multiple sources?**

This decision should be made before defining its final structure.

---

## 5.4. Domain Decision

Based on the current evidence:

### `UserProgress` should not be kept in its current semantic form as the learner-level aggregate.

The following responsibilities should remain clearly separated:

1. `Enrollment + Progress` for Course-scoped learning progress.
2. `LessonCompletion` for Lesson completion evidence.
3. Assessment concepts for assessment evidence.
4. Learner-level state should be defined separately after its business meaning is established.
5. `UserProgress` should not currently be treated as a source of truth.

There is also a concrete implementation issue in the current code.

`UserProgress.create(...)` can construct the object with `currentProgress == null`, after which the constructor calls:

```text
Progress.createInitial(0)
```

However, `Progress` requires `totalLessons > 0`.

Therefore, the current `UserProgress` creation path is inconsistent with the invariant defined by `Progress`.

This is a confirmed implementation issue, but it should not be solved merely by patching the constructor before deciding whether `UserProgress` itself has the correct domain responsibility.

---

# 6. Domain Gaps

## 6.1. Learner Level

A learner-level Current Level model has not been found.

The current `CEFRLevel` concept is primarily used for content classification.

Therefore:

```text
Content CEFR Level
≠
Learner Current Level
```

---

## 6.2. Competency / Skills

No independent learner competency model has been found for:

* Vocabulary;
* Grammar;
* Reading;
* Listening;
* Speaking;
* Writing.

Therefore, the current implementation does not yet provide a complete representation of:

> "Where is the learner strong or weak?"

---

## 6.3. Learning Evidence

Several forms of evidence already exist:

```text
LessonCompletion
QuizAttempt
UserAnswer
Course Progress
```

However, they are not currently unified into a learner-level evidence model.

---

## 6.4. Learning Direction

No complete implementation has been found for:

* recommendations;
* learning plans;
* next activities;
* adaptive learning.

This is a future capability gap.

---

## 6.5. Assessment Flow

Assessment domain concepts already exist, but their application and persistence integration is incomplete.

Therefore:

```text
Assessment Domain Model
        exists

Assessment Capability
        incomplete
```

These two statements should remain separate.

---

# 7. Domain Decisions

This section converts the analysis into decisions that can be used in subsequent V3 design stages.

## 7.1. Keep

### Course

Keep `Course` as the aggregate responsible for the structured learning course.

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

### Enrollment

Keep `Enrollment` as the representation of learner participation in a specific Course.

### Progress

Keep `Progress` as a Course-scoped value object.

### LessonCompletion

Keep `LessonCompletion` as a Lesson completion record and source of learning evidence.

### Quiz / QuizAttempt

Keep the existing assessment concepts within the Learning Context for now.

There is currently insufficient evidence to introduce a separate Assessment Context.

### CEFRLevel

Keep `CEFRLevel` for content-level classification.

Do not use it to equate content level with learner Current Level.

---

## 7.2. Redefine / Consolidate

### UserProgress

Do not keep the current `UserProgress` semantics unchanged.

Its relationship with:

```text
Enrollment.Progress
```

must be resolved.

Further design should determine whether the learner-level concept should:

* replace the current `UserProgress`;
* consolidate some of its responsibilities elsewhere;
* or exist as a different domain/read-model concept.

This decision belongs to the Target Model stage.

---

## 7.3. Future Investigation

The following concepts require further investigation:

```text
Learner Current Level
Learner Competency
Learning Evidence
Level Progress
Learning Direction
```

In particular, the system needs to determine whether:

> **Learner-level state is genuine domain state or a derived/read model.**

This should be resolved before introducing a final aggregate structure.

---

# 8. Out of Scope / Not Yet Decided

This document does not yet decide:

* final aggregate boundaries;
* new database schema;
* new API contracts;
* final names for new domain entities;
* the exact Current Level calculation;
* the exact Competency calculation;
* recommendation algorithms;
* Learning Plans;
* Mock Exam models;
* whether a dedicated Learner aggregate is required;
* whether a dedicated Learner Learning State aggregate is required;
* how `UserProgress` should ultimately be migrated or refactored.

These decisions belong to later stages:

```text
Architecture Analysis
        ↓
Decision
        ↓
Target Model
        ↓
Refactoring
        ↓
Validation
```

---

# 9. Summary of the Current Domain Model

Based on the current implementation, the Learning Context can be understood as follows:

```text
                    LEARNING CONTEXT
                           │
          ┌────────────────┴────────────────┐
          │                                 │
   Course-based Learning              Assessment
          │                                 │
       Course                            Quiz
          │                                 │
       Section                       QuizAttempt
          │                                 │
       Lesson                         UserAnswer
          │
     LessonItem
          │
          │
      Enrollment
          │
       Progress
          │
  LessonCompletion
```

This represents the part of the domain that currently has relatively clear business meaning.

The learner-level part is still incomplete:

```text
                LEARNER-LEVEL LEARNING
                         │
          ┌──────────────┼──────────────┐
          │              │              │
     Current Level   Competency    Learning Evidence
          │              │              │
          └──────────────┼──────────────┘
                         │
                Learning Direction
```

`UserProgress` currently sits between these two scopes:

```text
Course-scoped Progress
        ↕
    UserProgress
        ↕
Learner-level State
```

This is why `UserProgress` is the main domain concept requiring further clarification in V3.

---

# 10. Conclusion

The analysis shows that the current Learning Context is **not fundamentally wrong as a course-based learning model**.

The existing flow:

```text
Course
→ Enrollment
→ LessonCompletion
→ Progress
```

has relatively clear responsibilities and is already used by the application and frontend.

The main issue appears when DeutschHub expands the meaning of Learning from:

> **"Learning through a Course"**

to:

> **"Understanding and guiding the learning journey of a learner."**

This broader interpretation introduces concepts such as:

```text
Current Level
Competency
Learning Evidence
Level Progress
Learning Direction
```

The current `UserProgress` model is not sufficient or clear enough to represent this broader learner-level state.

Therefore, the V3 Domain Analysis concludes:

> **The existing Course-based Learning model should not be discarded.**

> **Course Progress and Learner-level Learning State must remain conceptually distinct.**

> **The ambiguity and responsibility overlap of `UserProgress` must be resolved before expanding the Learning Context.**

> **Learner-level state, competency, Current Level, and Learning Direction should be designed from their business meaning rather than by simply extending the existing `UserProgress` model.**



