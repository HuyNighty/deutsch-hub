# Topic Name Value Object

## Purpose

`TopicName` represents the name of a Topic.

`TopicName` is immutable and always valid.

---

## Responsibilities

- Store the Topic name.
- Validate Topic name invariants.
- Normalize user input.
- Provide a stable human-readable value for the Topic.

---

## Invariants

- Topic name cannot be null.
- Topic name cannot be blank.
- Leading and trailing whitespace is automatically removed.
- Maximum length is 100 characters.

---

## Normalization

Leading and trailing whitespace is removed.

For example:

```text
"  Natural Expressions  "
````

becomes:

```text
"Natural Expressions"
```

The original display casing is preserved.

For example:

```text
"Natural German Expressions"
```

remains:

```text
"Natural German Expressions"
```

---

## Case-Insensitive Uniqueness

Topic names are compared case-insensitively for uniqueness within the same Category.

The following values represent the same logical Topic name:

```text
Natural Expressions
natural expressions
NATURAL EXPRESSIONS
```

Therefore, they cannot coexist within the same Category.

However, the same logical Topic name may exist under different Categories.

For example:

```text
Communication
└── Expressions

Vocabulary
└── Expressions
```

may coexist.

Topic name uniqueness is therefore scoped by:

```text
categoryId + logical topic name
```

`TopicName` itself does not guarantee uniqueness across Topics.

Uniqueness is handled through:

* Application-level collision checking.
* Database-level unique constraint.
