# Create Draft

## Actor

- Editor

## Preconditions

- User has `CONTENT_EDITOR` permission.

## Business Rules

- Create a new `Article`.
- Create the first `ArticleVersion` as the Draft Version.
- Generate a unique slug.
- Assign ownership to the current editor.
- Set `PublicationStatus = UNPUBLISHED`.
- Set `EditorialStatus = DRAFT`.

## State Transition

Publication:

null → UNPUBLISHED

Editorial:

null → DRAFT

## Result

- A new draft article is created.
- The editor becomes the owner of the article.
- The first Draft Version is ready for editing.

---

# Update Draft

## Actor

- Editor
- Admin (for emergency editing)

## Preconditions

- `EditorialStatus` is `DRAFT` or `CHANGES_REQUESTED`.
- A Draft Version exists.
- The actor must own the Article, unless the Application Authorization Policy grants administrative permission.
## Business Rules

- Only the current Draft Version can be modified.
- The Published Version must never be modified.
- Ownership cannot be changed.
- Publication Status cannot be changed.
- Editorial Status cannot be changed.
- Updating a draft does not create a new `ArticleVersion`.
- The update operation is atomic (all changes succeed or all changes are rolled back).
- Only editable content may be modified.
- Changing the title does not automatically change the Article slug.
- The slug is part of the Article public identity.
- Slug changes require an explicit administrative action.

## Editable Content

- Title
- Summary
- Body
- Cover Media
- Primary Category
- Topics
- References

## State Transition

Editorial:

No change.

Publication:

No change.

## Result

- The Draft Version is updated.
- Readers continue to access the current Published Version (if one exists).
---
# Submit Review

## Actor

- Editor
- Admin

## Preconditions

- EditorialStatus is `DRAFT` or `CHANGES_REQUESTED`.
- A Draft Version exists.
- The actor must own the Article, unless the Application Authorization Policy grants administrative permission.

## Business Rules

- The Draft Version must contain all required content.
- Title is required.
- Summary is required.
- Body is required.
- Cover Media is required.
- A Primary Category is required.
- At least one Topic is required.
- At least one Reference is required.
- All referenced Sources must exist and be active.
- The selected Category must be active.
- All selected Topics must be active.
- The Draft Version becomes non-editable while the Article is `IN_REVIEW`.- Record the submission time.
- Record the submitting user.
- The business needs to keep a history of reviews.
- Create a Review History entry (It will be implemented in future versions.).

## State Transition

Editorial

DRAFT
or
CHANGES_REQUESTED

↓

IN_REVIEW

Publication

No change.

## Result

- The draft is submitted for review.
- Editors can no longer modify the draft.
- The article is waiting for an administrative decision.

---
# Withdraw Review

## Actor

- Editor
- Admin

## Preconditions

- EditorialStatus is `IN_REVIEW`.
- The article has not been reviewed yet.
- The actor owns the article or has administrative permission.

## Business Rules

- Cancel the current review request.
- Unlock the Draft Version for editing.
- Complete the current Review Cycle with result = WITHDRAWN.

## State Transition

Editorial

IN_REVIEW

↓

DRAFT

Publication

No change.

## Result

- The Draft Version becomes editable again.
- The article is removed from the review queue.
---

# Request Changes

## Actor

- Admin

## Preconditions

- `EditorialStatus` is `IN_REVIEW`.
- The current Review Cycle is still open.

## Business Rules

- Reject the current review request.
- Reviewer feedback is required.
- Unlock the Draft Version for editing.
- Continue editing on the existing Draft Version.
- Do not create a new `ArticleVersion`.
- Record the review time.
- Record the reviewer.
- Complete the current Review Cycle with result = `CHANGES_REQUESTED`.

## State Transition

Editorial

IN_REVIEW

↓

CHANGES_REQUESTED

Publication

No change.

## Result

- The Draft Version becomes editable again.
- The editor receives review feedback.
- The current Review Cycle is completed.

---
---

# Publish

## Actor

- Admin

## Preconditions

- `EditorialStatus` is `IN_REVIEW`.
- A Draft Version exists.
- The current Review Cycle is still open.
- The current Draft Version satisfies Publication Completeness.
- All Application-level reference validation has succeeded.

## Business Rules

- Approve the current Draft Version.
- Replace the current Published Version with the Draft Version.
- The Draft Version becomes the new Published Version.
- The Article must always have only one current Published Version.
- Clear the active Draft Version.
- Set `draftVersionId = null`.
- Set `publishedVersionId` to the current Draft Version.
- Set `PublicationStatus = PUBLISHED`.
- Set `EditorialStatus = IDLE`.
- Record the publication time.
- Record the publishing user.
- Complete the current Review Cycle with result = `APPROVED`.
- The publish operation must be atomic.

## State Transition

Editorial

IN_REVIEW

↓

IDLE

Publication

UNPUBLISHED → PUBLISHED

or

PUBLISHED → PUBLISHED

## Result

- The Draft Version becomes the current Published Version.
- Readers immediately see the new published content.
- The current Review Cycle is completed.
-
> **Note**
>
> Publishing does not create a new `ArticleVersion`.
> The current Draft Version is promoted to become the new Published Version.
> A new `ArticleVersion` is created only when a published article starts a new editorial cycle through **Create New Draft**.

---

# Create New Draft

## Actor

- Editor
- Admin

## Preconditions

One of the following conditions must be true:

### Published Article

- `PublicationStatus` is `PUBLISHED`.
- `EditorialStatus` is `IDLE`.
- A Published Version exists.
- No active Draft Version exists.

### Archived Article

- `PublicationStatus` is `ARCHIVED`.
- A Published Version exists.
- No active Draft Version exists.

The actor must own the Article or have administrative permission.


## Business Rules

- Create a new Draft Version from the current Published Version.
- Clone the entire Published Version as a complete snapshot.
- Generate a new version number.
- Assign the new version as the active Draft Version.
- Preserve the current Published Version.
- The Article must have only one active Draft Version.
- Keep the same slug.
- Set `EditorialStatus = DRAFT`.
- Preserve the current `PublicationStatus`.
- Record the draft creator.
- Record the draft creation time.

## State Transition

Editorial

IDLE

↓

DRAFT

Publication

PUBLISHED

↓

PUBLISHED

## Result

- A new Draft Version is created.
- Readers continue to see the current Published Version.
- Editors start a new editorial cycle without affecting public content.
> **Note**
>
> Creating a new draft starts a new editorial cycle.
> The new Draft Version is created by cloning the current Published Version,
> ensuring that every `ArticleVersion` remains a complete and independent snapshot.

---
# Archive

## Actor

- Admin

## Preconditions

- `PublicationStatus` is `PUBLISHED`.

## Business Rules

- Remove the Article from the public catalog immediately.
- Preserve the Article and all Article Versions.
- Preserve the complete Review History.
- Set `PublicationStatus = ARCHIVED`.
- Do not modify or delete the Published Version.
- If a Review Cycle is currently `PENDING`, withdraw the active Review Cycle.
- Record the archive time.
- Record the archiving user.

## State Transition

Editorial

IDLE

↓

IDLE

Publication

PUBLISHED

↓

ARCHIVED

## Result

- Readers can no longer access the article.
- Public endpoints return HTTP 410 (Gone).
- All historical data is preserved.

---
# Restore

`Restore` is intentionally not supported in V2.

An archived Article cannot return directly to the public catalog.

If the Article needs to become public again, the editorial workflow must be completed again:

```text
ARCHIVED
    ↓
Create New Draft
    ↓
Update Draft
    ↓
Submit Review
    ↓
Admin Review
    ↓
Publish
    ↓
PUBLISHED
```

# Transfer Ownership

## Actor

- Admin

## Preconditions

- The Article exists.
- The new owner exists.
- The new owner has `CONTENT_EDITOR` permission.
- `EditorialStatus` is not `IN_REVIEW`.

## Business Rules

- Transfer ownership of the Article to the new editor.
- The current Draft Version (if any) is transferred together with the Article.
- Do not modify the Draft Version or the Published Version.
- Do not create a new `ArticleVersion`.
- Do not change `PublicationStatus`.
- Do not change `EditorialStatus`.
- Preserve the complete Review History.
- Record the transfer time.
- Record the transferring user.

## State Transition

Editorial

No change.

Publication

No change.

## Result

- The new editor becomes responsible for future editorial work.
- The previous owner no longer has editorial ownership.

| Publication   | Editorial          | Can Update | Submit Review | Archive | Publish |
| ------------- | ------------------ |-----------:|--------------:|--------:|--------:|
| `UNPUBLISHED` | `DRAFT`            |     `Yes`  |           `Yes` |      No |      No |
| `UNPUBLISHED` | `IN_REVIEW`        |         No |            No |      No |     `Yes` |
| `UNPUBLISHED` | `CHANGE_REQUESTED` |      `Yes` |           `Yes` |      No |      No |
| `PUBLISHED`   | `IDLE`             |         No |            No |     `Yes` |      No |
| `PUBLISHED`   | `DRAFT`            |      `Yes` |           `Yes` |     `Yes` |      No |
| `PUBLISHED`   | `IN_REVIEW`        |         No |            No |     `Yes` |     `Yes` |
| `PUBLISHED`   | `CHANGE_REQUESTED` |      `Yes` |           `Yes` |     `Yes` |      No |
| `ARCHIVED`    | `IDLE`             |         No |            No |      No |      No |
| `ARCHIVED`    | `DRAFT`            |      `Yes` |           `Yes` |      No |      No |
| `ARCHIVED`    | `IN_REVIEW`        |         No |            No |      No |     `Yes` |

# Ownership and Authorization

Ownership is represented by `Article.ownerId`.

The Article aggregate provides domain behavior to verify ownership:

```java
article.ensureOwnedBy(actorId);