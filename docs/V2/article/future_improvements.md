# Future Improvements

The current implementation intentionally keeps `Source` simple.

Future versions may introduce additional capabilities.

---

## Source Metadata

Additional metadata may be stored.

Examples:

- publisher
- author
- publishedDate
- accessedDate
- language

---

## Source Validation

The system may validate:

- HTTP availability
- HTTPS enforcement
- Allowed domains
- Duplicate references

---

## Rich Source Preview

The system may automatically retrieve:

- page title
- favicon
- preview image
- description

---

## Shared Source Catalog

Source may become an Aggregate Root.

Multiple ArticleVersions could reference the same Source entity.

This enables:

- centralized source management
- duplicate detection
- source reputation
- source deactivation

---

## Source Health Monitoring

The system may periodically check:

- broken links
- redirects
- removed pages

Editors may receive notifications when published sources become unavailable.

---

## Citation Support

Support academic citation formats.

Examples:

- APA
- MLA
- Chicago
- IEEE

---

## Trust Score

Each source may receive a trust score.

For example:

- Official Government
- University
- NGO
- News
- Community
- Personal Blog

This score may assist reviewers during the editorial process.

---
## Future Improvements

Future versions may introduce:

- Reserved slug validation
    - admin
    - login
    - register
    - api
    - explore
    - ...

- International transliteration

Example:

München

↓

munchen

Đức

↓

duc

- Automatic slug generation from ArticleTitle.

- Manual slug override for administrators.

- Slug history.

Example:

study-in-germany

↓

study-in-germany-2026

↓

study-in-germany-guide

Old URLs continue working through redirects.

- SEO redirect management.