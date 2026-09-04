# Architecture Analysis

## 1. Purpose

This document analyzes the current architecture of DeutschHub based on the existing source code, focusing on:

- the overall architectural structure;
- the organization of Business Contexts;
- the separation between Domain, Application, and Infrastructure;
- the use of Hexagonal Architecture / Ports & Adapters;
- the degree of alignment between business boundaries and package structure;
- confirmed architectural issues;
- the required direction for future refactoring.

This document describes the **Current State**. It does not define a complete Target Architecture or perform any code refactoring.

---

# 2. Current Overall Architecture

DeutschHub is currently implemented as a **Modular Monolith** within a single Spring Boot application.

The main package root is:

```text
src/main/java/com/deutschhub/
├── common/
├── domain/
├── application/
└── infrastructure/
````

The main Business Contexts currently present are:

```text
domain/
├── identity/
├── learning/
├── content/
├── media/
└── shared/
```

The corresponding contexts are also organized under the Application and Infrastructure layers:

```text
application/
├── identity/
├── learning/
├── content/
├── media/
└── shared/

infrastructure/
├── identity/
├── learning/
├── content/
├── media/
├── config/
└── shared/
```

This demonstrates that the system already has **context separation at the package level**, rather than placing all domain models and application logic into a flat structure.

### Assessment

This provides a suitable foundation for a Modular Monolith:

```text
                 DeutschHub
                     │
        ┌────────────┼────────────┐
        │            │            │
    Identity      Learning      Content
        │            │            │
        └────────────┼────────────┘
                     │
                   Media
```

However, the degree to which business boundaries are explicitly reflected in the architecture is **not consistent across all contexts**.

---

# 3. Layered Architecture

Each context is separated into three primary layers:

```text
Domain
   ↑
Application
   ↑
Infrastructure
```

## Domain

The Domain layer contains the business model and business rules.

Examples include:

```text
domain/learning/model/
domain/identity/aggregate/
domain/content/article/
```

## Application

The Application layer contains application use cases, input ports, output ports, and orchestration logic.

Examples include:

```text
application/learning/port/in/
application/learning/port/out/
application/learning/service/

application/content/article/port/in/
application/content/article/port/out/
application/content/article/service/
```

## Infrastructure

The Infrastructure layer contains concrete adapters for frameworks, persistence, and web/API concerns.

Examples include:

```text
infrastructure/learning/persistence/
infrastructure/learning/web/

infrastructure/content/article/persistence/
infrastructure/content/article/web/
```

This separation demonstrates that the system does not generally allow controllers or persistence components to directly own business logic. Instead, many flows pass through the Application layer.

---

# 4. Hexagonal Architecture / Ports & Adapters

The current codebase applies the main building blocks of **Hexagonal Architecture**.

A representative Learning flow is:

```text
Controller
    ↓
Input Port
    ↓
Application Service
    ↓
Domain Model
    ↓
Output Port
    ↓
Persistence Adapter
    ↓
JPA / Database
```

For example:

```text
MyLearningController
        ↓
CompleteLessonUseCase
        ↓
CompleteLessonService
        ↓
Enrollment / Course / LessonCompletion
        ↓
EnrollmentRepositoryPort
CourseRepositoryPort
LessonCompletionRepositoryPort
        ↓
JPA Repository Adapters
```

The relevant source files are:

```text
src/main/java/com/deutschhub/infrastructure/learning/web/controller/MyLearningController.java

src/main/java/com/deutschhub/application/learning/port/in/CompleteLessonUseCase.java

src/main/java/com/deutschhub/application/learning/service/CompleteLessonService.java

src/main/java/com/deutschhub/application/learning/port/out/

src/main/java/com/deutschhub/infrastructure/learning/persistence/adapter/
```

Content V2 follows the same pattern more explicitly.

A representative Article publishing flow is:

```text
ArticleEditorController
        ↓
PublishArticleUseCase
        ↓
PublishArticleService
        ↓
Article.publish(...)
        ↓
ArticleRepositoryPort
        ↓
JpaArticleRepositoryAdapter
        ↓
Spring Data / JPA
```

The relevant components are located at:

```text
application/content/article/port/in/PublishArticleUseCase.java

application/content/article/service/PublishArticleService.java

domain/content/article/aggregate/Article.java

application/content/article/port/out/ArticleRepositoryPort.java

infrastructure/content/article/persistence/adapter/JpaArticleRepositoryAdapter.java
```

### Assessment

**Learning and Identity should not be described as "non-Hexagonal."**

Hexagonal Architecture already exists at the context/layer level.

The main issue is the **degree of alignment between the Hexagonal structure and the underlying business boundaries**.

---

# 5. Uneven Architectural Maturity Across Contexts

One of the most important characteristics of the current architecture is that the contexts do not have the same level of architectural maturity.

A high-level comparison is:

```text
Content V2
    ↓
Business-boundary oriented
    ↓
Aggregate-oriented
    ↓
Application / Infrastructure mirror domain structure


Identity V1
    ↓
Business boundaries are relatively clear
    ↓
Structure remains primarily layer/type-oriented


Learning V1
    ↓
Multiple business capabilities exist
    ↓
Boundaries are not clearly expressed
    ↓
Course becomes the center of gravity
```

This distinction is important because the problem is not that the entire current architecture is incorrect.

Instead, **Content V2 demonstrates that the existing architecture can express business boundaries more clearly within the same codebase.**

---

# 6. Identity V1

## 6.1. Business Boundaries

Identity currently contains the main aggregates:

```text
domain/identity/aggregate/
├── User.java
└── UserSession.java
```

These represent relatively clear business responsibilities:

* `User` — user identity, profile, and lifecycle.
* `UserSession` — the user's authentication/session state.

Therefore, **Identity already has relatively clear business boundaries**.

This is an important distinction from Learning.

---

## 6.2. Package Structure

Identity is still primarily organized around technical layers and types.

The business concepts are not exposed directly in the package tree in the same way as Content V2.

For example, Content V2 exposes:

```text
content/
├── article/
├── category/
└── topic/
```

while Identity groups its aggregates under:

```text
identity/aggregate/
```

As a result, developers need to inspect the classes themselves to understand the business topology instead of being able to infer it directly from the package structure.

### Assessment

Identity **does not suffer from the same degree of missing business boundaries as Learning**.

Its main issue is:

> **The business boundaries are relatively clear, but the package, application, and infrastructure structures do not mirror those boundaries as explicitly as Content V2.**

Therefore, Identity requires architectural refactoring, but the primary purpose is **structural alignment**, rather than redesigning its domain from scratch.

---

# 7. Learning V1

Learning currently presents the clearest architectural challenges.

## 7.1. Existing Business Capabilities

The current source code reveals several business capabilities.

### Structured Learning

```text
Course
 └── Section
      └── Lesson
           └── LessonItem
```

These models are located under:

```text
domain/learning/model/
```

`Course` directly manages:

* Sections;
* Lessons;
* Lesson Items;
* metadata;
* publication state;
* estimated hours.

Therefore, `Course` is currently a strong business center in the implementation.

---

### Learner Participation

Learner participation is represented by:

```text
Enrollment
```

`Enrollment` contains:

```text
userId
courseId
Progress
status
```

and has a lifecycle including:

```text
ENROLLED
IN_PROGRESS
COMPLETED
DROPPED
EXPIRED
```

---

### Learning Progress

Learning progress is currently represented by:

```text
Progress
LessonCompletion
UserProgress
```

These concepts have different meanings:

* `Progress` is a course-scoped value object representing progress.
* `LessonCompletion` represents completion evidence.
* `UserProgress` also contains course/enrollment-scoped progress state.

In particular, `UserProgress` introduces ambiguity because its name suggests learner-level state, while its fields:

```text
userId
courseId
enrollmentId
```

indicate that its current state is course-scoped.

---

### Assessment

The domain model already contains:

```text
Quiz
 └── Question
      └── AnswerQuestion

QuizAttempt
 └── UserAnswer
```

This demonstrates that Assessment already exists as a business capability in the domain model.

However, a complete corresponding vertical slice has not been found in the Application and Infrastructure layers for `Quiz` and `QuizAttempt`.

---

### Certification

`Certificate` also exists as an aggregate:

```text
domain/learning/model/aggregate/Certificate.java
```

However, a complete corresponding application and persistence flow has not been found.

---

# 8. Core Architectural Problem in Learning

The problem is not simply:

> "Learning is not packaged like Content."

The deeper issue is that **the business boundaries are not clearly expressed by the architecture**.

The Domain layer is currently organized primarily around:

```text
model/
├── aggregate/
├── entity/
├── valueobject/
└── enum/
```

The Application layer is primarily organized around:

```text
service/
port/in/
port/out/
dto/
```

The Infrastructure layer is primarily organized around:

```text
persistence/
web/
```

This structure is technically valid from a layering perspective, but it hides the business topology.

A developer has to reconstruct relationships such as:

```text
Course
Enrollment
Progress
Assessment
Certification
```

by reading multiple classes and use cases.

### Consequence

`Course` becomes the **center of gravity** of the Learning context.

This does not mean that `Course` is incorrect or that it should not be an important aggregate.

On the contrary, `Course` is currently one of the most complete and clearly implemented aggregates.

The problem is:

> **The Learning context is broader than Course, but the current architecture does not clearly express the full scope of those capabilities.**

---

# 9. Content V2 as the Current Reference

Content V2 provides the clearest example of business-boundary-oriented architecture in the current codebase.

The Domain layer is organized as:

```text
domain/content/
├── article/
├── category/
└── topic/
```

Within Article:

```text
article/
├── aggregate/
├── entity/
├── valueobject/
├── enums/
└── service/
```

The Application layer follows the same business structure:

```text
application/content/article/
├── dto/
├── port/
├── service/
└── validator/
```

The Infrastructure layer also follows it:

```text
infrastructure/content/article/
├── persistence/
└── web/
```

The important point is not simply that the package tree is cleaner.

`Article` is a real aggregate with business behavior such as:

```text
createDraft
updateDraft
submitReview
withdrawReview
requestChanges
publish
createNewDraft
archive
transferOwnership
```

`Article` also owns:

```text
ArticleVersion
ReviewCycle
```

Therefore, the package structure reflects a business boundary that actually exists in the domain model.

This is why Content V2 is easier to discover and extend.

---

# 10. Cross-Context Dependencies

The current source code contains several dependencies between contexts.

Confirmed examples include:

```text
application/content/
        ↓
domain.identity
```

primarily for authorization-related concerns.

Learning has dependencies on Media in some application services:

```text
application/learning/service/AddLessonItemService.java
application/learning/service/GetMyLessonItemMediaService.java
        ↓
application.media
domain.media
```

Media also has dependencies on Content in Infrastructure:

```text
infrastructure/media/persistence/adapter/JpaMediaLookupAdapter.java
infrastructure/media/persistence/adapter/JpaPublicMediaAccessAdapter.java
        ↓
application.content
```

Security/current-actor infrastructure also depends on Identity.

### Assessment

These dependencies **do not automatically represent architectural violations**.

Some dependencies represent actual business requirements. For example, Learning requires Media for lesson item media.

There is currently insufficient evidence to conclude that these dependencies should be removed.

One positive observation is that the Domain layers of the main contexts remain relatively separated from one another; most observed cross-context coupling occurs in Application or Infrastructure.

---

# 11. Confirmed Architectural Issues

Based on the current source code, the following architectural issues can be identified.

## 11.1. Business Boundaries and Package Boundaries Are Not Consistently Aligned

Content V2 mirrors its business boundaries relatively well.

Identity and Learning do not expose their business boundaries as clearly through their package structures.

---

## 11.2. Identity Requires Structural Alignment

Identity already has relatively clear business boundaries, but its package organization remains primarily oriented around technical layers and types.

Therefore, Identity requires architectural refactoring to better reflect its existing business boundaries.

---

## 11.3. Learning Requires Deeper Boundary Alignment

Learning requires more than package restructuring.

Before restructuring its packages, the relationships between:

```text
Course
Enrollment
Progress
LessonCompletion
Assessment
Certification
Learner-level state
```

must be clearly established.

Only after these boundaries are understood can the architecture appropriately mirror them.

---

## 11.4. The Domain Model Is Ahead of the Application/Infrastructure Implementation in Some Capabilities

Learning contains:

```text
Quiz
QuizAttempt
Certificate
UserProgress
```

but complete corresponding application and persistence implementations have not been found.

This indicates that the current domain model contains capabilities that are not yet represented as complete application/infrastructure vertical slices.

---

## 11.5. Some Naming and Model Inconsistencies Need Further Attention

For example, in `Quiz.createDraft(...)`, a parameter named `courseId` is used to initialize a field named `lessonId`.

This is a domain/model naming inconsistency that should be reviewed during future refactoring.

Similarly, `UserProgress` has a name suggesting learner-level state, while its current state is course/enrollment-scoped.

---

# 12. Architectural Refactoring Direction

Based on the confirmed findings, **Identity and Learning should undergo architectural refactoring**.

However, the refactoring should not begin immediately.

The appropriate sequence is:

```text
Current Code
     ↓
Domain Discovery
     ↓
Domain Boundaries
     ↓
Architecture Analysis
     ↓
Architecture Design
     ↓
Refactoring
     ↓
Implementation Validation
```

The V3 documentation serves as the **refactoring baseline**.

During the documentation phase, if new evidence changes our understanding of a boundary or responsibility, the architectural understanding should be updated before modifying the code.

### Guiding Principle

The goal is not:

```text
Content V2
    ↓
copy package structure
    ↓
Learning / Identity
```

Instead:

```text
Business Boundaries
        ↓
Architectural Boundaries
        ↓
Package Structure
```

Package structure should be the **result of architectural understanding**, not the starting point.

---

# 13. Scope of Architecture Refactoring

This architecture analysis does **not** determine:

* the final target package structure;
* the final bounded-context decomposition;
* new aggregate boundaries;
* database schema changes;
* API redesign;
* migration strategy;
* adoption of event-driven architecture;
* decomposition of the Modular Monolith into Microservices.

These decisions should only be made after sufficient evidence has been collected and should be addressed in the appropriate future phase.

The immediate goal is to make the architecture **better aligned with the business model already identified in the codebase**, rather than changing the architecture simply because another structure appears cleaner.

---

# 14. Conclusion

DeutschHub already has a relatively clear architectural foundation:

* Modular Monolith;
* Business Context separation;
* Domain / Application / Infrastructure layering;
* Hexagonal Architecture / Ports & Adapters;
* aggregates containing actual business behavior.

However, the degree of architectural alignment between contexts is inconsistent.

**Content V2** currently provides the clearest representation of the relationship:

```text
Business Boundary
        ↓
Aggregate
        ↓
Application Boundary
        ↓
Infrastructure Boundary
```

**Identity V1** already has relatively clear business boundaries, but its package structure does not fully mirror those boundaries.

**Learning V1** contains multiple business capabilities and domain concepts, but their boundaries are not yet clearly expressed by the architecture. This makes Course the center of gravity and makes the broader business topology of Learning harder to discover.

Therefore:

> **Identity and Learning require architectural refactoring, but the refactoring should happen after the Domain and Architecture analyses are sufficiently complete. The structure of Content V2 should be used as a reference, not copied directly into the other contexts.**

The objective of the next phase is not to "rewrite the architecture", but to **align the architecture with the business boundaries confirmed through the codebase**.


