# Topic Status

## Purpose

`TopicStatus` represents the lifecycle state of a Topic.

A Topic does not have an editorial workflow or publication workflow.

Its lifecycle only determines whether the Topic is currently available for new content classification.

---

# States

## ACTIVE

The Topic is currently active and available for use.

An active Topic:

- Can be selected for new ArticleVersions.
- Can be used for content navigation.
- Can be used for filtering.
- Can be used for content discovery.

---

## INACTIVE

The Topic is currently inactive and unavailable for new editorial operations.

An inactive Topic:

- Cannot be selected for new ArticleVersions.
- Cannot be used when submitting a new ArticleVersion for review.
- Remains available as historical data.
- Does not invalidate existing ArticleVersions that reference it.
- Can be reactivated by an authorized administrator.

---

# State Transitions

```text
             Deactivate
ACTIVE --------------------> INACTIVE
  ▲                              │
  │                              │
  └------------------------------┘
             Reactivate
````

---

# Business Rules

## Create Topic

A newly created Topic always starts with:

```text
ACTIVE
```

---

## Deactivate Topic

Only an `ACTIVE` Topic can be deactivated.

```text
ACTIVE → INACTIVE
```

---

## Reactivate Topic

Only an `INACTIVE` Topic can be reactivated.

```text
INACTIVE → ACTIVE
```

---

# Historical Data

Changing the Topic status does not modify historical ArticleVersions.

For example:

```text
Topic:
Natural Expressions
ACTIVE
```

may be referenced by:

```text
ArticleVersion:
topicIds = [Natural Expressions]
```

If the Topic later becomes:

```text
Natural Expressions
INACTIVE
```

the existing ArticleVersion remains unchanged.

The inactive status only prevents the Topic from being used in new editorial operations.

---

# Category Association

Topic has an immutable Category association.

Once created:

```text
Topic.categoryId
```

cannot be changed.

Changing the Topic status does not change its Category association.

If a Topic is incorrectly associated with a Category, the existing Topic should be deactivated and a new Topic should be created under the correct Category.

---

# Notes

`TopicStatus` is an enum rather than a Value Object.

The current version intentionally supports only:

```text
ACTIVE
INACTIVE
```

No additional lifecycle states are required.

Topic does not support:

* DRAFT
* IN_REVIEW
* CHANGES_REQUESTED
* PUBLISHED
* ARCHIVED
* RESTORED
* DELETED
