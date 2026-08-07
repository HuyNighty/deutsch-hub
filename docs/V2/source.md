# Source Value Object

## Purpose

`Source` represents a reference used to support the factual content of an ArticleVersion.

A Source is immutable and belongs exclusively to a single ArticleVersion.

It is treated as part of the editorial snapshot rather than a shared business entity.

---

## Responsibilities

The Source Value Object is responsible for:

- Storing reference information.
- Identifying the original source used by the editor.
- Preserving references as part of a published article snapshot.

---

## Fields

- title
- url

---

## Invariants

A Source guarantees that:

- Title must not be empty.
- URL must not be empty.
- URL must be a valid URI.

---

## Notes

Source belongs to ArticleVersion.

It is never shared between different articles.

When an ArticleVersion is published, its Sources become immutable together with the entire version.

Changing a Source requires creating a new Draft Version.