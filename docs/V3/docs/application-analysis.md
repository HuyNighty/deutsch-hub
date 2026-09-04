# Application Analysis

## 1. Purpose

This document analyzes the Learning Application layer based on the current source code, with the following objectives:

- identify the currently implemented use cases;
- determine which business capabilities those use cases support;
- examine the relationship between the Application layer and the Domain model;
- identify the persistence boundaries represented by Output Ports;
- distinguish fully implemented capabilities from domain-level concepts;
- and, most importantly, determine whether the current Learning implementation is truly limited to a Course Context or already represents a broader Learning Context.

The primary scope of this analysis is:

```text
src/main/java/com/deutschhub/application/learning/
````

The Application layer is analyzed together with:

```text
src/main/java/com/deutschhub/domain/learning/
src/main/java/com/deutschhub/infrastructure/learning/
```

---

# 2. Current Learning Application Layer

The Learning Application layer currently contains:

* **31 Input Ports / Use Cases**;
* **31 corresponding Application Services**;
* **3 Output Ports** for persistence:

    * `CourseRepositoryPort`;
    * `EnrollmentRepositoryPort`;
    * `LessonCompletionRepositoryPort`.

The current structure is:

```text
application/learning/
├── dto/
│   ├── request/
│   └── response/
├── port/
│   ├── in/
│   │   └── 31 Use Cases
│   └── out/
│       ├── CourseRepositoryPort
│       ├── EnrollmentRepositoryPort
│       └── LessonCompletionRepositoryPort
└── service/
    └── 31 Application Services
```

Each Input Port is currently implemented by a corresponding Application Service.

This demonstrates a relatively clear separation between:

```text
Inbound Use Case
        ↓
Application Service
        ↓
Outbound Dependency
```

### Assessment

The Application layer already contains a substantial number of use cases representing different Learning behaviors.

However, the services are currently grouped together under:

```text
application/learning/service/
```

rather than being organized explicitly around business capabilities.

Therefore:

> **The use-case inventory is relatively clear, but the business topology of Learning is not directly expressed by the package structure.**

---

# 3. Course / Structured Learning

Course-related functionality is the largest and most completely implemented capability in the current Application layer.

## 3.1. Course Lifecycle

The current use cases include:

```text
CreateCourse
UpdateCourse
DeleteCourse
PublishCourse
UnpublishCourse
GetCourses
GetCourseDetail
GetPublishedCourses
GetPublishedCourseDetail
GetViewerCourseDetail
```

These use cases primarily operate through:

```text
CourseRepositoryPort
```

and the domain aggregate:

```text
Course
```

---

## 3.2. Course Structure

The Application layer also provides use cases for managing the internal structure of a Course:

```text
AddSectionToCourse
UpdateSection
DeleteSection
GetCourseSection
GetSectionLessons

AddLessonToSection
UpdateLesson
DeleteLesson

AddLessonItem
```

These use cases operate around the structure:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

This corresponds to the Domain model under:

```text
src/main/java/com/deutschhub/domain/learning/model/
```

and to the persistence graph:

```text
CourseJpaEntity
 └── SectionJpaEntity
      └── LessonJpaEntity
           └── LessonItemJpaEntity
```

### Assessment

Application, Domain, and Infrastructure all express a relatively clear boundary around **Structured Learning / Course Content**.

This is currently the most mature capability within Learning.

---

# 4. Learner Enrollment

Beyond Course Management, the Application layer contains a distinct group of use cases around learner participation in a Course:

```text
EnrollCourse
DropCourse
ExpireEnrollment
GetCourseEnrollments
GetEnrollmentDetail
GetMyCourses
GetMyCourseDetail
GetMyCourseProgress
```

The corresponding services include:

```text
EnrollCourseService
DropCourseService
ExpireEnrollmentService
GetCourseEnrollmentsService
GetEnrollmentDetailService
GetMyCoursesService
GetMyCourseDetailService
GetMyCourseProgressService
```

The corresponding Domain model is:

```text
Enrollment
```

with:

```text
userId
courseId
Progress
status
```

and the lifecycle:

```text
ENROLLED
IN_PROGRESS
COMPLETED
DROPPED
EXPIRED
```

Infrastructure also provides an independent persistence boundary:

```text
EnrollmentJpaEntity
JpaEnrollmentRepositoryAdapter
SpringDataEnrollmentRepository
```

### Assessment

Enrollment is not merely a field or supporting entity of Course.

It has:

* its own lifecycle;
* its own application use cases;
* its own repository port;
* its own persistence adapter;
* its own persistence entity.

Therefore:

> **Enrollment already represents a significant business boundary within the Learning context.**

---

# 5. Learning Progress

Learning progress is currently represented through:

```text
Progress
LessonCompletion
Enrollment.progress
```

and through the use cases:

```text
CompleteLesson
GetCompletedLessons
GetMyCourseProgress
```

The most important flow is `CompleteLessonService`.

The current flow can be summarized as:

```text
Complete Lesson
       ↓
Validate Enrollment
       ↓
Load Course
       ↓
Find Lesson
       ↓
Check LessonCompletion
       ↓
Create LessonCompletion
       ↓
Calculate Progress
       ↓
Update Enrollment.progress
       ↓
Save Enrollment
```

The service uses:

```text
CourseRepositoryPort
EnrollmentRepositoryPort
LessonCompletionRepositoryPort
```

The corresponding Infrastructure layer contains:

```text
EnrollmentJpaEntity
LessonCompletionJpaEntity
```

and their persistence adapters.

### Assessment

The Application and Infrastructure layers show that Progress is currently primarily:

> **course/enrollment-scoped learning progress**

rather than an independent learner-level state.

This is consistent with the findings from the Domain Analysis.

---

# 6. Learner Learning Experience

Another group of use cases demonstrates that Learning is not limited to Course Management.

Examples include:

```text
GetMyCourses
GetMyCourseDetail
GetMyCourseProgress
GetMyLessonDetail
GetMyLessonItemMedia
GetCompletedLessons
CompleteLesson
```

These use cases directly support the learner's interaction with learning content.

This can be distinguished as:

```text
Course Management
        ↓
Create / manage the learning offering


Learner Learning Experience
        ↓
Access / consume / progress through the learning offering
```

This distinction is also reflected in the Web layer:

```text
AdminCourseController
AdminEnrollmentController
CourseController
MyLearningController
```

In particular:

```text
MyLearningController
```

uses learner-facing use cases rather than only performing Course administration.

### Assessment

This provides important evidence that:

> **The current Learning context already contains learner-facing behavior beyond Course Management.**

---

# 7. Assessment

The Domain layer already contains a relatively clear Assessment model:

```text
Quiz
 └── Question
      └── AnswerQuestion

QuizAttempt
 └── UserAnswer
```

However, when examining the Application layer:

```text
src/main/java/com/deutschhub/application/learning/port/in/
```

no corresponding use cases were found for:

```text
Quiz
QuizAttempt
Question
UserAnswer
Score
```

Likewise, under:

```text
src/main/java/com/deutschhub/application/learning/port/out/
```

no corresponding repository ports were found.

The Infrastructure layer also does not contain corresponding persistence components.

### Assessment

Assessment must therefore be distinguished as:

```text
Domain Concept
        ↓
exists
```

while:

```text
Application Capability
        ↓
not found
```

and:

```text
Persistence Implementation
        ↓
not found
```

Therefore:

> **Assessment currently exists at the domain-model level, but it is not implemented as a complete Application/Infrastructure capability.**

Assessment should not be described as a fully implemented subsystem of the current Learning implementation.

It is evidence that the Domain model has already begun to expand beyond the currently implemented Course/Enrollment/Progress flows.

---

# 8. Certification

`Certificate` currently exists as an aggregate under:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Certificate.java
```

However, no corresponding Application use cases were found for certificate-related operations such as issuing or retrieving certificates.

No corresponding persistence implementation was found either.

Therefore:

> **Certification currently exists as a domain-level concept, but not as a complete Application capability.**

This is similar to the current state of Assessment.

---

# 9. UserProgress

`UserProgress` exists in the Domain layer:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/UserProgress.java
```

However, no corresponding:

* `UserProgressRepositoryPort`;
* Application Service directly operating on `UserProgress`;
* persistence adapter;
* persistence entity

was found.

At the same time, the implemented Learning flows use:

```text
Enrollment.progress
```

for course progress management.

This creates two concepts in the Domain model:

```text
Enrollment
    └── Progress
```

and:

```text
UserProgress
```

but only the first model is integrated into the current Application and Infrastructure flows.

### Assessment

This reinforces the finding from the Domain Analysis:

> `UserProgress` currently has ambiguous responsibility and is not fully integrated into the Application architecture.

Its name suggests learner-level state, while its current fields:

```text
userId
courseId
enrollmentId
```

indicate a course/enrollment-scoped state.

The actual application flow continues to rely on:

```text
Enrollment.progress
```

---

# 10. Persistence Boundaries Revealed by the Application Layer

The Learning Application layer currently defines three Output Ports:

```text
CourseRepositoryPort
EnrollmentRepositoryPort
LessonCompletionRepositoryPort
```

These correspond directly to the Infrastructure adapters:

```text
CourseRepositoryPort
        ↓
JpaCourseRepositoryAdapter

EnrollmentRepositoryPort
        ↓
JpaEnrollmentRepositoryAdapter

LessonCompletionRepositoryPort
        ↓
JpaLessonCompletionRepositoryAdapter
```

These persistence boundaries show that:

```text
Course
```

has its own persistence boundary;

```text
Enrollment
```

has its own persistence boundary;

and:

```text
LessonCompletion
```

also has its own persistence boundary.

Course and Enrollment are therefore not persisted as a single aggregate.

Their current relationship is essentially:

```text
Course
   ↑
   │ courseId
   │
Enrollment
   │
   └── Progress
```

This provides further evidence that Course and Enrollment are distinct business objects with separate boundaries, even though they are directly related.

---

# 11. Infrastructure Confirms the Course-Centric Center of Gravity

The current persistence structure:

```text
courses
course_sections
course_lessons
lesson_items
```

forms a clear hierarchy:

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

while learner state is persisted separately through:

```text
enrollments
lesson_completions
```

The resulting persistence topology can be represented as:

```text
Learning Content
        │
      Course
        │
     Section
        │
      Lesson
        │
    LessonItem
```

and:

```text
Learner State
        │
    Enrollment
        │
     Progress
        │
 LessonCompletion
```

Application services connect these two topologies through use cases such as `CompleteLesson`.

### Assessment

This provides strong evidence for the following finding:

> **Learning is broader than Course, but Course remains the strongest implemented aggregate and the architectural center of gravity.**

---

# 12. Current Business Capability Map from the Application Layer

Based on the use cases that are actually implemented, the Learning Application layer can be provisionally grouped into:

```text
Learning
│
├── 1. Course / Structured Learning
│   ├── Course lifecycle
│   ├── Section management
│   ├── Lesson management
│   └── Lesson Item management
│
├── 2. Learner Enrollment
│   ├── Enrollment lifecycle
│   └── Enrollment queries
│
├── 3. Learning Progress
│   ├── Lesson completion
│   ├── Completed lessons
│   └── Course progress
│
├── 4. Learner Learning Experience
│   ├── My Courses
│   ├── My Course Detail
│   ├── My Lesson Detail
│   └── Lesson Item Media
│
├── 5. Assessment
│   └── Domain concept exists
│       Application flow not found
│
└── 6. Certification
    └── Domain concept exists
        Application flow not found
```

**Note:** This is a **business capability grouping of the current implementation**, not a bounded-context decomposition or Target Architecture.

---

# 13. Course Context vs. Learning Context

This is the most important finding of the Application Analysis.

If only the current package structure is considered:

```text
application/learning/service/
```

Learning can appear to be a large module containing many Course-related services.

However, when the actual application behavior is examined:

```text
Course Management
        +
Enrollment
        +
Progress
        +
Learner Learning Experience
```

the scope is clearly broader than Course Management.

The current implementation can therefore be represented as:

```text
                 Learning Context
                       │
          ┌────────────┼────────────┐
          │            │            │
       Course      Enrollment    Progress
          │            │            │
          └────────────┼────────────┘
                       │
              Learner Experience

          Assessment / Certification
                 (domain only)
```

This supports the following conclusion:

> **The current Learning implementation is not simply a Course Context. Course is the dominant capability and aggregate in the current implementation, but the Application layer already exposes additional capabilities related to learner participation, progress, and learning experience.**

---

# 14. Architectural Implication

The Domain, Application, and Infrastructure analyses reveal a relatively consistent pattern:

```text
Domain
    ↓
Course
Enrollment
Progress
LessonCompletion
Assessment
Certificate
UserProgress

Application
    ↓
Course Management
Enrollment
Progress
Learner Experience
Assessment / Certification not implemented

Infrastructure
    ↓
Course Content
Enrollment
Lesson Completion
```

This indicates that the current architecture is **in the process of evolving from a Course-centric implementation toward a broader Learning Context**.

Course remains the most complete capability:

```text
Course
    ↓
Domain
    ↓
Application
    ↓
Persistence
    ↓
Web
```

while several other capabilities currently exist only partially:

```text
Domain
    ↓
partial / not implemented
```

---

# 15. Confirmed Findings and Undecided Areas

## Confirmed from the Current Code

* Learning contains 31 Input Ports / Use Cases and 31 corresponding Application Services.
* Course Management is the largest and most complete group of use cases.
* Enrollment has its own application lifecycle and persistence boundary.
* Progress is currently managed through `Enrollment.progress`.
* LessonCompletion has its own Application and persistence flow.
* Learner-facing learning experience is implemented through `MyLearning`-related use cases.
* Assessment, Certification, and UserProgress exist in the Domain model but do not currently have complete Application/Infrastructure flows.
* Course → Section → Lesson → LessonItem is the clearest aggregate and persistence structure.
* Course is currently the center of gravity of the Learning implementation.

## Not Yet Decided

This Application Analysis does **not** determine:

* whether Course should become a separate bounded context;
* whether Enrollment should become a separate bounded context;
* whether Progress should become a separate context or capability;
* whether Assessment should become an independent boundary;
* where Certification should belong;
* how learner-level Learning State should be modeled;
* the target package structure;
* target aggregate boundaries;
* target database structure;
* target API architecture.

These decisions belong to the **Architecture Design** phase rather than the Current-State Application Analysis.

---

# 16. Conclusion

The Learning Application layer reveals a significantly broader picture than the current package structure suggests.

At the implementation core, Learning currently has a clear flow:

```text
Course
   ↓
Enrollment
   ↓
Progress
   ↓
LessonCompletion
```

with learner-facing behavior built around this core.

At the same time, the Domain model has already begun to expand toward:

```text
Assessment
Certification
UserProgress
```

although these capabilities are not yet fully implemented in the Application and Infrastructure layers.

Therefore:

> **The current Learning implementation is strongly Course-centric, but it is already broader than a pure Course Context.**

Course is the **center of gravity**, not the entirety of the Learning Context.

This provides a basis for the future effort to **elevate the Learning Context**: not by removing Course, but by identifying and organizing the broader capabilities of Learning so that the architecture reflects the business scope already emerging from the codebase.

Most importantly:

> **The next architectural step should be to identify and define the boundaries within this broader Learning capability before restructuring packages or refactoring code.**

