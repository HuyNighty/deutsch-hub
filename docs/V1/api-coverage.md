# API Coverage Report (V1)

## Status

| Status | Description |
|----------|-------------|
| Used | Backend API đã được Frontend sử dụng trong V1 |
| Backend Only | Backend đã có nhưng Frontend chưa sử dụng |
| Planned V2 | Không thuộc phạm vi của V1, sẽ triển khai trong V2 |
| Deprecated | API không còn sử dụng hoặc dự kiến thay thế |

---

# Identity Context

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| POST /auth/register | Yes | Used | Register account |
| POST /auth/login | Yes | Used | User authentication |
| POST /auth/refresh | Yes | Used | Refresh access token |
| POST /auth/logout | Yes | Used | Logout current session |
| GET /auth/me | No | Planned V2 | Retrieve current authenticated user. Will be used by Account/Profile instead of checking only the access token. |

---

# User Context

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| PATCH /users/me/profile | No | Planned V2 | Update profile information |
| PUT /users/me/password | No | Planned V2 | Change password |
| POST /users/me/logout-all | No | Planned V2 | Logout all devices |
| GET /users/me/sessions | No | Planned V2 | View active sessions |
| DELETE /users/me/sessions/{sessionId} | No | Planned V2 | Revoke a session |
| PATCH /users/me/deactivate | No | Planned V2 | Deactivate current account |

> User Context will be implemented together with the Profile / Account module.

---

# Course Context

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| GET /courses | Yes | Used | Course catalog |
| GET /courses/{courseId} | Yes | Used | Public course detail |
| GET /courses/{courseId}/viewer | Yes | Used | Course preview before enrollment |
| POST /courses/{courseId}/enroll | Yes | Used | Enroll into a course |

---

# Learning Context

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| GET /me/courses | Yes | Used | My Learning |
| GET /me/courses/{courseId} | Yes | Used | Learning course detail |
| GET /me/courses/{courseId}/viewer | Yes | Used | Learning viewer with progress information |
| GET /me/courses/{courseId}/lessons/{lessonId} | Yes | Used | Lesson detail |
| POST /me/courses/{courseId}/lessons/{lessonId}/complete | Yes | Used | Complete lesson |
| GET /me/courses/{courseId}/progress | Yes | Used | Current learning progress. Currently overlaps with Viewer response but should remain for future extensions. |
| GET /me/courses/{courseId}/completed-lessons | No | Backend Only | Retrieve completed lesson ids. Can be used for synchronization and future learning features. |
| POST /me/courses/{courseId}/drop | No | Backend Only | Drop an enrolled course. Frontend has not implemented this feature yet. |

---

# Quiz Context

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| Quiz APIs | No | Planned V2 | Quiz, Question, UserAnswer, QuizAttempt and scoring workflow will be implemented in V2. |

---

# Admin Context

## User Management

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| GET /admin/users/{userId} | No | Planned V2 | User detail |
| GET /admin/users | No | Planned V2 | User list |
| GET /admin/users?keyword= | No | Planned V2 | Search users |
| PATCH /admin/users/{userId}/activate | No | Planned V2 | Activate user |
| PATCH /admin/users/{userId}/deactivate | No | Planned V2 | Deactivate user |

---

## Course Management

### Course

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| POST /admin/courses | No | Planned V2 | Create course |
| GET /admin/courses | No | Planned V2 | Course list |
| GET /admin/courses/{courseId} | No | Planned V2 | Course detail |
| PATCH /admin/courses/{courseId} | No | Planned V2 | Update course |
| DELETE /admin/courses/{courseId} | No | Planned V2 | Delete course |
| POST /admin/courses/{courseId}/publish | No | Planned V2 | Publish course |
| POST /admin/courses/{courseId}/unpublish | No | Planned V2 | Unpublish course |
| GET /admin/courses/{courseId}/enrollments | No | Planned V2 | View enrollments |

### Section

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| POST /admin/courses/{courseId}/sections | No | Planned V2 | Create section |
| GET /admin/courses/{courseId}/sections | No | Planned V2 | Section list |
| PATCH /admin/courses/{courseId}/sections/{sectionId} | No | Planned V2 | Update section |
| DELETE /admin/courses/{courseId}/sections/{sectionId} | No | Planned V2 | Delete section |

### Lesson

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| POST /admin/courses/{courseId}/sections/{sectionId}/lessons | No | Planned V2 | Create lesson |
| GET /admin/courses/{courseId}/sections/{sectionId}/lessons | No | Planned V2 | Lesson list |
| PATCH /admin/courses/{courseId}/sections/{sectionId}/lessons/{lessonId} | No | Planned V2 | Update lesson |
| DELETE /admin/courses/{courseId}/sections/{sectionId}/lessons/{lessonId} | No | Planned V2 | Delete lesson |

### Lesson Item

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| POST /admin/courses/{courseId}/sections/{sectionId}/lessons/{lessonId}/items | No | Planned V2 | Create lesson item |

### Enrollment

| API | Frontend | Status | Notes |
|-----|----------|--------|-------|
| GET /admin/enrollments/{enrollmentId} | No | Planned V2 | Enrollment detail |
| GET /admin/enrollments/{enrollmentId}/expire | No | Planned V2 | Expire enrollment |

---

# Summary

## Completed in V1

- Identity Context
- Course Context
- Learning Context (Core Flow)

## Planned for V2

- User / Profile Context
- Quiz Context
- Admin Context
- Account Management
- Course Management Dashboard

---

# Technical Debt

## Backend

- Introduce Flyway/Liquibase migration.
- Replace Hibernate `ddl-auto=update` with `validate`.
- Move all sensitive configurations to environment variables.
- Improve API documentation.

## Frontend

- Replace token-based authentication check with `GET /auth/me`.
- Introduce AuthProvider.
- Implement global error handling.
- Add toast notification.
- Add loading and skeleton components.
- Add empty states.
- Improve responsive layout.
- Build Design System.