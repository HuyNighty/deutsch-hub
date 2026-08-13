
# Topic Business Rules

## Purpose

`Topic` is an Aggregate Root of the Content Context.

A Topic represents a specific subject within a Category and provides a more detailed level of content classification for ArticleVersions.

Topics are used to:

- Provide detailed content classification.
- Improve content filtering.
- Improve content navigation.
- Improve content discovery.

A Topic is independent from the Article Aggregate and the Category Aggregate.

---

# Aggregate Boundary

## Aggregate Root

- Topic

## Owned Entities

- None.

## Value Objects

- TopicName

## Enums

- TopicStatus

The Topic Aggregate does not own:

- Articles.
- ArticleVersions.
- Categories.
- Media.

These are independent Aggregates.

---

# Aggregate Structure

A Topic contains:

```text
Topic
├── id
├── name
├── categoryId
└── status
````

## Identity

* `id`

The Topic identity uniquely identifies the Topic throughout its lifecycle.

The identity never changes.

---

## Definition

* `name`

The Topic name is the human-readable name used to identify the Topic.

The name is represented by the `TopicName` Value Object.

---

## Category Association

* `categoryId`

Every Topic belongs to exactly one Category.

The Category relationship is represented through Category identity.

The Topic does not contain a Category Aggregate object.

The Topic stores:

```text
categoryId
```

instead of:

```text
Category category
```

---

## Lifecycle

* `status`

The Topic lifecycle is represented by `TopicStatus`.

The current version supports:

```text
ACTIVE
INACTIVE
```

---

# Topic Invariants

The Topic Aggregate guarantees that:

* A Topic always has a valid identity.
* A Topic always has a valid name.
* A Topic name cannot be null.
* A Topic name cannot be blank.
* Leading and trailing whitespace is removed from the Topic name.
* A Topic name cannot exceed the defined maximum length.
* Every Topic must belong to exactly one Category.
* `categoryId` cannot be null.
* A newly created Topic always starts as `ACTIVE`.
* A Topic always has a valid lifecycle status.
* An inactive Topic cannot be used for new editorial operations.
* Deactivating a Topic does not delete the Topic.
* Deactivating a Topic does not modify historical ArticleVersions.
* Reactivating a Topic makes it available for new editorial operations again.
* A Topic cannot be transferred from one Category to another after creation.

---

# Topic Name Rules

## Required

A Topic must always have a valid name.

The following values are invalid:

```text
null
""
"   "
```

---

## Normalization

Leading and trailing whitespace is removed.

For example:

```text
"  Natural Expressions  "
```

becomes:

```text
"Natural Expressions"
```

---

## Maximum Length

The Topic name has a maximum length of 100 characters.

The exact limit is enforced by the `TopicName` Value Object.

---

## Case-Insensitive Uniqueness

Topic names are unique within their Category regardless of letter casing.

For example, the following Topics cannot coexist within the same Category:

```text
Natural Expressions
natural expressions
NATURAL EXPRESSIONS
```

However, the same logical Topic name may exist under different Categories if the business model allows it.

For example:

```text
Communication
└── Expressions

Vocabulary
└── Expressions
```

may coexist because they belong to different Categories.

Topic uniqueness is therefore scoped by:

```text
categoryId + logical topic name
```

The Topic Aggregate itself cannot guarantee uniqueness across all Topics.

Uniqueness is enforced through multiple layers:

```text
Domain
    ↓
Valid TopicName

Application
    ↓
Collision checking within Category

Database
    ↓
Unique constraint(category_id, normalized_name)
```

---

# Topic Status

A Topic has exactly one lifecycle status:

```text
ACTIVE
INACTIVE
```

---

## ACTIVE

An `ACTIVE` Topic is currently available for use.

An active Topic:

* Can be selected for new ArticleVersions.
* Can be used for content filtering.
* Can be used for content navigation.
* Can be used for content discovery.

---

## INACTIVE

An `INACTIVE` Topic is currently unavailable for new editorial operations.

An inactive Topic:

* Cannot be selected for new ArticleVersions.
* Cannot be used when submitting a new ArticleVersion for review.
* Remains available as historical data.
* Does not invalidate existing ArticleVersions that reference it.
* Can be reactivated by an authorized administrator.

---

# Topic Lifecycle

The Topic lifecycle is intentionally simple.

```text
             Deactivate
ACTIVE --------------------> INACTIVE
  ▲                              │
  │                              │
  └------------------------------┘
             Reactivate
```

The Topic does not have an editorial workflow.

It does not require:

* Draft.
* Review.
* Approval.
* Publication.
* Archive.
* Restore.

---

# Category Association

## One Category Per Topic

Every Topic belongs to exactly one Category.

For example:

```text
Category
└── Communication

    Topic
    ├── Natural Expressions
    ├── Small Talk
    └── Everyday Conversation
```

A Topic cannot belong to multiple Categories simultaneously.

---

## Immutable Category Association

Once a Topic is created:

```text
Topic.categoryId
```

cannot be changed.

For example:

```text
Natural Expressions
        ↓
Communication
```

cannot later become:

```text
Natural Expressions
        ↓
Vocabulary
```

The Topic Aggregate does not support category reassignment.

---

## Why Category Association Is Immutable

Topics may already be referenced by historical ArticleVersions.

If a Topic were moved to another Category, an existing ArticleVersion could become logically inconsistent.

For example:

```text
ArticleVersion
├── primaryCategoryId = Communication
└── topicIds = [Natural Expressions]
```

Initially:

```text
Natural Expressions.categoryId = Communication
```

If the Topic were moved to:

```text
Natural Expressions.categoryId = Vocabulary
```

the historical ArticleVersion would contain:

```text
Category = Communication
Topic = Natural Expressions
Topic Category = Vocabulary
```

This creates a taxonomy inconsistency.

Therefore, Topic Category association is immutable.

If a Topic is incorrectly classified:

```text
Deactivate existing Topic
        ↓
Create a new Topic
        ↓
Associate the new Topic with the correct Category
```

---

# Actions

# Create Topic

## Actor

* Admin

## Preconditions

* The actor has permission to manage content taxonomy.
* The Topic name is valid.
* `categoryId` is not null.
* The referenced Category exists.
* The referenced Category is `ACTIVE`.
* No Topic with the same logical name exists within the Category.

## Business Rules

* Generate a unique Topic identity.
* Create the Topic with the provided name.
* Associate the Topic with exactly one Category.
* Set `TopicStatus = ACTIVE`.

## State Transition

```text
null
 ↓
ACTIVE
```

## Result

* A new active Topic is created.
* The Topic becomes available for ArticleVersion classification.

---

# Rename Topic

## Actor

* Admin

## Preconditions

* The Topic exists.
* The new Topic name is valid.
* No other Topic with the same logical name exists within the same Category.

## Business Rules

* Replace the current Topic name.
* Preserve the Topic identity.
* Preserve the Category association.
* Do not modify ArticleVersion references.

## State Transition

```text
No state change.
```

## Result

The Topic receives a new human-readable name while maintaining the same identity and Category association.

For example:

```text
Topic ID = T001
Category = Communication
Name = Natural Expressions
```

becomes:

```text
Topic ID = T001
Category = Communication
Name = Natural German Expressions
```

---

# Deactivate Topic

## Actor

* Admin

## Preconditions

* The Topic exists.
* The Topic status is `ACTIVE`.

## Business Rules

* Change the Topic status to `INACTIVE`.
* Do not delete the Topic.
* Do not modify existing ArticleVersions.
* Preserve historical references.

## State Transition

```text
ACTIVE
   ↓
INACTIVE
```

## Result

The Topic can no longer be selected for new editorial operations.

Existing ArticleVersions that reference the Topic remain unchanged.

---

# Reactivate Topic

## Actor

* Admin

## Preconditions

* The Topic exists.
* The Topic status is `INACTIVE`.

## Business Rules

* Change the Topic status to `ACTIVE`.

## State Transition

```text
INACTIVE
   ↓
ACTIVE
```

## Result

The Topic becomes available for new editorial operations again.

---

# ArticleVersion Classification Rules

An ArticleVersion contains:

```text
primaryCategoryId
topicIds[]
```

The following relationship must hold:

```text
Every selected Topic
        ↓
belongs to
        ↓
the selected Primary Category
```

For example:

```text
ArticleVersion
├── primaryCategoryId = Communication
│
└── topicIds
    ├── Natural Expressions
    └── Small Talk
```

Both Topics must belong to:

```text
Communication
```

---

# Cross-Aggregate Validation

The Topic Aggregate cannot validate whether it is correctly associated with an ArticleVersion.

The Article Aggregate cannot query Topic or Category Aggregates directly.

The Application layer is responsible for coordinating these validations.

When an Article is submitted for review, the Application layer must verify:

* The Primary Category exists.
* The Primary Category is `ACTIVE`.
* Every selected Topic exists.
* Every selected Topic is `ACTIVE`.
* Every selected Topic belongs to the selected Primary Category.

Only after these validations succeed should the Application layer invoke:

```text
Article.submitReview(...)
```

---

# Historical Data

Topic status changes must not invalidate historical ArticleVersions.

For example:

```text
Topic:
Natural Expressions
status = ACTIVE
```

may be referenced by:

```text
ArticleVersion
topicIds = [Natural Expressions]
```

If the Topic becomes:

```text
Natural Expressions
status = INACTIVE
```

the existing ArticleVersion remains unchanged.

The inactive status only prevents the Topic from being selected for new editorial operations.

---

# Relationship With Category

Topic and Category are separate Aggregate Roots.

The relationship is represented through identity:

```text
Category Aggregate
       ↑
       │
       │ categoryId
       │
Topic Aggregate
```

Topic does not contain:

```text
Category category;
```

and Category does not contain:

```text
List<Topic> topics;
```

---

# Relationship With Article

ArticleVersion references Topics through:

```text
topicIds[]
```

The Article Aggregate does not own Topics.

The relationship is:

```text
Topic Aggregate
       ↑
       │
       │ topicIds
       │
ArticleVersion
       ↑
       │
       │ belongs to
       │
Article Aggregate
```

---

# Application Layer Responsibilities

The Application layer is responsible for:

* Checking whether the Category exists.
* Checking whether the Category is active when creating a Topic.
* Checking Topic name collision within the Category.
* Checking whether selected Topics exist.
* Checking whether selected Topics are active.
* Checking whether selected Topics belong to the ArticleVersion's Primary Category.

The Topic Aggregate itself does not query:

* Category.
* Article.
* ArticleVersion.
* Other Topics.

---

# Persistence Rules

The persistence model must preserve Topic identity, Category association, and historical references.

The database must enforce:

* Primary key on Topic ID.
* Foreign key from Topic to Category.
* Unique constraint on `(category_id, normalized_name)`.
* Valid Topic status.
* Referential integrity for ArticleVersion Topic references.

Topic should not be hard-deleted when historical ArticleVersions may reference it.

Deactivation is used instead.

---

# Deletion Policy

Topic does not support hard deletion in the current version.

Instead of:

```text
DELETE Topic
```

the system uses:

```text
ACTIVE
   ↓
INACTIVE
```

This preserves:

* Historical ArticleVersions.
* Historical classification.
* Referential integrity.
* Content history.

---

# Hierarchy

Topic does not have a hierarchy in the current version.

A Topic belongs directly to exactly one Category.

The taxonomy structure is:

```text
Category
   ↓
Topic
```

There is no:

```text
Topic
   ↓
Subtopic
```

relationship.

---

# Scope

The Topic Aggregate is intentionally lightweight.

It is responsible for:

* Topic identity.
* Topic name.
* Category association.
* Topic lifecycle.
* Topic invariants.

It is not responsible for:

* Article management.
* ArticleVersion management.
* Category management.
* Media management.
* Editorial workflows.
* Publication workflows.
* Search implementation.
* Content rendering.

---

# Unsupported Features

The current version intentionally does not support:

* Topic hierarchy.
* Multiple Category associations.
* Topic reassignment between Categories.
* Topic ownership.
* Topic versioning.
* Topic review.
* Topic publication.
* Topic archiving.
* Topic restoration.
* Hard deletion.

These capabilities may be introduced in future versions only when supported by explicit business requirements.

---

# Design Principles

## Aggregate Independence

Topic is an independent Aggregate Root.

It must not contain other Aggregates.

---

## Single Category Association

Every Topic belongs to exactly one Category.

The Category association is immutable.

---

## Identity-Based References

Other Aggregates reference Topics through their identities.

For example:

```text
ArticleVersion.topicIds
```

does not contain Topic Aggregate objects.

---

## Historical Integrity

Deactivating a Topic must never destroy or invalidate historical content.

---

## Controlled Taxonomy

Topics are managed taxonomy elements rather than free-form tags.

Editors select existing Topics instead of creating arbitrary labels during Article editing.

---

## Simple Lifecycle

Topic intentionally has a simple lifecycle:

```text
ACTIVE
INACTIVE
```

The complexity of the Article editorial workflow must not be transferred to taxonomy management.

---

## Future Evolution

The Topic model should remain simple until new business requirements justify additional concepts or lifecycle states.

Future capabilities must be introduced as explicit domain requirements rather than speculative features.
