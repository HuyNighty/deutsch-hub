# Shared Kernel

## Purpose

The Shared Kernel contains domain concepts that are shared across multiple Bounded Contexts.

These concepts represent common business language and should remain stable.

The Shared Kernel is intentionally kept small to minimize coupling between contexts.

---

# Current Shared Value Objects

## UserId

Represents the identity of a user across the entire system.

Examples:

- Article.ownerId
- Article.createdBy
- Article.publishedBy
- Article.archivedBy

- Course.createdBy
- Lesson.createdBy

- Enrollment.userId

- Media.uploaderId

The meaning of the field may differ, but the identity always represents the same business concept:

"A User from the Identity Context."

---

## Why UserId instead of OwnerId?

Ownership is a business role.

User identity is a business identity.

For example:

ownerId

means

"The owner is this User."

rather than

"This is an Owner object."

Using UserId allows every Bounded Context to speak the same language.

---

# Design Principles

Shared Value Objects should:

- Represent stable business concepts.
- Have no lifecycle.
- Be immutable.
- Be referenced by multiple Bounded Contexts.

---

# What does NOT belong here?

Business-specific concepts.

Examples:

- ArticleTitle
- Slug
- VersionNumber
- CourseTitle
- LessonDuration

These belong to their own Domain.

---

# Future Shared Value Objects

As the system evolves, additional shared identifiers may be introduced.

Examples:

- MediaId
- CourseId
- ArticleId
- CategoryId
- TopicId

Only introduce a shared Value Object when multiple Bounded Contexts genuinely need it.

Avoid creating shared abstractions prematurely.