# Current Learning Implementation

## 1. Purpose

This document describes the **current state of the Learning implementation in DeutschHub**, based on the actual source code, application flows, persistence layer, database migrations, and V1 development history.

The purpose is to answer:

> **How is Learning currently implemented in DeutschHub?**

This document does not evaluate whether the current design is correct or incorrect, and it does not define a target domain model or architecture. Problems, gaps, and design decisions will be addressed in subsequent V3 documents.

---

## 2. Evolution from V1 to Current Implementation

### 2.1 V1 Started as a Course-Centered Learning Implementation

The V1 database migration explicitly identifies the schema as:

`src/main/resources/db/migration/V1__initial_schema.sql`

```sql
-- LEARNING CONTEXT V1
```

The initial Learning schema included:

```text
courses
course_sections
course_lessons
lesson_items
enrollments
lesson_completions
```

Git history also shows how the Learning implementation evolved:

```text
fbe8619 feat: add Course - the aggregate in Learning Context
54f8b84 feat: implement Lesson entity in Learning Context
c5613ce refactor: redesign Learning Context with proper Aggregate structure
3d2c779 feat: Implement Enrollment Aggregate base
a1df7b2 feat: Implement Aggregate UserProgress for Learning Context
a73aaa8 feat: Completed Quiz Aggregate
96bc2bf feat(domain): complete Learning Context V1
```

The `96bc2bf` commit describes the V1 Learning Context around concepts such as:

```text
Course
Section
Lesson
Enrollment
UserProgress
Quiz
```

Therefore, V1 did identify a **Learning Context**, but its implementation started from a **course-centered learning flow** and gradually introduced additional learner and assessment concepts.

---

## 3. Current Domain Model

The current Learning domain is located at:

`src/main/java/com/deutschhub/domain/learning/`

### 3.1 Aggregates

The current aggregate classes are located at:

`src/main/java/com/deutschhub/domain/learning/model/aggregate/`

```text
Certificate
Course
Enrollment
Quiz
QuizAttempt
UserProgress
```

These classes represent the domain concepts currently present in the Learning module.

---

### 3.2 Course Aggregate

`domain/learning/model/aggregate/Course.java`

The `Course` aggregate contains the course hierarchy:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

The aggregate currently contains behavior related to:

* updating course information
* adding sections
* deleting sections
* publishing
* unpublishing
* restoring sections
* soft deletion

The course also calculates estimated learning time from its lessons.

This shows that the course structure is explicitly modeled inside the current Learning domain.

---

### 3.3 Course Hierarchy

The course hierarchy entities are located at:

`src/main/java/com/deutschhub/domain/learning/model/entity/`

The relevant entities are:

```text
Section
Lesson
LessonItem
```

The current structure is:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

#### Section

`domain/learning/model/entity/Section.java`

A Section belongs to a Course and manages:

* title
* description
* order index
* lessons

It also provides behavior for:

* updating the section
* adding lessons
* restoring lessons
* changing order
* soft deletion

#### Lesson

`domain/learning/model/entity/Lesson.java`

A Lesson belongs to a Section and manages:

* title
* description
* estimated minutes
* CEFR level
* order index
* free preview
* lesson items

It provides behavior for:

* updating the lesson
* adding items
* removing items
* restoring items
* soft deletion

#### LessonItem

`domain/learning/model/entity/LessonItem.java`

A LessonItem is the content unit inside a Lesson.

The current model supports different item types through:

`domain/learning/model/enums/LessonItemType.java`

A LessonItem can reference:

* `mediaId`
* `quizId`

This shows that quizzes have already been considered as part of the learning content structure, although the current learner-facing Quiz workflow is not exposed as a complete Learning API flow.

---

## 4. Learner Participation

### 4.1 Enrollment

`domain/learning/model/aggregate/Enrollment.java`

Enrollment represents the learner's participation in a Course:

```text
User
  ↓
Enrollment
  ↓
Course
```

The current Enrollment model contains:

* `userId`
* `courseId`
* `status`
* `progress`
* `enrolledAt`
* `completedAt`
* `droppedAt`
* `expiredAt`

Enrollment status is defined in:

`domain/learning/model/enums/EnrollmentStatus.java`

Enrollment provides behavior related to:

* updating progress
* dropping an enrollment
* expiring an enrollment
* checking whether the enrollment is active

---

### 4.2 Lesson Completion

`domain/learning/model/entity/LessonCompletion.java`

LessonCompletion records the completion of a lesson by an enrolled learner:

```text
Enrollment
    ↓
LessonCompletion
    ↓
Lesson
```

It stores:

* enrollment ID
* lesson ID
* completion time

The persistence layer also contains a dedicated repository for lesson completion:

```text
application/learning/port/out/LessonCompletionRepositoryPort.java

infrastructure/learning/persistence/
├── JpaLessonCompletionRepositoryAdapter
├── SpringDataLessonCompletionRepository
└── LessonCompletionJpaEntity
```

Therefore, lesson completion is currently a concrete mechanism for recording learner progress through course content.

---

## 5. Progress

There are two different progress-related concepts in the current domain that need to be distinguished.

### 5.1 Enrollment Progress

`domain/learning/model/aggregate/Enrollment.java`

Enrollment contains:

```text
Progress
```

The value object is located at:

`domain/learning/model/valueobject/Progress.java`

The current Progress model represents information such as:

* completed lessons
* total lessons
* completion percentage
* total study minutes
* last updated time

This is the progress mechanism currently used by the application flow.

For example, `CompleteLessonService.java` performs the following flow:

```text
LessonCompletion is created
        ↓
Completed lessons are counted
        ↓
Study time is calculated
        ↓
Enrollment.updateProgress(...)
        ↓
Enrollment is saved
```

---

### 5.2 UserProgress Aggregate

The domain also contains:

`domain/learning/model/aggregate/UserProgress.java`

UserProgress contains concepts such as:

* user ID
* course ID
* enrollment ID
* current progress
* completed sections
* completed lessons
* total study minutes
* status
* start time
* last activity time
* completion time

It also provides behavior such as:

* `recordLessonCompletion`
* `updateProgress`
* `markAsCompleted`
* `recordSectionCompletion`

However, the current application and persistence layers do not show the same level of integration for UserProgress.

No current implementation was found for:

* `UserProgressRepositoryPort`
* `UserProgressJpaEntity`
* `UserProgressRepository`
* an application service that persists UserProgress

Instead, the current learning flow uses:

```text
Enrollment
+
LessonCompletion
```

to manage learner progress.

### Current-State Observation

Therefore:

> **Enrollment Progress is the progress mechanism currently used by the implemented application flow.**

`UserProgress` is a domain concept that exists in the current source code, but it has not been integrated into a complete application and persistence flow.

This observation does not determine whether UserProgress should be retained, removed, or redesigned.

---

## 6. Quiz and Certificate

The current domain also contains:

```text
Quiz
QuizAttempt
Certificate
```

under:

`domain/learning/model/aggregate/`

Related entities include:

```text
Question
AnswerQuestion
UserAnswer
```

under:

`domain/learning/model/entity/`

### Current Quiz State

The V1 documentation at:

`docs/V1/api-coverage.md`

described Quiz APIs as planned for a later version.

However, the current source code already contains the Quiz and QuizAttempt domain models.

Furthermore, `LessonItem` can reference a `quizId`.

### Confirmed

The Quiz domain model **exists in the current source code**.

### Not Confirmed as a Complete Learning Workflow

The current Learning controllers do not expose a complete learner-facing Quiz workflow such as:

```text
create quiz
publish quiz
start attempt
answer questions
submit attempt
score attempt
```

Therefore, the presence of Quiz domain classes should not be interpreted as evidence that a complete Quiz learning capability is currently implemented.

The same distinction applies to `Certificate`: the domain concept exists, but a complete learner-facing certificate workflow is not established by the current Learning controller surface.

---

## 7. Current Application Layer

The Learning application layer is located at:

`src/main/java/com/deutschhub/application/learning/`

The current use cases can be grouped as follows.

### Course Structure Management

```text
CreateCourse
UpdateCourse
DeleteCourse

AddSectionToCourse
UpdateSection
DeleteSection

AddLessonToSection
UpdateLesson
DeleteLesson

AddLessonItem
```

### Course Publication

```text
PublishCourse
UnpublishCourse
```

### Enrollment

```text
EnrollCourse
DropCourse
ExpireEnrollment
GetEnrollmentDetail
GetCourseEnrollments
```

### Learner Access

```text
GetMyCourses
GetMyCourseDetail
GetMyLessonDetail
GetViewerCourseDetail
GetPublishedCourses
GetPublishedCourseDetail
```

### Learning Progress

```text
CompleteLesson
GetMyCourseProgress
GetCompletedLessons
```

### Media Access

```text
GetMyLessonItemMedia
```

These use cases represent the currently implemented application-level Learning capabilities.

---

## 8. Current HTTP API Surface

The Learning controllers are located at:

`src/main/java/com/deutschhub/infrastructure/learning/web/controller/`

### 8.1 Public Course APIs

`CourseController.java`

```text
GET  /api/v1/courses
GET  /api/v1/courses/{courseId}
GET  /api/v1/courses/{courseId}/viewer
POST /api/v1/courses/{courseId}/enroll
```

These APIs support:

```text
Browse course
View course
Preview course
Enroll
```

---

### 8.2 My Learning APIs

`MyLearningController.java`

```text
GET  /api/v1/me/courses
GET  /api/v1/me/courses/{courseId}

POST /api/v1/me/courses/{courseId}/lessons/{lessonId}/complete

GET  /api/v1/me/courses/{courseId}/progress
GET  /api/v1/me/courses/{courseId}/completed-lessons

POST /api/v1/me/courses/{courseId}/drop

GET  /api/v1/me/courses/{courseId}/lessons/{lessonId}

GET  /api/v1/me/courses/{courseId}/lessons/{lessonId}/items/{itemId}/media
```

This is the clearest learner-facing representation of the current Learning flow.

---

### 8.3 Admin Course Management

`AdminCourseController.java`

The current controller supports:

```text
Course CRUD
Section CRUD
Lesson CRUD
LessonItem creation
Course publish/unpublish
Course enrollments
```

`AdminEnrollmentController.java` provides operations related to:

```text
Enrollment detail
Enrollment expiration
```

---

## 9. Current Persistence Model

The Learning persistence layer is located at:

`src/main/java/com/deutschhub/infrastructure/learning/persistence/`

The current Learning repository ports include:

```text
CourseRepositoryPort
EnrollmentRepositoryPort
LessonCompletionRepositoryPort
```

Their persistence implementations include:

```text
JpaCourseRepositoryAdapter
JpaEnrollmentRepositoryAdapter
JpaLessonCompletionRepositoryAdapter
```

The corresponding JPA entities include:

```text
CourseJpaEntity
SectionJpaEntity
LessonJpaEntity
LessonItemJpaEntity

EnrollmentJpaEntity
LessonCompletionJpaEntity
```

The current persistence structure can therefore be summarized as:

```text
Course
├── Section
│   └── Lesson
│       └── LessonItem
│
Enrollment
│
└── LessonCompletion
```

Progress is currently persisted through the Enrollment persistence model rather than through a separate UserProgress persistence model.

---

## 10. Current Learning Flows

### 10.1 Course-Based Learning Flow

The current application services establish the following learner flow:

```text
Browse Published Course
        ↓
View Course
        ↓
Enroll
        ↓
Enrollment Created
        ↓
My Courses
        ↓
Open Course
        ↓
Open Lesson
        ↓
Complete Lesson
        ↓
LessonCompletion Created
        ↓
Enrollment Progress Updated
```

The main application services involved include:

* `EnrollCourseService.java`
* `GetMyCoursesService.java`
* `GetMyCourseDetailService.java`
* `GetMyLessonDetailService.java`
* `CompleteLessonService.java`
* `GetMyCourseProgressService.java`

---

### 10.2 Lesson Completion Flow

`CompleteLessonService.java` currently performs a flow equivalent to:

```text
Find Enrollment
        ↓
Check Enrollment is Active
        ↓
Find Course
        ↓
Find Lesson
        ↓
Check Lesson is Not Completed
        ↓
Create LessonCompletion
        ↓
Count Completed Lessons
        ↓
Update Enrollment Progress
        ↓
Save Enrollment
```

### Current-State Observation

The current progress mechanism is therefore based on lesson completion and reflected back into the learner's Enrollment.

---

## 11. Current Learning Implementation — Summary

Based on the current implementation, the Learning module can be described at the implementation level as follows:

```text
Current Learning Implementation
│
├── Learning Content Structure
│   └── Course
│       └── Section
│           └── Lesson
│               └── LessonItem
│
├── Learner Participation
│   └── Enrollment
│
├── Learning Completion
│   └── LessonCompletion
│
├── Learning Progress
│   └── Enrollment → Progress
│
├── Assessment Domain
│   ├── Quiz
│   ├── QuizAttempt
│   ├── Question
│   └── UserAnswer
│
└── Completion Credential
    └── Certificate
```

However, the current source distinguishes between concepts that participate in an implemented application/persistence flow and concepts that currently exist primarily as domain models.

### Implemented Application and Persistence Flow

```text
Course
Section
Lesson
LessonItem
Enrollment
LessonCompletion
Enrollment Progress
```

### Domain Concepts Currently Present

```text
UserProgress
Quiz
QuizAttempt
Certificate
Question
UserAnswer
```

The second group should not automatically be interpreted as fully implemented learner-facing capabilities.

---

## 12. Current-State Observation

Based on the current implementation:

> **DeutschHub currently implements Learning primarily around a course-based learning flow.**

The central implemented flow is:

```text
Course
  ↓
Enrollment
  ↓
Lesson
  ↓
Lesson Completion
  ↓
Progress
```

In this implementation:

* **Course** organizes learning content.
* **Enrollment** represents learner participation in a course.
* **LessonCompletion** records completed learning content.
* **Progress** represents the learner's progress within the enrollment.

At the same time, the domain already contains additional concepts such as `UserProgress`, `Quiz`, `QuizAttempt`, and `Certificate`. These concepts do not currently form complete learner-facing workflows equivalent to the Course / Enrollment / Completion flow.

---

## 13. What This Document Does Not Decide

This document does not decide:

* whether Course should remain the center of the Learning Context
* whether Enrollment belongs to the Learning Context
* whether UserProgress should exist
* whether Progress should belong to Enrollment or another model
* whether Quiz belongs to the Learning Context or another Context
* whether Certificate belongs to the Learning Context or another Context
* whether Learning should include Vocabulary, Grammar, or Skill learning
* whether Learning should move beyond the current course-based model
* whether the current aggregate boundaries are appropriate as target boundaries

These questions belong to the subsequent V3 analysis and decision process.
