# Article Aggregate

## Purpose

`Article` is the Aggregate Root of the Explore Germany Content Context.

It represents the editorial lifecycle of an article rather than the article content itself.

The Article aggregate coordinates article versions, protects editorial and publication workflows, and determines which version is visible to readers.

---

## Responsibilities

The Article aggregate is responsible for:

- Managing the editorial workflow.
- Managing the publication workflow.
- Managing editorial ownership.
- Coordinating `ArticleVersion`.
- Protecting business invariants.
- Determining the active Draft Version.
- Determining the current Published Version.
- Managing the public identity (`Slug`) of the article.

---

## Not Responsible For

The Article aggregate is NOT responsible for:

- Storing editorial content.
- Managing media files.
- Managing categories.
- Managing topics.
- Managing sources.
- Rendering Markdown.
- Searching articles.
- Managing user enrollment.

---

## Aggregate Boundary

### Owns

- ArticleVersion

### References

- Category
- Topic
- Media

---

## Aggregate Invariants

The Article aggregate guarantees that:

- Only one active Draft Version exists.
- Only one Published Version exists.
- Published content is immutable.
- Readers always access the current Published Version.
- Every ArticleVersion belongs to exactly one Article.
- A published slug remains stable unless explicitly changed by an administrative action.

---

## Aggregate Structure

### Identity

- id

### Public Identity

- slug

### Workflow

- publicationStatus
- editorialStatus

### Version Management

- draftVersionId
- publishedVersionId

### Ownership

- ownerId

### Lifecycle Metadata

- createdAt
- createdBy
- publishedAt
- publishedBy
- archivedAt
- archivedBy

---

## Notes

The `Article` aggregate stores workflow and lifecycle information only.

The Article aggregate never stores editorial content directly.

All editable content belongs to `ArticleVersion`.

The `Slug` belongs to the `Article` because it represents the public identity of the article rather than the content of a specific version.

---
# ArticleVersion

## Purpose

`ArticleVersion` represents a complete snapshot of an article's editorial content at a specific point in time.

Each version belongs to exactly one `Article`.

A new `ArticleVersion` is created only when a new editorial cycle starts.

---

## Responsibilities

The ArticleVersion entity is responsible for:

- Storing editorial content.
- Storing taxonomy information.
- Storing media references.
- Storing content sources.
- Recording version metadata.
- Recording review history.

---

## Aggregate Parent

Owned by:

- Article

---

## Child Entities

- ReviewCycle

---

## Aggregate References

- Category
- Topic
- Media

---

## Aggregate Invariants

The ArticleVersion guarantees that:

- A version always belongs to exactly one Article.
- Published versions are immutable.
- A version has exactly one primary category.
- All selected topics must belong to the selected primary category.
- All referenced sources must exist.
- All referenced media must exist.

---

## Aggregate Structure

### Identity

- id
- versionNumber

### Editorial Content

- title
- summary
- body

### Taxonomy

- primaryCategoryId
- topicIds

### Media

- coverMediaId

### Sources

- sources

### Metadata

- createdAt
- createdBy
- lastModifiedAt
- lastModifiedBy

---

## Notes

`ArticleVersion` stores only editorial content.

It does not manage publication, ownership, or editorial workflow.

Review history belongs to each individual version through `ReviewCycle`.