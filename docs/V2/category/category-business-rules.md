# Category Business Rules

## Purpose

`Category` is an Aggregate Root of the Content Context.

A Category represents a high-level taxonomy used to organize and classify Articles.

Categories support:

- Content organization.
- Navigation.
- Filtering.
- Content discovery.

A Category is independent from the Article Aggregate.

Articles reference Categories through their identity.

---

# Aggregate Boundary

## Aggregate Root

- Category

## Owned Entities

- None.

## Value Objects

- CategoryName

## Enums

- CategoryStatus

The Category Aggregate does not own Articles, ArticleVersions, or Topics.

---

# Category Structure

A Category contains:

- Identity
- Name
- Status

```text
Category
├── id
├── name
└── status
````

---

# Business Rules

## 1. Category Identity

* Every Category must have a unique identity.
* Category identity is generated when the Category is created.
* Category identity does not change during the Category lifecycle.

---

## 2. Category Name

* Category name is required.
* Category name cannot be blank.
* Leading and trailing whitespace is removed.
* Category name has a maximum length of 100 characters.
* Category names are compared case-insensitively for uniqueness.
* Two Categories cannot have the same logical name.

For example:

```text
Grammar
grammar
GRAMMAR
```

represent the same logical Category name.

Category name uniqueness is not guaranteed by the Aggregate itself.

The Application layer performs collision checking.

The Database provides a unique constraint as the final consistency boundary.

---

## 3. Category Status

A Category has exactly one of the following statuses:

```text
ACTIVE
INACTIVE
```

A newly created Category always starts as:

```text
ACTIVE
```

---

## 4. Active Category

An `ACTIVE` Category:

* Can be selected as the Primary Category of a new ArticleVersion.
* Can be used for content navigation.
* Can be used for filtering.
* Can be used for content discovery.

---

## 5. Inactive Category

An `INACTIVE` Category:

* Cannot be selected for new editorial operations.
* Must not be used when submitting a new ArticleVersion for review.
* Remains available as historical data.
* Does not invalidate existing ArticleVersions that reference it.

Deactivating a Category does not delete the Category.

---

# Lifecycle

A Category has a simple lifecycle:

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

There is no editorial workflow for Categories.

Categories do not have:

* DRAFT
* IN_REVIEW
* CHANGES_REQUESTED
* PUBLISHED
* ARCHIVED
* RESTORED

---

# Actions

## Create Category

### Actor

* Admin

### Preconditions

* Actor has permission to manage content taxonomy.
* Category name is valid.
* No Category with the same logical name exists.

### Business Rules

* Generate a unique Category ID.
* Create the Category with the provided name.
* Set `CategoryStatus = ACTIVE`.

### State Transition

```text
null → ACTIVE
```

### Result

A new active Category is created and becomes available for content classification.

---

## Rename Category

### Actor

* Admin

### Preconditions

* Category exists.
* New Category name is valid.
* No other Category has the same logical name.

### Business Rules

* Replace the current Category name.
* Preserve the Category identity.
* Do not modify ArticleVersion references.

### State Transition

```text
No state change.
```

### Result

The Category has a new name while existing ArticleVersions continue referencing the same Category.

---

## Deactivate Category

### Actor

* Admin

### Preconditions

* Category exists.
* Category status is `ACTIVE`.

### Business Rules

* Change the Category status to `INACTIVE`.
* Do not delete the Category.
* Do not modify existing ArticleVersions.
* Preserve all historical references.

### State Transition

```text
ACTIVE → INACTIVE
```

### Result

The Category can no longer be used for new editorial operations.

---

## Reactivate Category

### Actor

* Admin

### Preconditions

* Category exists.
* Category status is `INACTIVE`.

### Business Rules

* Change the Category status to `ACTIVE`.

### State Transition

```text
INACTIVE → ACTIVE
```

### Result

The Category becomes available for new editorial operations again.

---

# Relationship With Article

An ArticleVersion references a Category using:

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

The Category Aggregate does not query Articles or ArticleVersions.

---

# Relationship With Topic

A Topic is a separate Aggregate Root.

A Topic references exactly one Category through:

```text
categoryId
```

The Category Aggregate does not own Topics.

The relationship is:

```text
Category Aggregate
       ↑
       │ categoryId
       │
Topic Aggregate
```

The current version does not support hierarchical Categories.

Category is a flat taxonomy.

More detailed classification is handled by Topic.

---

# Historical Data

Category status changes must not invalidate historical ArticleVersions.

For example:

```text
Category:
Grammar
ACTIVE
```

may be referenced by:

```text
ArticleVersion V1
primaryCategoryId = Grammar
```

If the Category becomes:

```text
Grammar
INACTIVE
```

then:

```text
ArticleVersion V1
primaryCategoryId = Grammar
```

remains unchanged.

The Category becomes unavailable only for new editorial operations.

---

# Responsibilities of the Application Layer

The Application layer is responsible for coordinating Category with other Aggregates.

For example, when an Article is submitted for review, the Application layer must verify that:

* The referenced Category exists.
* The Category is `ACTIVE`.
* The selected Topics exist.
* The selected Topics are `ACTIVE`.
* The selected Topics belong to the selected Category.

The Category Aggregate itself does not perform these cross-Aggregate validations.

---

# Persistence Rules

The Database must preserve Category identity and prevent duplicate logical names.

The persistence model should enforce:

* Primary key on Category ID.
* Unique constraint for normalized/case-insensitive Category name.
* Valid Category status.

Category must not be hard-deleted when it has historical references.

Deactivation is used instead.

---

# Design Constraints

The current version intentionally keeps Category simple.

Category does not support:

* Hierarchical relationships.
* Parent Categories.
* Child Categories.
* Category ownership.
* Editorial review.
* Publication workflow.
* Versioning.
* Hard deletion.

These capabilities may be introduced in future versions only if required by business needs.
