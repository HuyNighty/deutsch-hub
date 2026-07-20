# DeutschHub Backend

DeutschHub is the backend for a German learning and culture platform. It is a long-term portfolio project built to practise business-oriented backend design with Domain-Driven Design (DDD), Hexagonal Architecture, and a modular monolith approach.

The current release delivers a complete learning MVP: users can authenticate, browse published courses, enroll, study lessons, track progress, and manage their account sessions. Administrators can manage users, courses, sections, lessons, and lesson content items.

## Highlights

- Java 21 and Spring Boot 3.4
- Domain-driven, ports-and-adapters architecture
- JWT access tokens with role-based authorization
- Opaque refresh tokens stored as hashes in server-side user sessions
- Refresh-token rotation, per-session logout, and logout from all devices
- Course authoring with sections, lessons, and ordered lesson items
- Public course catalog and authenticated learning flow
- Enrollment lifecycle, lesson completion, and course progress tracking
- Consistent API envelopes and validation/business error responses

## Current Scope

### Identity and Access

- User registration and login by username or email
- Short-lived JWT access tokens using HS512
- Opaque refresh tokens with hashed persistence in `user_sessions`
- Refresh-token rotation and session revocation
- Get and update the current profile
- Change password, deactivate an account, and revoke all personal sessions
- View active and revoked sessions; revoke one specific session
- Admin user search, detail lookup, activation/deactivation, and role management

### Learning

- Admin course CRUD, publishing, unpublishing, and soft deletion
- Course sections and lessons with ordered content
- Lesson items: `TEXT`, `VIDEO`, `PDF`, `DOCUMENT`, `AUDIO`, and `QUIZ`
- Public published-course catalog, search, and course detail
- Viewer course detail that includes enrollment information when a user is authenticated
- Course enrollment and drop-course flow
- My learning list, enrolled-course detail, lesson detail, and previous/next lesson navigation
- Lesson completion, completed-lesson lookup, and progress calculation
- Admin enrollment lookup and expiration

## Architecture

DeutschHub is organized by business domain. The domain layer contains business rules and does not depend on Spring, JPA, or web concerns. The application layer exposes use cases through input ports and depends on output ports. Infrastructure provides the web, persistence, and security adapters.

```text
src/main/java/com/deutschhub
|
|-- common/                         Shared exceptions, API utilities, and domain contracts
|
|-- domain/                         Framework-independent business model
|   |-- identity/                   User, UserSession, roles, and value objects
|   |-- learning/                   Course, Enrollment, Lesson, LessonItem, Quiz model
|   `-- content/                    Early Content Context domain model
|
|-- application/                    Use cases, DTOs, and ports
|   |-- identity/
|   `-- learning/
|
`-- infrastructure/                 Technical adapters and framework configuration
    |-- config/                     Spring Security and exception configuration
    |-- identity/                   JWT, password, web, and JPA adapters
    `-- learning/                   Course web and JPA adapters
```

### Request Flow

```text
HTTP Controller -> Input Port -> Application Service -> Domain Model -> Output Port -> Adapter
```

For example, an enrollment request enters `MyLearningController`, invokes `EnrollCourseUseCase`, lets the `Enrollment` aggregate enforce its business rules, then persists through `EnrollmentRepositoryPort` and its JPA adapter.

## Security Model

DeutschHub separates the two token responsibilities:

1. The access token is a short-lived JWT used to authorize API requests.
2. The refresh token is an opaque random value. Only its hash is stored in the database.
3. Each login creates a `UserSession`. Refreshing rotates the refresh token for that session.
4. Logout revokes a session, while logout-all revokes every active session for the current user.

This design allows server-side control over long-lived sessions without storing raw refresh tokens in the database.

## API Overview

All endpoints use the base path configured by the application:

```text
http://localhost:8080/deutsch-hub/api/v1
```

| Area | Example endpoints |
| --- | --- |
| Authentication | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /auth/me` |
| Account | `PATCH /users/me/profile`, `PUT /users/me/password`, `GET /users/me/sessions`, `POST /users/me/logout-all` |
| Public courses | `GET /courses`, `GET /courses/{courseId}`, `GET /courses/{courseId}/viewer` |
| Learning | `POST /courses/{courseId}/enroll`, `GET /me/courses`, `GET /me/courses/{courseId}`, `POST /me/courses/{courseId}/lessons/{lessonId}/complete` |
| Admin users | `GET /admin/users`, `PATCH /admin/users/{userId}/activate`, `PUT /admin/users/{userId}/roles` |
| Admin courses | `POST /admin/courses`, section/lesson management, publish/unpublish, lesson-item management |

Successful responses use a common `ApiResponse<T>` envelope. Validation errors and business errors return a stable `code`, `message`, and optional field-level `errors` collection.

## Technology Stack

| Technology | Purpose |
| --- | --- |
| Java 21 | Main programming language |
| Spring Boot 3.4 | Web application framework |
| Spring Security OAuth2 Resource Server | JWT authentication and authorization |
| Spring Data JPA and Hibernate | Persistence abstraction and ORM |
| MySQL | Relational database |
| Lombok | Boilerplate reduction |
| MapStruct | DTO mapping support |
| Jakarta Validation | Request validation |
| Maven | Build and dependency management |

The React frontend lives in the companion `deutsch-hub-web` project and currently consumes the Identity and Learning APIs.

## Run Locally

### Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8+

### Database Configuration

Create a MySQL database named `deutsch-hub`, then configure these environment variables before starting the application:

```powershell
$env:DBMS_CONNECTION = "jdbc:mysql://localhost:3306/deutsch-hub"
$env:DBMS_USERNAME = "****"
$env:DBMS_PASSWORD = "****"
```

### Start the Application

```powershell
mvn spring-boot:run
```

The API starts at `http://localhost:8080/deutsch-hub` by default.

## Development Status

Completed:

- Hexagonal Architecture and DDD project structure
- Identity, authentication, authorization, and session management
- Admin user management
- Learning MVP: course, section, lesson, lesson-item, enrollment, and progress flows
- React frontend integration for the core learning journey

In progress:

- Refactoring repeated Learning response and navigation logic
- Manual regression coverage for the V1 learning flow
- Frontend loading, empty, validation, and error states

Planned for V2:

- Media storage abstraction and upload management
- Content Context for articles, pages, categories, topics, and publishing
- Quiz authoring, attempts, scoring, and assessment rules
- Automated unit, integration, and end-to-end tests
- Database migrations, Docker, CI/CD, and production hardening

## License

This project is currently intended for personal learning and portfolio use.
