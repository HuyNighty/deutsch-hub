# Current Learning Boundaries

## 1. Purpose

This document identifies the **current boundaries of the Learning Context in DeutschHub** based on:

* findings from Learning Discovery;
* the current Domain, Application, and Infrastructure implementation;
* business capabilities currently supported by the system.

The purpose of this document is to answer:

> **What business responsibilities does the current Learning Context actually cover?**

This document describes the **Current State**.

It does not define:

* the Target Learning Context;
* Target Bounded Contexts;
* Target Aggregates;
* Target Architecture;
* or the refactoring approach.

Those decisions will be addressed in `target-learning-boundaries.md` and subsequent design stages.

---

# 2. Basis for Identifying the Boundary

Current Learning Boundaries are identified from two main sources.

### 2.1. Product / Learning Discovery

`learning-discovery.md` shows that Learning Experience can be viewed through four dimensions:

```text
Learning Experience
│
├── Learning Structure
├── Learning Activities
├── Learner State
└── Learning Direction
```

This provides the business/product perspective used to investigate the scope of Learning.

### 2.2. Current Implementation

The current source code shows that Learning already implements several concrete capabilities.

These capabilities are represented across:

```text
Domain
Application
Infrastructure
Web
Database
```

Therefore, the current boundary is not determined simply by the existence of the `learning` package.

It is determined by:

> **The business responsibilities that Learning currently owns and executes.**

---

# 3. Current Learning Boundary

Based on product discovery and the current implementation, the Learning Context can currently be viewed as:

```text
                         Learning Context
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
 Learning Structure      Learner Participation    Learner State
        │                       │                       │
     Course                 Enrollment             Progress
        │                                             │
     Section                                      LessonCompletion
        │
      Lesson
        │
    LessonItem

        │
        ├──────────────────────────────────────────────
        │
   Assessment / Certification
        │
   Domain concepts only
```

However, this is a **capability-oriented view of the current boundary**. It does not mean that all capabilities above have the same level of implementation completeness.

---

# 4. Boundary 1 — Learning Structure

## 4.1. Current Responsibility

Learning currently owns a clear learning structure:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

Relevant sources:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java

src/main/java/com/deutschhub/domain/learning/model/entity/Section.java

src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java

src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
```

The `Course` aggregate manages:

* Course metadata;
* Sections;
* Lessons;
* LessonItems;
* publication;
* lifecycle;
* estimated learning hours.

The Application layer also provides corresponding use cases such as:

```text
CreateCourse
UpdateCourse
DeleteCourse
PublishCourse
UnpublishCourse

AddSectionToCourse
UpdateSection
DeleteSection

AddLessonToSection
UpdateLesson
DeleteLesson

AddLessonItem
```

Therefore:

> **Structured Course Learning is the clearest currently implemented responsibility within the Learning Context.**

---

## 4.2. Course Is Not the Entire Learning Context

Although Course is the most complete capability, `learning-discovery.md` shows that Learning Experience may involve other forms of learning structure:

```text
Course
Vocabulary
Grammar
Skills
```

However, in the current implementation:

* no Vocabulary learning model has been found;
* no Grammar learning model has been found;
* no learner Skill model has been found.

Therefore:

```text
Learning Structure
│
├── Course            Current
├── Vocabulary        Not found
├── Grammar           Not found
└── Skills            Not found
```

### Boundary Finding

The current Learning Context **clearly owns Course-based Learning Structure**.

Other learning structures are currently product-level observations and cannot be confirmed as implemented boundaries.

---

# 5. Boundary 2 — Learner Participation

## 5.1. Enrollment

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Enrollment.java
```

`Enrollment` represents a learner's participation in a Course.

It contains:

```text
userId
courseId
status
Progress
lifecycle timestamps
```

Enrollment also manages its participation lifecycle:

```text
ENROLLED
    ↓
IN_PROGRESS
    ↓
COMPLETED

DROPPED
EXPIRED
```

The Application layer provides capabilities such as:

```text
EnrollCourse
DropCourse
ExpireEnrollment

GetEnrollmentDetail
GetCourseEnrollments
GetMyCourses
GetMyCourseDetail
```

Infrastructure also provides a dedicated persistence boundary:

```text
EnrollmentRepositoryPort
        ↓
JpaEnrollmentRepositoryAdapter
        ↓
EnrollmentJpaEntity
```

### Boundary Finding

> **Learner participation in a Course is a current responsibility of the Learning Context.**

Enrollment is not merely a database relationship between User and Course. It represents the learner's participation lifecycle in a specific Course.

---

# 6. Boundary 3 — Course Learning State

Current Learning also owns responsibilities related to the learner's state within a Course.

## 6.1. Progress

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/valueobject/Progress.java
```

`Progress` currently contains:

```text
completedLessons
totalLessons
completionPercentage
totalStudyMinutes
lastUpdatedAt
```

Progress is owned by:

```text
Enrollment
    └── Progress
```

Therefore, the current meaning of Progress is:

> **The learner's progress within a specific Course / Enrollment.**

---

## 6.2. Lesson Completion

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/LessonCompletion.java
```

`LessonCompletion` records:

```text
enrollmentId
lessonId
completedAt
```

The Application flow is:

```text
CompleteLessonService
        ↓
Create LessonCompletion
        ↓
Update Enrollment Progress
        ↓
Save Enrollment
```

Infrastructure provides dedicated persistence for completion records:

```text
LessonCompletionRepositoryPort
        ↓
JpaLessonCompletionRepositoryAdapter
```

Therefore, LessonCompletion is a concrete completion record in the current system.

---

## 6.3. Boundary Finding

Current learner state within Learning is primarily:

```text
Enrollment
    │
    ├── Progress
    │
    └── LessonCompletion
```

However, an important distinction must be maintained:

```text
Course Progress
≠
Complete Learner State
```

The current implementation can answer reasonably well:

> "How much has the learner completed in this Course?"

It cannot yet fully answer:

> "Where is this learner in their overall German learning journey?"

---

# 7. Boundary 4 — Learning Activities

`learning-discovery.md` identifies Learning Activities such as:

```text
Learning
Practice
Review
Listening
Speaking
Reading
Writing
```

and activities involving video or authentic content.

## 7.1. Current Implementation

The current implementation contains learner behaviors related to consuming learning content:

```text
CompleteLesson
GetCompletedLessons
GetMyLessonDetail
GetMyLessonItemMedia
```

In particular:

```text
src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java
```

shows the following flow:

```text
Access Lesson
    ↓
Complete Lesson
    ↓
Create completion record
    ↓
Update progress
```

`GetMyLessonItemMediaService` also shows that Media can be accessed as part of the learner's Course/Lesson experience.

---

## 7.2. No Clear Activity Model Yet

No independent domain concepts or application capabilities have been found for:

```text
Practice
Review
Listening Activity
Speaking Activity
Reading Activity
Writing Activity
```

Therefore:

```text
Learning Activities
│
├── Lesson consumption       Current behavior
├── Lesson completion        Current
├── Media-based learning     Current capability
├── Practice                 Not found
├── Review                   Not found
├── Listening activity       Not found
├── Speaking activity        Not found
├── Reading activity         Not found
└── Writing activity         Not found
```

### Boundary Finding

Current Learning contains **learning activity behavior**, but it does not yet have a clearly modeled **Learning Activity boundary**.

This does not provide enough evidence to conclude that Activities should become a separate Aggregate or Bounded Context.

The current evidence only supports:

> Learners can interact with Course/Lesson content, but activity abstraction has not yet been clearly modeled in the code.

---

# 8. Boundary 5 — Assessment

The current Learning Domain contains:

```text
Quiz
QuizAttempt
Question
AnswerQuestion
UserAnswer
```

Relevant sources:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Quiz.java

src/main/java/com/deutschhub/domain/learning/model/aggregate/QuizAttempt.java
```

This represents a capability distinct from Course Progress.

Conceptually:

```text
Quiz
    ↓
QuizAttempt
    ↓
UserAnswer
    ↓
Score
```

However, the current implementation does not provide a complete Application/Infrastructure flow.

No complete implementation has been found for:

```text
QuizRepositoryPort
QuizAttemptRepositoryPort

Quiz Application Services

Quiz persistence adapters
Quiz persistence entities
```

Therefore:

```text
Assessment
    │
    ├── Domain Model         Exists
    ├── Application          Not complete
    └── Infrastructure       Not found
```

### Boundary Finding

Assessment is **a capability that has appeared in the Learning Domain**, but it is not currently a complete implemented boundary.

---

# 9. Boundary 6 — Certification

The current Domain contains:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Certificate.java
```

Certificate contains concepts such as:

```text
userId
courseId
issuedAt
certificateNumber
```

However, no complete implementation has been found for:

* Application use cases;
* repository ports;
* persistence entities;
* persistence adapters.

Therefore:

```text
Certification
    │
    ├── Domain Model         Exists
    ├── Application          Not found
    └── Infrastructure       Not found
```

### Boundary Finding

Certification is currently a **domain concept**, not a fully implemented capability.

---

# 10. Boundary 7 — Learner-level State

This is the least clearly defined boundary in the current implementation.

`learning-discovery.md` shows that Learner State may include:

```text
Progress
Competency
Vocabulary learned
Grammar progress
XP
Level
Streak
Achievements
Learning statistics
```

The current implementation clearly represents only part of this:

```text
Course Progress
Lesson Completion
Study Time
```

---

## 10.1. UserProgress

Source:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

The name `UserProgress` suggests a learner-level concept.

However, its identity fields are:

```text
userId
courseId
enrollmentId
```

and it contains:

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

Therefore, its actual scope remains strongly related to:

```text
User
  +
Course
  +
Enrollment
```

Meanwhile, the current Application flow uses:

```text
Enrollment
    └── Progress
```

for Course Progress.

---

## 10.2. Current Boundary Finding

The following can be confirmed:

```text
Course-level learner state
        Current
```

But the following cannot yet be confirmed:

```text
Learner-level Learning State
         Not fully implemented
```

In particular, no complete implementation has been found for:

```text
Learner Competency
Learner Current Level
Skill State
Vocabulary State
Grammar State
Learning Achievement
```

Therefore, `UserProgress` is currently considered:

> **A domain concept whose boundary responsibility is unclear, rather than evidence of a complete Learner State boundary.**

---

# 11. Boundary 8 — Learning Direction

`learning-discovery.md` identifies Learning Direction concepts such as:

```text
Daily Learning
Learning Plan
Recommendations
Review due
Weakness-oriented Practice
Learning Goals
Exam Preparation
```

The current implementation does not contain complete domain/application capabilities for these responsibilities.

No implementation has been found for:

```text
LearningPlan
LearningGoal
Recommendation
DailyPlan
NextActivity
ExamPreparation
```

Therefore:

```text
Learning Direction
    │
    ├── Domain              Not found
    ├── Application         Not found
    └── Infrastructure      Not found
```

### Boundary Finding

Learning Direction is **not currently an implemented Learning capability**.

It currently exists at the level of:

```text
Product / Learning Discovery
```

rather than as a confirmed implementation boundary.

---

# 12. Current Boundary Map

The current Learning Context can therefore be summarized as:

```text
                         CURRENT LEARNING CONTEXT
                                  │
          ┌───────────────────────┼────────────────────────┐
          │                       │                        │
          ▼                       ▼                        ▼
 Learning Structure       Learner Participation     Course Learning State
          │                       │                        │
       Course                Enrollment              Progress
          │                                              │
       Section                                       LessonCompletion
          │
       Lesson
          │
    LessonItem
          │
          └───────────────┐
                          │
                          ▼
                    Learning Experience
                          │
                  Lesson Consumption
                  Lesson Completion
                  Media Access

                          │
              ┌───────────┴───────────┐
              ▼                       ▼
         Assessment              Certification
        Domain only              Domain only

              │
              ▼
       Learner-level State
          (ambiguous)

              │
              ▼
      Learning Direction
       (not implemented)
```

---

# 13. Current Boundary Classification

Current Learning capabilities can be classified into three levels.

## 13.1. Clearly Implemented

The following responsibilities have relatively clear business meaning and implementation:

```text
Course / Structured Learning
Enrollment
Course Progress
Lesson Completion
Learner Course Experience
```

These capabilities have evidence across multiple layers:

```text
Domain
  ↓
Application
  ↓
Infrastructure
  ↓
Web
```

---

## 13.2. Domain Exists but Capability Is Incomplete

```text
Assessment
Certification
UserProgress
```

These concepts exist in the Domain but do not have complete vertical flows.

---

## 13.3. Product-level Capability Not Yet Implemented

```text
Vocabulary
Grammar
Skills

Practice
Review
Listening
Speaking
Reading
Writing

Competency
Learner Current Level
Achievements

Daily Learning
Learning Plan
Recommendations
Exam Preparation
```

These capabilities appear in Learning Discovery but have not been found in the current Learning implementation.

---

# 14. Current Learning Boundary vs Course Boundary

An important finding is:

> **The current Learning Context is broader than Course Management, but Course remains the center of gravity of the implementation.**

The current implementation can be represented as:

```text
                         Learning
                            │
                ┌───────────┴───────────┐
                │                       │
          Course Structure       Learner Participation
                │                       │
             Course                 Enrollment
                │                       │
             Section                Progress
                │                       │
             Lesson             LessonCompletion
                │
           LessonItem
```

Therefore, it would be inaccurate to describe:

```text
Learning = Course
```

However, there is also not enough evidence to claim:

```text
Learning = Full Learner Learning Platform
```

The current reality is between these two extremes:

> **Learning is currently a Course-centered Learning Context with Enrollment, Course Progress, Lesson Completion, Learner Experience, and several domain concepts for Assessment and Certification.**

---

# 15. Current Boundary Findings

The analysis produces the following findings.

### Finding 1 — Course is the clearest current boundary

Course Structure is a complete capability with implementation across:

```text
Domain
Application
Infrastructure
Web
Database
```

---

### Finding 2 — Enrollment is a clear learner participation boundary

Enrollment has its own lifecycle, Application services, and persistence boundary.

---

### Finding 3 — Progress is currently Course-scoped

Progress belongs to Enrollment and represents progress within a specific Course.

It does not represent the learner's complete learning state.

---

### Finding 4 — LessonCompletion is completion evidence

LessonCompletion records the completion of a Lesson.

It does not directly represent competency or mastery.

---

### Finding 5 — Learning Activities do not yet have a clear boundary abstraction

The current system supports learner interaction with Lessons and Media, but there is no clear model for Practice, Review, or skill-based activities.

---

### Finding 6 — Assessment exists but is incomplete

Quiz and QuizAttempt exist in the Domain but do not currently have complete Application and Infrastructure implementation.

---

### Finding 7 — UserProgress is a boundary ambiguity

`UserProgress` has a learner-oriented name, but its current implementation is Course/Enrollment-scoped and overlaps with `Enrollment.Progress`.

---

### Finding 8 — Learner-level Learning State is not fully implemented

Concepts such as:

```text
Competency
Current Level
Skill State
Vocabulary State
Grammar State
```

have not been found in the current implementation.

---

### Finding 9 — Learning Direction is not part of the current implementation

Daily Learning, Learning Plan, Recommendation, and Exam Preparation currently exist only as product-level concepts identified during discovery.

---

# 16. Current Boundary Statement

Based on the current codebase, the Learning Context can be described as:

> **The current Learning Context is primarily responsible for organizing Course-based learning, managing learner participation through Enrollment, recording Lesson completion, and maintaining Course-scoped learning progress.**

Beyond these implemented responsibilities, the Domain model has started to contain concepts for:

```text
Assessment
Certification
UserProgress
```

but these capabilities are incomplete or do not yet have sufficiently clear responsibilities.

At the product level, Learning Discovery shows that Learning may also encompass:

```text
Learning Activities
Learner-level State
Learning Direction
```

but these areas cannot currently be confirmed as implemented Learning boundaries.

---

# 17. What This Current Boundary Tells Us

The current boundary reveals an important difference between the product view and the implementation:

```text
PRODUCT VIEW

Learning
├── Structure
├── Activities
├── Learner State
└── Learning Direction
```

while:

```text
CURRENT IMPLEMENTATION

Learning
├── Course Structure
├── Enrollment
├── Course Progress
├── Lesson Completion
├── Learner Experience
├── Assessment (partial)
├── Certification (partial)
└── UserProgress (ambiguous)
```

The gap between these two views is an important subject for further investigation.

However:

> **This gap is not yet the Target Architecture.**

It only shows that the current Learning boundary does not fully represent the broader Learning Experience identified during discovery.

---

# 18. Conclusion

The current Learning Context has a relatively clear core:

```text
Course
   ↓
Enrollment
   ↓
LessonCompletion
   ↓
Progress
```

This part is implemented and supported across the Domain, Application, and Infrastructure layers.

At the same time, the Learning Domain has begun to contain broader concepts:

```text
Quiz
QuizAttempt
Certificate
UserProgress
```

but these concepts are incomplete or do not yet have sufficiently clear responsibilities.

At the product level, Learning Discovery expands the scope of Learning into:

```text
Learning Structure
        +
Learning Activities
        +
Learner State
        +
Learning Direction
```

The current implementation clearly covers the first area and parts of Learner State and Learning Experience.

Therefore:

> **The current Learning Context is a Course-centered Learning Context, but it is not equivalent to a Course Context.**

> **Its current boundary already extends beyond Course Management through Enrollment, Progress, Lesson Completion, and Learner Experience.**

> **However, Learner-level State, Learning Activities, and Learning Direction have not yet become fully modeled responsibilities in the current implementation.**

This establishes the baseline for:

`target-learning-boundaries.md`
