# DeutschHub Backend

DeutschHub is the backend for a German learning and culture platform. It is a long-term portfolio project focused on practising business-oriented backend development with **Domain-Driven Design (DDD)**, **Hexagonal Architecture**, and a **modular monolith** approach.

The backend is organized around business domains rather than technical layers alone. The current codebase contains implementations for **Identity, Learning, Content, and Media**, while the boundaries and responsibilities of some domains continue to evolve as the project develops.

## Project Status

DeutschHub is an actively evolving portfolio project.

The current backend contains:

* Identity and authentication
* User and session management
* Course-based learning flows
* Learning progress and enrollment
* Content management for articles, categories, and topics
* Media management and storage abstraction
* Role-based authorization
* Domain-oriented application and persistence structure

The **Learning Context is currently being reassessed in V3**. The existing implementation originated from the course-centered Learning work developed in V1, and V3 is focused on discovering and defining what the Learning Context should represent before further structural changes are made.

## Core Domains

### Identity

The Identity implementation is responsible for users, authentication, authorization, and user sessions.

Current capabilities include:

* User registration and login
* JWT access-token authentication
* Opaque refresh tokens with server-side session persistence
* Refresh-token rotation
* Per-session logout
* Logout from all active sessions
* Current-user profile management
* Password change
* Account deactivation
* Session listing and revocation
* Administrative user management
* Role management

Relevant source:

```text
src/main/java/com/deutschhub/domain/identity/
src/main/java/com/deutschhub/application/identity/
src/main/java/com/deutschhub/infrastructure/identity/
```

### Learning

The Learning implementation originated in V1 as a **course-centered learning implementation**.

The current implementation includes:

* Courses
* Sections
* Lessons
* Lesson items
* Course publication
* Enrollment lifecycle
* Lesson completion
* Course progress
* Learner course and lesson views
* Media access from lesson items

The current domain also contains additional concepts such as:

* Quiz
* Quiz Attempt
* User Progress
* Certificate
* Questions and answers

The presence of these domain concepts does not necessarily mean that all of them currently form complete learner-facing workflows.

Relevant source:

```text
src/main/java/com/deutschhub/domain/learning/
src/main/java/com/deutschhub/application/learning/
src/main/java/com/deutschhub/infrastructure/learning/
```

The Learning Context is currently being investigated as part of **V3 — Learning Context**.

### Content

The Content implementation provides content management capabilities for articles and their taxonomy.

Current source structure includes:

```text
src/main/java/com/deutschhub/domain/content/
src/main/java/com/deutschhub/application/content/
src/main/java/com/deutschhub/infrastructure/content/
```

Current Content capabilities include:

* Article drafts
* Article editing
* Article review workflow
* Article publishing
* Article archiving
* Article ownership transfer
* Categories
* Topics
* Category and topic lifecycle management
* Published article queries

### Media

Media is implemented as its own technical/domain area and is used by other parts of the backend where media resources are required.

Relevant source:

```text
src/main/java/com/deutschhub/domain/media/
src/main/java/com/deutschhub/application/media/
src/main/java/com/deutschhub/infrastructure/media/
```

Current capabilities include:

* Media upload
* Media retrieval
* Media content access
* Media storage abstraction
* Media access policies
* Persistence for media metadata

Database migrations for media were introduced after the initial Learning schema:

```text
src/main/resources/db/migration/V2__create_media_table.sql
src/main/resources/db/migration/V3__replace_lesson_item_resource_url_with_media_id.sql
```

## Architecture

DeutschHub follows a modular monolith structure organized around business domains.

```text
src/main/java/com/deutschhub
│
├── common/
│
├── domain/
│   ├── identity/
│   ├── learning/
│   ├── content/
│   ├── media/
│   └── shared/
│
├── application/
│   ├── identity/
│   ├── learning/
│   ├── content/
│   ├── media/
│   └── shared/
│
└── infrastructure/
    ├── config/
    ├── identity/
    ├── learning/
    ├── content/
    ├── media/
    └── shared/
```

The intended dependency direction is:

```text
Infrastructure
      ↓
Application
      ↓
Domain
```

The application layer exposes use cases through input ports and depends on output ports. Infrastructure provides adapters for web, persistence, security, media storage, and other technical concerns.

A typical request flow is:

```text
HTTP Controller
      ↓
Input Port
      ↓
Application Service
      ↓
Domain Model
      ↓
Output Port
      ↓
Infrastructure Adapter
```

The exact flow varies by use case and is documented within the corresponding domain implementation.

## Repository Structure

The main backend source tree is:

```text
src/
├── main/
│   ├── java/com/deutschhub/
│   │   ├── common/
│   │   ├── domain/
│   │   ├── application/
│   │   └── infrastructure/
│   │
│   └── resources/
│       ├── db/migration/
│       └── application.yaml
│
└── test/
```

Database schema evolution is managed through Flyway migrations:

```text
src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2__create_media_table.sql
├── V3__replace_lesson_item_resource_url_with_media_id.sql
├── V4__initial_schema_content_context.sql
└── V5__add_article_optimistic_locking.sql
```

## Security Model

Authentication uses two different token responsibilities:

1. A short-lived JWT access token is used to authorize API requests.
2. A long-lived refresh token is represented as an opaque random value.
3. Only the refresh-token hash is persisted on the server.
4. Each login creates a `UserSession`.
5. Refreshing a session rotates its refresh token.
6. A session can be revoked independently.
7. All active sessions can be revoked for the current user.

The security implementation is located primarily under:

```text
src/main/java/com/deutschhub/infrastructure/identity/security/
```

Identity domain and application responsibilities are located under:

```text
src/main/java/com/deutschhub/domain/identity/
src/main/java/com/deutschhub/application/identity/
```

## API

The backend exposes REST APIs under the `/api/v1` namespace.

The main controller areas are:

```text
src/main/java/com/deutschhub/infrastructure/
├── identity/web/controller/
├── learning/web/controller/
├── content/*/web/controller/
└── media/web/controller/
```

The current API surface includes areas such as:

| Area           | Main responsibilities                                                            |
| -------------- | -------------------------------------------------------------------------------- |
| Authentication | Registration, login, refresh, logout, current-user access                        |
| Identity       | Profile, password, sessions, account management                                  |
| Admin Identity | User search, activation/deactivation, role management                            |
| Learning       | Course catalog, enrollment, learner course access, lessons, completion, progress |
| Admin Learning | Course, section, lesson, lesson-item, and enrollment management                  |
| Content        | Articles, drafts, review, publishing, categories, topics                         |
| Media          | Upload, retrieval, content access, and media administration                      |

The authoritative API definitions are the controllers and application use cases in the source tree rather than this README.

## Technology Stack

| Technology                             | Purpose                              |
| -------------------------------------- | ------------------------------------ |
| Java 21                                | Main programming language            |
| Spring Boot 3.4.2                      | Application framework                |
| Spring Web                             | REST API                             |
| Spring Security OAuth2 Resource Server | JWT authentication and authorization |
| Spring Data JPA / Hibernate            | Persistence and ORM                  |
| MySQL                                  | Relational database                  |
| Flyway                                 | Database migration                   |
| Jakarta Validation                     | Request validation                   |
| Lombok                                 | Boilerplate reduction                |
| MapStruct 1.6.3                        | DTO mapping                          |
| Maven                                  | Build and dependency management      |
| JaCoCo                                 | Test coverage reporting              |

The versions above are based on the project's `pom.xml`.

## Development Direction

DeutschHub is developed incrementally rather than treating the current implementation as a final architecture.

The project currently includes separate development contexts:

```text
V1
└── Course-centered Learning implementation

V2
└── Content Context

V3
└── Learning Context discovery and evolution
```

V3 is currently focused on the Learning Context.

The V3 process is:

```text
Discovery
    ↓
Current State Analysis
    ↓
Domain Analysis
    ↓
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

The current V3 documentation is located under:

```text
docs/V3/
```

The V3 Learning Context work intentionally follows a discovery-first approach. Existing concepts are not automatically treated as the final domain model, and architectural changes are not made without a concrete domain or implementation reason.

## Run Locally

### Prerequisites

* JDK 21
* Maven
* MySQL

### Database Configuration

Create the required MySQL database and configure the environment variables used by the application.

For example:

```powershell
$env:DBMS_CONNECTION = "jdbc:mysql://localhost:3306/deutsch-hub"
$env:DBMS_USERNAME = "your-username"
$env:DBMS_PASSWORD = "your-password"
```

The application reads the database connection from the configured environment.

### Start the Application

```powershell
mvn spring-boot:run
```

The application uses the Spring Boot configuration under:

```text
src/main/resources/application.yaml
```

## Frontend

The React frontend is maintained in the companion repository:

```text
deutsch-hub-web
```

The frontend consumes the backend REST APIs but is maintained separately from this backend repository.

## Documentation

Project documentation is organized under:

```text
docs/
```

Version-specific documentation is grouped by development context, for example:

```text
docs/V1/
docs/V2/
docs/V3/
```

V3 currently contains the documentation for the Learning Context discovery and current-state analysis.

## License

This project is currently intended for personal learning and portfolio use.
