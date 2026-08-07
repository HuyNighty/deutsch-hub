# Article Aggregate

## Purpose

`Article` is the Aggregate Root of the Explore Germany Content Context.

It represents the editorial lifecycle of an article rather than its content.

The Article aggregate coordinates article versions, protects editorial and publication workflows, and determines which version is visible to readers.

---

## Responsibilities

The Article aggregate is responsible for:

- Managing the editorial workflow.
- Managing the publication workflow.
- Managing editorial ownership.
- Coordinating `ArticleVersion`.
- Coordinating `ReviewCycle`.
- Protecting business invariants.
- Determining the active Draft Version.
- Determining the current Published Version.
- Managing the public identity (`slug`) of the article.

---

## Not Responsible For

The Article aggregate is NOT responsible for:

- Storing article content.
- Managing media files.
- Managing categories.
- Managing topics.
- Managing references.
- Rendering markdown.
- Searching articles.
- Managing user enrollment.

---

## Aggregate Boundary

### Owns

- ArticleVersion
- ReviewCycle

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
- Every Draft Version belongs to exactly one Article.
- A published slug remains stable unless explicitly changed by an administrative action.

---

## Aggregate Structure

### Fields

- id
- slug
- publicationStatus
- editorialStatus
- ownerId
- draftVersionId
- publishedVersionId
- createdAt
- createdBy
- publishedAt
- publishedBy
- archivedAt
- archivedBy

---

## Notes

The `Article` aggregate stores workflow and lifecycle information only.

Actual editorial content belongs to `ArticleVersion`.

The slug belongs to the `Article` because it represents the public identity of the article rather than the content of a specific version.