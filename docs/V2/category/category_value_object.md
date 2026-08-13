# CategoryName Value Object

## Purpose

Represents the name of a Category.

`CategoryName` is immutable and always valid.

---

## Responsibilities

- Store the Category name.
- Validate Category name invariants.
- Normalize user input.
- Provide a stable value for Category identity and display.

---

## Invariants

- Category name cannot be null.
- Category name cannot be blank.
- Leading and trailing whitespace is automatically removed.
- Maximum length is 100 characters.

---

## Normalization

Category names are normalized before being stored.

Leading and trailing whitespace is removed.

For example:

```text
"  Grammar  "
```
becomes:

```text
"Grammar"
```

The original display casing is preserved.

## Case-Insensitive Uniqueness

Category names are unique regardless of letter casing.

The following values represent the same logical Category name:

```text
Grammar
grammar
GRAMMAR
```

Therefore, they cannot coexist as separate Categories.

However, ***CategoryName*** itself does not guarantee uniqueness across Categories.

A Value Object only validates the name of one Category.

Category name collision is handled by:

- Application-level uniqueness checking.
- Database-level unique constraint.

### Notes

CategoryName is independent from Slug.

Category does not currently have a public slug.

The Category name is used as the primary human-readable identifier.

Changing the Category name does not change the Category identity.

Existing ArticleVersions continue to reference the same Category through categoryId.