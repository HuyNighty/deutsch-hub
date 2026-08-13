# Content Evolution Roadmap

## Phase 1

Storage Model

Markdown

```text
Body

↓

String
```

Goals

- Fast implementation
- Easy rendering
- Easy versioning
- Simple persistence

---

## Phase 2

Storage Model

Block-based Rich Content

```text
Body

↓

List<Block>
```

Possible Blocks

- Heading
- Paragraph
- Image
- Quote
- Code
- Table
- Divider
- Callout
- Video

Goals

- Rich editing
- Drag & Drop
- Better collaboration
- Notion-like editing experience

---

## Migration Strategy

The migration should only affect the Body implementation.

Business Actions remain unchanged.

Article Aggregate remains unchanged.

ArticleVersion remains unchanged.

Only the internal representation of Body evolves.