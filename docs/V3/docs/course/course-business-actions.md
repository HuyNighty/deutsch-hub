# Course Business Actions

## 1. Purpose

This document describes the business actions currently implemented for the Course domain.

It focuses on how application-level actions invoke behavior on the `Course` Aggregate and how state changes are persisted.

The document does not redefine business rules. Business rules are documented separately in `course-business-rules.md`.

The analysis is based on the current implementation under:

```text
src/main/java/com/deutschhub/application/learning/service/
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
src/main/java/com/deutschhub/domain/learning/model/entity/Section.java
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
src/main/java/com/deutschhub/domain/learning/model/entity/LessonItem.java
````

---

## 2. Action Model

Course state-changing actions generally follow this interaction:

```text
Client
  ↓
Application Service
  ↓
Load Course Aggregate
  ↓
Invoke Course Domain Behavior
  ↓
Persist Course Aggregate
  ↓
Map Result / Response
```

The Application Service is responsible for orchestration.

The `Course` Aggregate is responsible for enforcing Course business rules and controlling mutations to its internal entities.

The current implementation does not use independent repositories for `Section`, `Lesson`, or `LessonItem` when mutating Course structure.

This supports the current Aggregate boundary:

```text
Course Aggregate
└── Course
    └── Section
        └── Lesson
            └── LessonItem
```

---

# 3. Course Lifecycle Actions

## 3.1 Create Course

### Application Service

```text
CreateCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/CreateCourseService.java
```

### Flow

```text
CreateCourseService
    ↓
Construct CEFRLevel
    ↓
Construct Money
    ↓
Course.create(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The service delegates Course creation to:

```text
Course.create(...)
```

Location:

```text
src/main/java/com/deutschhub/domain/learning/model/aggregate/Course.java
```

The Course Aggregate establishes its initial state and validates the required creation data.

### Application Responsibilities

The application layer:

* receives the creation command;
* converts primitive input into domain types such as `CEFRLevel` and `Money`;
* invokes `Course.create(...)`;
* persists the resulting Aggregate;
* maps the result to the application response.

### Domain Responsibilities

The domain layer:

* creates the Course Aggregate;
* validates Course creation invariants;
* establishes the initial Course state.

### State Change

A new Course is created in an unpublished and non-deleted state.

### Status

**Confirmed and correctly separated between Application and Domain.**

---

## 3.2 Update Course

### Application Service

```text
UpdateCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/UpdateCourseService.java
```

### Flow

```text
UpdateCourseService
    ↓
Load Course
    ↓
Prepare updated values
    ↓
Course.updateMetadata(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate handles the actual metadata mutation through:

```text
Course.updateMetadata(...)
```

### Application Responsibilities

The application layer:

* loads the Course;
* maps optional update fields;
* prepares domain values;
* invokes the Aggregate behavior;
* persists the Aggregate.

### Domain Responsibilities

The domain layer determines whether the Course metadata can be changed and validates the resulting state.

### State Change

Course metadata may be changed when the domain rules permit the operation.

### Status

**Confirmed and correctly separated.**

---

## 3.3 Delete Course

### Application Service

```text
DeleteCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/DeleteCourseService.java
```

### Flow

```text
DeleteCourseService
    ↓
Load Course
    ↓
Course.softDelete(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

Deletion is represented by the Course domain behavior:

```text
Course.softDelete(...)
```

The current model uses a soft-delete representation rather than physically removing the Course from the domain model.

### State Change

The Course enters its deleted state through `deletedAt`.

### Status

**Confirmed.**

Whether deleting a Course should also soft-delete all child Sections, Lessons, and LessonItems is not fully established as a domain requirement by the current implementation.

This remains an open business decision.

---

## 3.4 Publish Course

### Application Service

```text
PublishCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/PublishCourseService.java
```

### Flow

```text
PublishCourseService
    ↓
Load Course
    ↓
Course.publish(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Aggregate controls publication through:

```text
Course.publish(...)
```

The method enforces the publication rules defined by the Course domain.

The current implementation requires, among other conditions:

* the Course must not be deleted;
* the Course must not already be published;
* at least one active Section must exist;
* at least one active Lesson must exist in an active Section;
* the actor must be authorized.

### State Change

```text
published: false → true
```

The Course modification timestamp is also updated.

### Status

**Confirmed and correctly modeled as Aggregate behavior.**

---

## 3.5 Unpublish Course

### Application Service

```text
UnpublishCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/UnpublishCourseService.java
```

### Flow

```text
UnpublishCourseService
    ↓
Load Course
    ↓
Course.unpublish(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Aggregate controls unpublication through:

```text
Course.unpublish(...)
```

The domain determines whether the Course can transition from published to unpublished.

### State Change

```text
published: true → false
```

### Status

**Confirmed and correctly modeled as Aggregate behavior.**

---

# 4. Course Structure Actions

## 4.1 Add Section

### Application Service

```text
AddSectionToCourseService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/AddSectionToCourseService.java
```

### Flow

```text
AddSectionToCourseService
    ↓
Create Section
    ↓
Course.addSection(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate owns the addition of the Section:

```text
Course.addSection(...)
```

The Application Service does not directly manipulate the Course's internal collection.

### State Change

A new `Section` becomes part of the Course Aggregate.

The Course's derived estimated duration is recalculated by the current domain implementation.

### Status

**Confirmed.**

---

## 4.2 Update Section

### Application Service

```text
UpdateSectionService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/UpdateSectionService.java
```

### Flow

```text
UpdateSectionService
    ↓
Load Course
    ↓
Course.updateSection(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate locates and updates the requested Section.

### State Change

The selected Section's mutable properties are updated through the Aggregate boundary.

### Status

**Confirmed.**

---

## 4.3 Delete Section

### Application Service

```text
DeleteSectionService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/DeleteSectionService.java
```

### Flow

```text
DeleteSectionService
    ↓
Load Course
    ↓
Course.deleteSection(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate controls Section deletion.

The current domain model represents deletion using the Section's soft-delete state.

### State Change

The selected Section becomes deleted.

The Course's derived estimated duration is recalculated by the current implementation.

### Status

**Confirmed.**

---

# 5. Lesson Actions

## 5.1 Add Lesson

### Application Service

```text
AddLessonToSectionService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/AddLessonToSectionService.java
```

### Flow

```text
AddLessonToSectionService
    ↓
Create Lesson
    ↓
Course.addLessonToSection(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate controls the addition of the Lesson to the selected Section.

The Application Service does not directly modify the Section's internal collection.

### State Change

A new `Lesson` becomes part of the Course Aggregate through its owning Section.

### Status

**Confirmed.**

---

## 5.2 Update Lesson

### Application Service

```text
UpdateLessonService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/UpdateLessonService.java
```

### Flow

```text
UpdateLessonService
    ↓
Load Course
    ↓
Course.updateLesson(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate locates the requested Lesson and applies the update.

### State Change

The selected Lesson's mutable properties are changed.

The Course's derived estimated duration is recalculated by the current implementation.

### Implementation Finding

The current `Lesson.changeOrderIndex(int orderIndex)` method in:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
```

validates the incoming value but does not currently assign the new value to the entity state.

Therefore, an order-index update may not actually change the Lesson state.

This is an implementation issue, not a newly established business rule.

### Status

**Business Action confirmed. Implementation issue requires later correction.**

---

## 5.3 Delete Lesson

### Application Service

```text
DeleteLessonService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/DeleteLessonService.java
```

### Flow

```text
DeleteLessonService
    ↓
Load Course
    ↓
Course.deleteLesson(...)
    ↓
CourseRepositoryPort.save(...)
```

### Domain Behavior

The Course Aggregate controls Lesson deletion.

The current implementation uses soft deletion.

### State Change

The selected Lesson becomes deleted.

The Course's estimated duration is recalculated.

### Status

**Confirmed.**

---

# 6. LessonItem Actions

## 6.1 Add LessonItem

### Application Service

```text
AddLessonItemService
```

Location:

```text
src/main/java/com/deutschhub/application/learning/service/AddLessonItemService.java
```

### Flow

```text
AddLessonItemService
    ↓
Determine LessonItem type
    ↓
Validate external Media when required
    ↓
Create LessonItem
    ↓
Course.addLessonItemToLesson(...)
    ↓
CourseRepositoryPort.save(...)
    ↓
Build response/navigation data
```

### Domain Behavior

The Course Aggregate owns the LessonItem through:

```text
Course.addLessonItemToLesson(...)
```

The `LessonItem` itself enforces type-specific construction rules.

For example:

```text
TEXT  → content required
MEDIA → mediaId required
QUIZ  → quizId required
```

### External Reference Validation

For `MEDIA` LessonItems, the Application Service validates the referenced Media through:

```text
MediaRepositoryPort
```

This keeps the Media Aggregate outside the Course Aggregate.

The Course therefore stores an external reference rather than owning Media.

### Quiz Reference

The current LessonItem model stores `quizId` for `QUIZ` items.

The currently inspected `AddLessonItemService` does not establish a corresponding Quiz repository validation step.

Therefore, this document does not treat Quiz existence validation as a confirmed Course action requirement.

### Additional Application Logic

`AddLessonItemService` currently contains navigation resolution logic for determining previous and next Lessons.

This is query/presentation-oriented logic rather than Course Aggregate mutation.

It is therefore documented separately from the Course business behavior.

### State Change

A new LessonItem becomes part of the Course Aggregate through its Lesson.

### Status

**Aggregate boundary: confirmed.**

**External Media validation: confirmed.**

**Navigation resolution: application/query concern; review later.**

---

# 7. Query Responsibilities

Not every Course-related Application Service represents a business action that changes Aggregate state.

The current application layer also contains read-oriented services such as:

```text
GetCourseDetailService
GetPublishedCourseDetailService
GetViewerCourseDetailService
GetCourseSectionService
GetSectionLessonsService
GetCoursesService
GetPublishedCoursesService
```

Locations:

```text
src/main/java/com/deutschhub/application/learning/service/
```

These services should be treated as query/read responsibilities rather than Course Aggregate business actions.

The distinction is:

```text
Command
    ↓
Change Course state
    ↓
Course Aggregate behavior
```

versus:

```text
Query
    ↓
Read Course data
    ↓
Return projection/response
```

This document focuses on the first category.

---

# 8. Action-to-Domain Mapping

| Business Action  | Application Service         | Domain Behavior                  | Aggregate State Change |
| ---------------- | --------------------------- | -------------------------------- | ---------------------- |
| Create Course    | `CreateCourseService`       | `Course.create()`                | New Course             |
| Update Course    | `UpdateCourseService`       | `Course.updateMetadata()`        | Course metadata        |
| Delete Course    | `DeleteCourseService`       | `Course.softDelete()`            | Course deleted state   |
| Publish Course   | `PublishCourseService`      | `Course.publish()`               | Published state        |
| Unpublish Course | `UnpublishCourseService`    | `Course.unpublish()`             | Unpublished state      |
| Add Section      | `AddSectionToCourseService` | `Course.addSection()`            | New Section            |
| Update Section   | `UpdateSectionService`      | `Course.updateSection()`         | Section state          |
| Delete Section   | `DeleteSectionService`      | `Course.deleteSection()`         | Section deleted state  |
| Add Lesson       | `AddLessonToSectionService` | `Course.addLessonToSection()`    | New Lesson             |
| Update Lesson    | `UpdateLessonService`       | `Course.updateLesson()`          | Lesson state           |
| Delete Lesson    | `DeleteLessonService`       | `Course.deleteLesson()`          | Lesson deleted state   |
| Add LessonItem   | `AddLessonItemService`      | `Course.addLessonItemToLesson()` | New LessonItem         |

---

# 9. Application-to-Domain Responsibility Boundary

The current Course actions demonstrate the following responsibility split.

## Application Layer

The Application Layer is responsible for:

* receiving use-case commands;
* loading Aggregates;
* constructing domain objects when appropriate;
* converting input data into domain types;
* validating external references when required by the use case;
* invoking domain behavior;
* persisting the Aggregate;
* preparing application responses.

## Domain Layer

The Domain Layer is responsible for:

* Course invariants;
* Course lifecycle transitions;
* Course structure mutations;
* ownership of Section, Lesson, and LessonItem;
* validation of domain state;
* derived Course state such as estimated hours;
* authorization rules currently implemented as part of Course mutation behavior.

This separation is consistent across the current Course command services.

---

# 10. Implementation Findings

The current implementation does not indicate a need for a broad Application Service refactor.

The following findings are concrete and should be treated separately from the business action definitions.

## 10.1 Lesson Order Index Update

File:

```text
src/main/java/com/deutschhub/domain/learning/model/entity/Lesson.java
```

`changeOrderIndex(int orderIndex)` validates the input but does not currently assign the new value.

This can cause a valid order-index update to have no effect.

**Classification:** implementation bug.

---

## 10.2 Navigation Resolution

File:

```text
src/main/java/com/deutschhub/application/learning/service/AddLessonItemService.java
```

`resolveNavigation()` calculates previous and next Lesson information while preparing the response.

This logic is not required to establish the Course Aggregate invariant.

**Classification:** application/query responsibility; possible future cleanup.

No relocation is required based on the current evidence.

---

## 10.3 Course Deletion Cascade

The current Course deletion action soft-deletes the Course.

The current domain implementation does not establish a confirmed rule that deleting a Course must also soft-delete every child Section, Lesson, and LessonItem.

**Classification:** open business decision.

---

# 11. Open Decisions

The following points should remain open until their business meaning is explicitly decided.

### 11.1 Course Deletion Cascade

Should deleting a Course automatically delete:

```text
Course
 ├── Section
 │    └── Lesson
 │         └── LessonItem
```

or should child deletion be handled independently?

The current implementation does not provide enough evidence to establish the intended business rule.

---

### 11.2 LessonItem Duration Contribution

`Course.recalculateEstimatedHours()` currently derives Course estimated hours from Lesson estimated minutes.

The current implementation does not directly add LessonItem estimated minutes to the Course total.

Whether this is intentional or a missing business rule remains open.

---

### 11.3 LessonItem Type Mutation

`LessonItem` currently determines its type during creation, and no operation for changing the type was identified.

It is not yet established whether:

```text
TEXT → MEDIA
MEDIA → QUIZ
```

should be supported as a business action or intentionally prohibited.

---

### 11.4 Authorization Responsibility

Course mutation methods currently receive actor information and enforce authorization inside the domain model.

Whether authorization should remain part of the Aggregate behavior or eventually be expressed through a dedicated domain policy is an architectural refinement question.

No change is required without a concrete problem.

---

# 12. Summary

The current Course business actions are centered on the `Course` Aggregate.

The dominant command pattern is:

```text
Application Service
    ↓
Load Course
    ↓
Invoke Course behavior
    ↓
Save Course
```

The Course Aggregate owns its internal structure:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

The current Application Services generally respect this boundary and do not directly mutate internal Aggregate collections.

The main Course business actions are:

```text
Lifecycle
├── Create
├── Update
├── Delete
├── Publish
└── Unpublish

Structure
├── Add Section
├── Update Section
├── Delete Section
├── Add Lesson
├── Update Lesson
├── Delete Lesson
└── Add LessonItem
```

The current implementation therefore provides a clear basis for the Course domain model.

No broad Application Layer redesign is justified by the current evidence.

Concrete implementation issues and open business decisions should be handled separately from the established Course action model.
