# Category Status

## Purpose

`CategoryStatus` represents the lifecycle state of a Category.

A Category does not have an editorial workflow or publication workflow.

Its lifecycle only determines whether the Category is currently available for new content classification.

---

## States

### ACTIVE

The Category is currently active and available for use.

An active Category:

- Can be selected as the Primary Category of a new ArticleVersion.
- Can be used for content navigation.
- Can be used for filtering and discovery.
- Can be referenced by new editorial content.

---

### INACTIVE

The Category is currently inactive and unavailable for new editorial operations.

An inactive Category:

- Cannot be selected as the Primary Category for a new ArticleVersion submission.
- Remains available as historical data.
- Does not invalidate existing ArticleVersions that reference it.
- Can be reactivated by an authorized administrator.

---

## State Transitions

```text
             Deactivate
ACTIVE --------------------> INACTIVE
  ▲                              │
  │                              │
  └------------------------------┘
             Reactivate