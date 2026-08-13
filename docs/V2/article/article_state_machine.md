# Article State Machine

## Overview

An `Article` has two independent business lifecycles:

1. **Editorial Workflow**
    - Describes how editors and administrators collaborate around the current Draft Version.

2. **Publication Lifecycle**
    - Describes whether the article is currently visible to public readers.

These two lifecycles are independent.

For example:

```text
PublicationStatus = PUBLISHED
EditorialStatus   = DRAFT
```

means:

- Readers continue accessing the current Published Version.
- Editors are working on a new Draft Version.
- The Draft Version does not affect public content until it is published.

---

# Editorial Workflow

## States

### DRAFT

The current Draft Version is editable.

Allowed actions:

- Update Draft
- Submit Review
- Archive, if `PublicationStatus = PUBLISHED`

---

### IN_REVIEW

The current Draft Version has been submitted for administrative review.

The Draft Version is locked for editing.

Allowed actions:

- Publish
- Request Changes
- Withdraw Review
- Archive, if `PublicationStatus = PUBLISHED`

---

### CHANGES_REQUESTED

The current review has been rejected with feedback.

The Draft Version is editable again.

Allowed actions:

- Update Draft
- Submit Review
- Archive, if `PublicationStatus = PUBLISHED`

---

### IDLE

There is no active editorial cycle.

Normally this means the current Published Version is stable and there is no active Draft Version.

Allowed actions:

- Create New Draft
- Archive, if `PublicationStatus = PUBLISHED`

---

# Editorial State Machine

```text
                         Submit Review
                    ┌──────────────────────┐
                    │                      ▼
                  DRAFT ──────────────> IN_REVIEW
                    ▲                       │
                    │                       │ Request Changes
                    │                       ▼
                    │               CHANGES_REQUESTED
                    │                       │
                    │                       │ Update Draft
                    │                       │
                    │                       ▼
                    │                      DRAFT
                    │
                    │
                    └──────────── Publish ──┐
                                             │
                                             ▼
                                            IDLE
```

More precisely:

```text
DRAFT
  │
  ├── Update Draft ────────────────> DRAFT
  │
  └── Submit Review ───────────────> IN_REVIEW

IN_REVIEW
  │
  ├── Withdraw Review ─────────────> DRAFT
  │
  ├── Request Changes ─────────────> CHANGES_REQUESTED
  │
  └── Publish ─────────────────────> IDLE

CHANGES_REQUESTED
  │
  ├── Update Draft ────────────────> CHANGES_REQUESTED
  │
  └── Submit Review ───────────────> IN_REVIEW
```

**Important:**

`Update Draft` does **not** automatically change:

```text
CHANGES_REQUESTED → DRAFT
```

The article remains `CHANGES_REQUESTED` until the editor submits the revised Draft for review.

---

# Publication Lifecycle

## States

### UNPUBLISHED

The Article has never been published.

Public users cannot access it.

Typical editorial states:

- `DRAFT`
- `IN_REVIEW`
- `CHANGES_REQUESTED`

Allowed action:

- Publish when `EditorialStatus = IN_REVIEW`

---

### PUBLISHED

The Article is currently visible to public readers.

The Article may or may not have an active Draft Version.

Examples:

```text
PUBLISHED / IDLE
PUBLISHED / DRAFT
PUBLISHED / CHANGES_REQUESTED
PUBLISHED / IN_REVIEW
```

Readers always access the current Published Version.

Allowed actions:

- Create New Draft
- Update Draft, when editorial state allows it
- Submit Review, when editorial state allows it
- Request Changes
- Withdraw Review
- Publish
- Archive

---

### ARCHIVED

The Article is removed from the public catalog.

Public requests should return:

```text
HTTP 410 Gone
```

The Article and all historical versions are preserved.

**Archive does not delete the current Published Version.**

An archived Article may still continue through its editorial workflow.

Examples:

```text
ARCHIVED / IDLE
ARCHIVED / DRAFT
ARCHIVED / CHANGES_REQUESTED
ARCHIVED / IN_REVIEW
```

However, an Article in `ARCHIVED / IN_REVIEW` is immediately converted to:

```text
ARCHIVED / DRAFT
```

because the active Review Cycle is withdrawn during Archive.

---

# Publication State Machine

There is intentionally **no Restore transition in V2**.

```text
UNPUBLISHED
      │
      │ Publish
      ▼
PUBLISHED
      │
      │ Archive
      ▼
ARCHIVED
```

There is no:

```text
ARCHIVED
    │
    │ Restore
    ▼
PUBLISHED
```

An archived Article must go through the editorial workflow again before becoming public.

---

# Archive Behavior

Archive changes the **Publication Status** immediately:

```text
PUBLISHED → ARCHIVED
```

It does **not** blindly change the Editorial Status.

### When EditorialStatus = IDLE

```text
PUBLISHED / IDLE
       │
       │ Archive
       ▼
ARCHIVED / IDLE
```

---

### When EditorialStatus = DRAFT

```text
PUBLISHED / DRAFT
       │
       │ Archive
       ▼
ARCHIVED / DRAFT
```

The Draft Version is preserved and remains editable.

---

### When EditorialStatus = CHANGES_REQUESTED

```text
PUBLISHED / CHANGES_REQUESTED
       │
       │ Archive
       ▼
ARCHIVED / CHANGES_REQUESTED
```

The Draft Version remains editable.

---

### When EditorialStatus = IN_REVIEW

```text
PUBLISHED / IN_REVIEW
       │
       │ Archive
       ▼
Withdraw Review
       │
       ▼
ARCHIVED / DRAFT
```

The current Review Cycle is completed with:

```text
ReviewResult = WITHDRAWN
```

The Draft Version remains available for further editing.

---

# Archived Article Can Become Public Again

An archived Article **cannot be restored directly**.

There is no:

```text
ARCHIVED → PUBLISHED
```

through a `Restore` action.

Instead, the Article must complete a new editorial cycle.

For an archived Article with a Published Version:

```text
ARCHIVED / IDLE
       │
       │ Create New Draft
       ▼
ARCHIVED / DRAFT
       │
       │ Update Draft
       ▼
ARCHIVED / DRAFT
       │
       │ Submit Review
       ▼
ARCHIVED / IN_REVIEW
       │
       │ Publish
       ▼
PUBLISHED / IDLE
```

The same applies when the archived Article already has a Draft Version:

```text
ARCHIVED / DRAFT
       │
       │ Update Draft
       ▼
ARCHIVED / DRAFT
       │
       │ Submit Review
       ▼
ARCHIVED / IN_REVIEW
       │
       │ Publish
       ▼
PUBLISHED / IDLE
```

This means **publishing is the only way to return an archived Article to the public catalog**.

---

# Version Relationship

The two lifecycles must be understood independently from Article Versions.

Example:

```text
PublicationStatus = PUBLISHED
EditorialStatus   = IDLE

Published Version = V1
Draft Version     = null
```

After `Create New Draft`:

```text
PublicationStatus = PUBLISHED
EditorialStatus   = DRAFT

Published Version = V1
Draft Version     = V2
```

Readers continue accessing:

```text
V1
```

while editors work on:

```text
V2
```

After `Publish`:

```text
PublicationStatus = PUBLISHED
EditorialStatus   = IDLE

Published Version = V2
Draft Version     = null
```

---

# Archived Version Relationship

Archiving does not delete versions.

For example:

```text
PublicationStatus = ARCHIVED
EditorialStatus   = DRAFT

Published Version = V1
Draft Version     = V2
```

Both versions remain part of the Article history.

The public simply cannot access the Article while:

```text
PublicationStatus = ARCHIVED
```

---

# Business Invariants

## Editorial

- At most one active Draft Version exists.
- A Draft Version must exist before submitting a review.
- Only the current Draft Version can be modified.
- A Draft Version cannot be modified while `IN_REVIEW`.
- Only one Review Cycle can be `PENDING` for the current Draft Version.
- `CHANGES_REQUESTED` keeps the current Draft Version editable.
- Updating a Draft does not automatically change `CHANGES_REQUESTED` to `DRAFT`.
- Submitting the revised Draft changes `CHANGES_REQUESTED` to `IN_REVIEW`.

---

## Publication

- At most one current Published Version exists.
- The current Published Version is immutable.
- Readers always access the current Published Version when `PublicationStatus = PUBLISHED`.
- `ARCHIVED` removes the Article from the public catalog.
- Archiving never deletes Article history.
- Archiving never deletes the Published Version.
- Archiving an `IN_REVIEW` Article withdraws the active Review Cycle.
- Archiving an `IN_REVIEW` Article changes its Editorial Status to `DRAFT`.
- Archiving a `DRAFT` Article preserves `DRAFT`.
- Archiving a `CHANGES_REQUESTED` Article preserves `CHANGES_REQUESTED`.
- Archiving an `IDLE` Article preserves `IDLE`.
- V2 does not support direct Restore.
- An archived Article must complete the editorial review workflow before being published again.

---

# Valid State Combinations

The two statuses are independent, but only specific combinations are meaningful.

| Publication | Editorial | Meaning |
|---|---|---|
| `UNPUBLISHED` | `DRAFT` | First draft is being edited |
| `UNPUBLISHED` | `IN_REVIEW` | First article is under review |
| `UNPUBLISHED` | `CHANGES_REQUESTED` | First article requires changes |
| `PUBLISHED` | `IDLE` | Stable published article |
| `PUBLISHED` | `DRAFT` | Published article has an active draft |
| `PUBLISHED` | `IN_REVIEW` | Published article has a draft under review |
| `PUBLISHED` | `CHANGES_REQUESTED` | Published article's draft requires changes |
| `ARCHIVED` | `IDLE` | Archived article with no active draft |
| `ARCHIVED` | `DRAFT` | Archived article has an editable draft |
| `ARCHIVED` | `CHANGES_REQUESTED` | Archived article's draft requires changes |
| `ARCHIVED` | `IN_REVIEW` | **Transient only**; Archive immediately withdraws the review and changes it to `DRAFT` |

---

# Action Matrix

| Publication | Editorial | Update Draft | Submit Review | Withdraw | Request Changes | Publish | Create New Draft | Archive |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| `UNPUBLISHED` | `DRAFT` | Yes | Yes | No | No | No | No | No |
| `UNPUBLISHED` | `IN_REVIEW` | No | No | Yes | Yes | Yes | No | No |
| `UNPUBLISHED` | `CHANGES_REQUESTED` | Yes | Yes | No | No | No | No | No |
| `PUBLISHED` | `IDLE` | No | No | No | No | No | Yes | Yes |
| `PUBLISHED` | `DRAFT` | Yes | Yes | No | No | No | No | Yes |
| `PUBLISHED` | `IN_REVIEW` | No | No | Yes | Yes | Yes | No | Yes |
| `PUBLISHED` | `CHANGES_REQUESTED` | Yes | Yes | No | No | No | No | Yes |
| `ARCHIVED` | `IDLE` | No | No | No | No | No | Yes* | No |
| `ARCHIVED` | `DRAFT` | Yes | Yes | No | No | No | No | No |
| `ARCHIVED` | `IN_REVIEW` | No | No | Yes** | Yes | Yes | No | No |
| `ARCHIVED` | `CHANGES_REQUESTED` | Yes | Yes | No | No | No | No | No |

`*` `Create New Draft` from `ARCHIVED / IDLE` starts a new editorial cycle while keeping `PublicationStatus = ARCHIVED`.

`**` Normally this state is transient because Archive withdraws the active Review Cycle immediately.

---

# Ownership and Authorization

Ownership is represented by:

```text
Article.ownerId
```

The Article aggregate exposes:

```java
article.ensureOwnedBy(actorId);
```

Ownership verification is a domain concern.

Role-based authorization is an Application-layer concern.

The Domain does not contain special handling for:

- `ADMIN`
- `CONTENT_EDITOR`

The Application Service decides whether the current actor:

- must own the Article, or
- has administrative permission to bypass the ownership requirement.

---

# Notes

This document defines business behavior only.

It does not describe:

- REST APIs
- Database tables
- JPA mappings
- Controllers
- Repositories
- Application Services
- Frontend implementation