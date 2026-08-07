# ArticleTitle Value Object

## Purpose

Represents the title of an ArticleVersion.

The title is immutable and always valid.

---

## Responsibilities

- Store the article title.
- Validate title invariants.
- Normalize whitespace.

---

## Invariants

- Title cannot be null.
- Title cannot be blank.
- Title is automatically trimmed.
- Maximum length is 200 characters.

---

## Notes

ArticleTitle is independent from Slug.

Changing the title does not automatically imply changing the slug.

Slug generation is handled by the Article aggregate according to business rules.

---
# Body Value Object

## Purpose

Represents the editorial content of an ArticleVersion.

In Version 2, the body is stored as Markdown.

---

## Responsibilities

- Store article content.
- Validate content invariants.
- Normalize user input.

---

## Invariants

- Body cannot be null.
- Body cannot be blank.
- Leading and trailing whitespace is automatically removed.
- Maximum length is 50,000 characters.

---

## Notes

The Body stores Markdown in Version 2.

Future versions may migrate to a block-based rich content model without changing the Article Aggregate.

---
# Slug Value Object

## Purpose

Represents the public identity of an Article.

A Slug is immutable, URL-friendly, and uniquely identifies an article from the reader's perspective.

---

## Responsibilities

- Store a URL-friendly identifier.
- Validate slug invariants.
- Normalize user input.

---

## Invariants

- Slug cannot be null.
- Slug cannot be blank.
- Leading and trailing whitespace is removed.
- Only lowercase letters, digits and hyphens are allowed.
- Slug cannot begin or end with a hyphen.
- Slug cannot contain consecutive hyphens.
- Maximum length is 200 characters.

---

## Notes

Slug uniqueness is enforced by the Article Aggregate.

Slug generation is handled outside of this Value Object.