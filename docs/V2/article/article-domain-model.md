# Article Aggregate

## Purpose

`Article` is the Aggregate Root of the Explore Germany Content Context.

It represents the editorial and publication lifecycle of an article rather than the article content itself.

The Article aggregate coordinates article versions, protects editorial and publication workflows, and determines which version is currently used for editing or publication.

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
- Coordinating review submission and review-related workflows.

---

## Not Responsible For

The Article aggregate is NOT responsible for:

- Storing editorial content directly.
- Managing media files.
- Managing categories.
- Managing topics.
- Managing sources.
- Rendering Markdown.
- Searching articles.
- Managing user enrollment.

---

# Aggregate Boundary

## Aggregate Root

- `Article`

## Child Entities

- `ArticleVersion`
    - `ReviewCycle`

## Aggregate References

The Article aggregate may reference other aggregates or bounded contexts through identifiers or value objects.

- `Category`
- `Topic`
- `Media`
- `User`

The Article aggregate does not own the lifecycle of these referenced objects.

---

# Aggregate Structure

```text
Article Aggregate
│
└── Article
    │
    └── ArticleVersion
        │
        └── ReviewCycle