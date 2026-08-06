# Article Business Actions

Business actions describe what an `Article` can do from the business perspective.
They represent business capabilities, not API endpoints or database operations.

---

## Create Draft

**Actor**

- Editor

**Description**

Create a new article together with its first Draft Version.

---

## Update Draft

**Actor**

- Editor

**Description**

Modify the current Draft Version without affecting the Published Version.

---

## Submit Review

**Actor**

- Editor

**Description**

Submit the current Draft Version for editorial review.

---

## Withdraw Review

**Actor**

- Editor

**Description**

Withdraw the current review request and continue editing the Draft Version.

---

## Request Changes

**Actor**

- Admin

**Description**

Request the editor to revise the current Draft Version before publication.

---

## Publish

**Actor**

- Admin

**Description**

Approve the current Draft Version and make it publicly available.

---

## Create New Draft

**Actor**

- Editor

**Description**

Start a new editorial cycle by creating a Draft Version from the current Published Version.

---

## Archive

**Actor**

- Admin

**Description**

Remove the article from the public catalog while preserving its complete history.

---

## Restore

**Actor**

- Admin

**Description**

Restore an archived article back to the public catalog.

---

## Transfer Ownership

**Actor**

- Admin

**Description**

Transfer editorial ownership of the article to another editor.

---

## Discard Draft *(Planned)*

**Actor**

- Editor

**Description**

Discard the current Draft Version. If the article has never been published, the article is soft-deleted; otherwise only the active Draft Version is removed.