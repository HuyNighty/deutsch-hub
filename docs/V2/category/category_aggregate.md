# Category Aggregate

## Purpose

`Category` is an Aggregate Root of the Content Context.

It represents a high-level taxonomy used to organize and classify Articles.

A Category provides a stable classification for content and supports content navigation, filtering, and discovery.

Category is independent from the Article Aggregate.

Articles reference a Category through its identity rather than owning the Category itself.

---

## Responsibilities

The Category aggregate is responsible for:

* Managing the identity of a category.
* Managing the category name.
* Managing the category lifecycle.
* Activating a category.
* Deactivating a category.
* Protecting category invariants.

---

## Not Responsible For

The Category aggregate is NOT responsible for:

* Managing Articles.
* Managing ArticleVersions.
* Managing Topics.
* Managing article editorial workflows.
* Managing article publication workflows.
* Searching Articles.
* Rendering content.
* Managing media files.

---

## Aggregate Boundary

### Owns

* Category

### References

* None.

Category does not contain Articles or Topics inside its aggregate boundary.

ArticleVersion references Category through `categoryId`.

Topic references Category through `categoryId`.

---

## Aggregate Invariants

The Category aggregate guarantees that:

* A Category always has a valid identity.
* A Category always has a valid name.
* A Category name cannot be blank.
* A newly created Category starts as `ACTIVE`.
* A Category can only have a valid lifecycle status.
* An `INACTIVE` Category cannot be used for new editorial operations.
* Deactivating a Category does not remove historical references to it.
* Reactivating a Category makes it available for new editorial operations again.

Category name uniqueness is not guaranteed by the Aggregate itself.

Uniqueness is enforced through:

* Application-level collision checking.
* Database-level unique constraint.

Category name comparison is case-insensitive.

For example:

```text
Grammar
grammar
GRAMMAR
```

represent the same logical category name and cannot coexist.

---

## Aggregate Structure

### Identity

* id

### Definition

* name

### Lifecycle

* status

---

# Category Status

## ACTIVE

The Category is currently available for use.

An active Category:

* Can be selected for new ArticleVersions.
* Can be used for content navigation.
* Can be used for filtering and discovery.

---

## INACTIVE

The Category is no longer available for new editorial operations.

An inactive Category:

* Cannot be selected for a new ArticleVersion submission.
* Remains available as historical data.
* Does not cause existing ArticleVersions to become invalid.
* Can be reactivated by an authorized administrator.

---

# Lifecycle

```text
ACTIVE
   │
   │ Deactivate
   ▼
INACTIVE
   │
   │ Reactivate
   ▼
ACTIVE
```

Category does not have an editorial review or publication workflow.



## Relationship With Article

An ArticleVersion stores:

```text
primaryCategoryId
```

The Article Aggregate does not own the Category.

The relationship is:

```text
Category Aggregate
       ↑
       │ categoryId
       │
ArticleVersion
       ↑
       │
Article Aggregate
```

The Application layer is responsible for verifying that the referenced Category exists and is `ACTIVE` when an Article is submitted for review.

The Category Aggregate itself does not query Articles or ArticleVersions.
