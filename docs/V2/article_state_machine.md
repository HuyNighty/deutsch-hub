# Article State Machine

## Overview

An `Article` has two independent business lifecycles.

1. **Editorial Workflow**
    - Describes how editors and administrators collaborate before publication.

2. **Publication Lifecycle**
    - Describes whether the article is visible to public readers.

These two lifecycles are independent.

For example:

```
Publication Status = PUBLISHED

Editorial Status = DRAFT
```

means:

- Readers continue reading the published version.
- Editors are working on the next draft version.

---

# Editorial Workflow

## States

### DRAFT

The editor is creating or editing the current draft.

Allowed actions:

- Update Draft
- Submit Review

---

### IN_REVIEW

The draft has been submitted for review.

Allowed actions:

- Publish
- Request Changes
- Withdraw Review

---

### CHANGES_REQUESTED

The reviewer has requested modifications.

Allowed actions:

- Update Draft
- Submit Review

---

## Editorial State Machine

```text
                 Submit Review
DRAFT ---------------------------------> IN_REVIEW
  ▲                                         │
  │                                         │
  │ Withdraw Review                         │ Publish
  │                                         ▼
CHANGES_REQUESTED <------ Request Changes (Draft closed)
        │
        │ Update Draft
        └────────────────────────────────────┘
```

---

# Publication Lifecycle

## States

### UNPUBLISHED

The article has never been published.

Public users cannot access it.

Allowed actions:

- Publish

---

### PUBLISHED

The article is publicly available.

Allowed actions:

- Archive
- Create New Draft

Creating a new draft **does not** change the publication status.

Readers continue reading the current published version.

---

### ARCHIVED

The article is no longer publicly available.

Allowed actions:

- Restore

Public requests should return **HTTP 410 Gone**.

---

## Publication State Machine

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
      │
      │ Restore
      ▼
PUBLISHED
```

---

# Relationship Between Both Lifecycles

Example:

```
Publication Status = PUBLISHED

Published Version = V1

Draft Version = null
```

Editor creates a new draft.

```
Publication Status = PUBLISHED

Published Version = V1

Draft Version = V2
```

Notice that:

- Publication Status does not change.
- Readers still access V1.
- Editors work on V2.

---

# Business Invariants

## Editorial

- At most one active Draft Version.
- A Draft must exist before review.
- Only Draft can be submitted for review.

---

## Publication

- At most one Published Version.
- Readers always read the Published Version.
- Archiving never deletes article history.
- Restoring republishes the last Published Version.

---

# Notes

This document defines business behavior only.

It does not describe:

- REST APIs
- Database tables
- Controllers
- Repositories
- Frontend implementation