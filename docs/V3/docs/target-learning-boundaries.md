# Target Learning Boundaries

## 1. Purpose

This document defines the **target business scope of the Learning Context in DeutschHub** as the product evolves from a Course-centered Learning model toward a broader Learning Experience.

The document answers:

> **What business responsibilities should the Learning Context own?**

It is based on:

```text
Learning Discovery
        +
Current Learning Boundaries
        +
Domain Analysis
        +
Application Analysis
````

The purpose is not to immediately decide:

* Aggregates;
* Entities;
* Bounded Contexts;
* packages;
* database schema;
* or implementation details.

Instead, the target boundary first identifies:

> **Which business responsibilities genuinely belong to Learning.**

---

# 2. From Current to Target

The current Learning implementation has a clear core:

```text
Course
   ↓
Enrollment
   ↓
LessonCompletion
   ↓
Progress
```

This represents a primarily Course-centered Learning model.

However, Learning Discovery identifies a broader Learning Experience:

```text
Learning
│
├── Learning Structure
├── Learning Activities
├── Learner State
└── Learning Direction
```

Therefore, the target is not:

```text
Current Course
        ↓
Bigger Course
```

but rather:

```text
Course-centered Learning
        ↓
Learner-centered Learning Experience
```

This is a change in **business scope** before it is a change in architecture.

---

# 3. Target Learning Boundary

The Target Learning Context is intended to cover four groups of responsibilities:

```text
                         LEARNING
                            │
       ┌────────────────────┼────────────────────┐
       │                    │                    │
       ▼                    ▼                    ▼
Learning Structure   Learning Activities   Learner State
       │                    │                    │
       └────────────────────┼────────────────────┘
                            │
                            ▼
                   Learning Direction
```

These four groups do not necessarily correspond to four Bounded Contexts.

They represent:

> **Four groups of business responsibilities used to define the scope of Learning.**

---

# 4. Target Boundary 1 — Learning Structure

## 4.1. Responsibility

Learning Structure is responsible for:

> **What the learner can learn and how learning content is organized into meaningful structures for learning.**

Course remains part of this responsibility.

However, Course is no longer considered the entirety of Learning Structure.

The target Learning Structure may include:

```text
Learning Structure
│
├── Course
├── Vocabulary
├── Grammar
└── Skills
```

Learning may also organize content through concepts such as:

```text
Level
Topic
Learning Area
```

as identified during Learning Discovery.

---

## 4.2. Course in the Target Learning Context

Course remains an important learning structure:

```text
Course
└── Section
    └── Lesson
        └── LessonItem
```

There is no business evidence from discovery that requires removing Course.

The important change is the perspective:

```text
CURRENT

Learning
└── Course
```

is no longer sufficient to describe the target scope.

Instead:

```text
TARGET

Learning
└── Learning Structure
    ├── Course
    ├── Vocabulary
    ├── Grammar
    └── Skills
```

### Target Boundary Finding

> **Course remains inside Learning, but Course is one form of Learning Structure rather than the definition of Learning itself.**

---

# 5. Target Boundary 2 — Learning Activities

This is an important area that is not clearly modeled in the current implementation.

Learning Activities are responsible for:

> **What the learner actually does in order to learn.**

According to Learning Discovery, activities may include:

```text
Learning
Practice
Review
Listening
Speaking
Reading
Writing
```

and learning through:

```text
Video
Authentic Content
```

---

## 5.1. Activities Are Not the Same as Content

This distinction is important.

For example:

```text
Video
```

may be a learning resource.

But:

```text
Watch a video
```

is a learning activity.

Similarly:

```text
Vocabulary
```

may represent learning content.

While:

```text
Practice vocabulary
Review vocabulary
```

represent learning activities.

Therefore:

```text
Learning Structure
        ↓
What can be learned?

Learning Activity
        ↓
What does the learner do?
```

---

## 5.2. Activities May Use Multiple Learning Structures

An Activity does not necessarily belong exclusively to one Course.

For example:

```text
Vocabulary
   ↓
Practice

Grammar
   ↓
Practice

Video
   ↓
Listening

Article
   ↓
Reading
```

Activities may also operate within a Course:

```text
Course Lesson
      ↓
Listening Activity
```

This leads to an important target boundary:

> **Learning Activities should be able to operate on learning resources without necessarily being identical to the structure that organizes those resources.**

---

## 5.3. Practice and Review

Practice and Review should be treated as potentially meaningful business concepts if DeutschHub intends to support adaptive learning.

Conceptually:

```text
Practice
=
The learner actively trains a capability.
```

while:

```text
Review
=
The learner revisits previously learned material.
```

If these activities have different business rules, they should not simply be represented as:

```text
LessonCompletion
```

### Target Boundary Finding

Target Learning should be capable of representing:

```text
Learning Activity
```

without being absolutely dependent on:

```text
Course
```

However, this document does **not** decide whether Activity should become an Aggregate or Bounded Context.

---

# 6. Target Boundary 3 — Learner State

This is the most important expansion from the current implementation.

Learner State is responsible for:

> **What the system knows about the learner's learning state.**

Target Learner State may include:

```text
Learner State
│
├── Progress
├── Competency
├── Current Level
├── Learning Evidence
├── Learning Statistics
└── Achievement
```

---

# 7. Course Progress and Learner State Must Remain Distinct

The target still retains:

```text
Enrollment
    └── Course Progress
```

because this state answers a specific question:

> How far has the learner progressed in a particular Course?

The target also introduces a broader question:

> Where is the learner in their overall learning journey?

Therefore:

```text
Course Progress
        ≠
Learner State
```

For example:

```text
Course A
    80% complete

Course B
    40% complete
```

does not directly tell us:

```text
German Competency
Listening ability
Speaking ability
Vocabulary knowledge
Grammar knowledge
Current Level
```

---

# 8. Target Learner State — Learning Evidence

Learner State needs a basis.

Therefore, the target distinguishes:

```text
Evidence
   ↓
State
```

Potential sources of evidence include:

```text
LessonCompletion
QuizAttempt
Practice
Review
Skill Activities
Vocabulary Activities
Grammar Activities
```

Conceptually:

```text
                    Learning Evidence
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
Lesson Completion     Assessment          Activities
        │                  │                  │
        └──────────────────┼──────────────────┘
                           ↓
                    Learner State
```

This does not mean that all evidence must be stored inside one object.

The target principle is:

> **Learner State should have a clear relationship with evidence produced by learning activities.**

---

# 9. Target Learner State — Competency

The target must distinguish:

```text
Progress
≠
Competency
```

Progress answers:

> How much has the learner completed?

Competency answers:

> What can the learner currently demonstrate?

For example:

```text
Course Progress = 90%
```

does not imply:

```text
Speaking Competency = 90%
```

Therefore, Competency represents a distinct responsibility within Learner State.

---

# 10. Target Learner State — Current Level

Current Level must also be distinguished from:

```text
Course Level
```

Target:

```text
Course CEFR Level
=
Level of the learning content
```

while:

```text
Learner Current Level
=
Estimated / assessed state of the learner
```

For example:

```text
Course
    Level = B1
```

does not mean:

```text
Learner
    Current Level = B1
```

A learner may be studying B1 content while their actual ability is not yet B1 across all skills.

---

# 11. Current Level Is Not Certification

The target also maintains the distinction:

```text
Learner Current Level
        ≠
Official Certification
```

Current Level may represent an:

```text
Estimated
Assessed
System-derived
```

learning state within DeutschHub.

Official certification belongs to:

```text
External Examination / Certification
```

Therefore:

```text
Current Level
=
Learning State

Certificate
=
Certification Outcome
```

These concepts should not be treated as equivalent.

---

# 12. Target Boundary 4 — Learning Direction

Learning Direction is responsible for:

> **What the learner should do next in order to continue progressing according to their goals and current learning state.**

This is different from Learner State.

```text
Learner State
=
Where am I?

Learning Direction
=
Where should I go next?
```

---

# 13. Target Learning Direction

Learning Direction may include:

```text
Learning Direction
│
├── Daily Learning
├── Learning Plan
├── Recommendations
├── Review Due
├── Weakness-oriented Practice
├── Learning Goals
└── Exam Preparation
```

These responsibilities are derived from Learning Discovery and represent **target business scope**, not current implementation.

---

# 14. Learning Direction Depends on Learner State

The target can be represented as:

```text
Learning Evidence
        ↓
Learner State
        ↓
Learning Direction
        ↓
Next Learning Activity
```

For example:

```text
Learner State
│
├── Listening weaker
├── Vocabulary improving
└── Goal = B1 exam
        ↓
Learning Direction
        ↓
Recommend listening practice
        ↓
Learning Activity
```

The important distinction is:

> **Learning Direction consumes learner state; it does not define learner state.**

---

# 15. Learning Direction and Recommendation

Recommendation should not simply mean:

```text
"Random content recommendation"
```

Within the target Learning Context, Recommendation has a learning purpose:

> Help the learner decide what to learn next based on their state and goals.

Therefore:

```text
Recommendation
    ← Learner State
    ← Learning Goals
    ← Learning Direction
```

Recommendation is a **learning decision**, not simply a content listing.

---

# 16. Target Learning Boundary — Overall

The Target Learning Context can be represented as:

```text
                         LEARNING CONTEXT
                                │
        ┌───────────────────────┼────────────────────────┐
        │                       │                        │
        ▼                       ▼                        ▼
Learning Structure       Learning Activities       Learner State
        │                       │                        │
        │                       │               ┌────────┼────────┐
        │                       │               │        │        │
        │                       │           Progress Competency Level
        │                       │
        │                       │               Evidence / Statistics
        │                       │
        └───────────────────────┼────────────────────────┐
                                │                        │
                                ▼                        ▼
                         Learning Evidence       Learning Direction
                                                         │
                                      ┌──────────────────┼──────────────────┐
                                      │                  │                  │
                                Daily Learning     Recommendation      Exam Preparation
```

A typical learning flow may be:

```text
Learning Structure
        ↓
Learning Activity
        ↓
Learning Evidence
        ↓
Learner State
        ↓
Learning Direction
        ↓
Next Learning Activity
```

This is the target business learning loop.

It is **not an architecture diagram**.

---

# 17. Course in the Target Learning Context

Course remains an important component:

```text
Learning Structure
        │
      Course
        │
     Section
        │
      Lesson
        │
    LessonItem
```

However, Course should not become the place where every Learning responsibility is stored.

The target distinction is:

```text
Course
=
Organizes learning content

Activity
=
Represents learning action

Learner State
=
Represents learner condition

Learning Direction
=
Guides the next learning action
```

This is one of the most important boundary decisions in V3.

---

# 18. Enrollment in the Target Learning Context

Enrollment retains the responsibility:

> **The learner's participation in a specific Course.**

```text
Course
    ↑
    │
Enrollment
    │
    └── Course Progress
```

Enrollment should not become the place where the system stores:

```text
Learner Competency
Learner Current Level
Learning Goals
Recommendations
```

because these concepts have a broader scope than a single Course.

---

# 19. Progress in the Target Learning Context

The target maintains two distinct concepts:

```text
Course Progress
```

and:

```text
Learner State
```

Course Progress answers:

```text
How far have I progressed in this Course?
```

Learner State answers:

```text
What is my current learning condition?
```

A single `Progress` concept should not be forced to answer both questions.

---

# 20. Assessment in the Target Learning Context

Assessment plays an important role because it produces evidence.

```text
Assessment
    ↓
Assessment Result
    ↓
Learning Evidence
    ↓
Learner State
```

Therefore:

```text
Quiz
QuizAttempt
UserAnswer
Score
```

may contribute to Learning Evidence.

However, the target boundary **does not conclude that Assessment must become a separate Bounded Context**.

That decision belongs to a later stage:

```text
Boundary
    ↓
Responsibility
    ↓
Coupling
    ↓
Data ownership
    ↓
Bounded Context decision
```

---

# 21. Certification in the Target Learning Context

Certification should remain distinct from Learner State.

Conceptually:

```text
Learning
    ↓
Assessment
    ↓
Learning Outcome
    ↓
Certification
```

However:

```text
Learner Current Level
```

does not mean:

```text
Certificate
```

Certification may be an outcome of learning, but it should not become the only way to represent learner achievement.

---

# 22. Vocabulary, Grammar, and Skills in the Target

Target Learning should be capable of supporting:

```text
Vocabulary
Grammar
Skills
```

However, it is too early to conclude that each must become an independent context.

For example:

```text
Vocabulary
```

may be both:

```text
Learning Structure
```

and a source of resources for:

```text
Practice
Review
```

Similarly:

```text
Listening
```

may relate to:

```text
Skill
```

while also being performed through:

```text
Learning Activity
```

Therefore, target boundaries should be defined according to:

> **Business responsibility**

rather than:

> **Feature name / screen name / content type.**

---

# 23. Target Boundary Classification

The target scope can be grouped into four areas.

## 23.1. Learning Structure

```text
Course
Vocabulary
Grammar
Skills
Level / Topic organization
```

Responsibility:

> Define and organize what can be learned.

---

## 23.2. Learning Activities

```text
Learning
Practice
Review
Listening
Speaking
Reading
Writing
```

Responsibility:

> Define what the learner does to learn.

---

## 23.3. Learner State

```text
Progress
Competency
Current Level
Learning Evidence
Learning Statistics
Achievement
```

Responsibility:

> Represent what is known about the learner's learning state.

---

## 23.4. Learning Direction

```text
Daily Learning
Learning Plan
Recommendations
Review Due
Learning Goals
Exam Preparation
```

Responsibility:

> Determine or guide what the learner should do next.

---

# 24. Target Responsibility Model

The four groups can be expressed through four fundamental questions:

```text
Learning Structure
    ↓
What can I learn?

Learning Activities
    ↓
What can I do to learn?

Learner State
    ↓
Where am I?

Learning Direction
    ↓
What should I do next?
```

This provides a business-oriented description of the Learning boundary without depending on the current implementation.

---

# 25. Target Learning Loop

A complete Learning Experience can be viewed as:

```text
             ┌───────────────────────┐
             │   Learning Structure  │
             │    What can I learn?  │
             └───────────┬───────────┘
                         ↓
             ┌───────────────────────┐
             │   Learning Activity   │
             │   What do I do?       │
             └───────────┬───────────┘
                         ↓
             ┌───────────────────────┐
             │   Learning Evidence  │
             │   What happened?      │
             └───────────┬───────────┘
                         ↓
             ┌───────────────────────┐
             │     Learner State     │
             │     Where am I?       │
             └───────────┬───────────┘
                         ↓
             ┌───────────────────────┐
             │  Learning Direction   │
             │  What next?           │
             └───────────┬───────────┘
                         │
                         └──────────────→ Next Activity
```

This is the target business loop.

It is not yet a technical architecture decision.

---

# 26. What Belongs to Learning

Based on the target scope, Learning should be responsible for questions directly related to:

```text
Learning content organization
Learning activities
Learner learning state
Learning progress
Learning evidence
Learning direction
```

More generally:

> **Learning is responsible for enabling, recording, understanding, and guiding the learner's learning journey.**

---

# 27. What Should Not Automatically Belong to Learning

A concept being related to the learner does not automatically mean that it belongs to Learning.

For example:

```text
Identity
```

is responsible for:

```text
Who is the user?
```

Learning is responsible for:

```text
How is this user learning?
```

Similarly, Media has its own responsibility for media resources.

Content has its own responsibility for content lifecycle.

Therefore, Target Learning should not absorb:

```text
Identity
Content Management
Media Management
```

simply because Learning uses them.

---

# 28. Important Cross-Context Relationships

Target Learning may use concepts from other Contexts:

```text
Identity
    ↓
Learner identity

Content
    ↓
Learning resources

Media
    ↓
Learning media

Learning
    ↓
Learning experience
```

This follows an important principle:

> **Using another Context is not the same as owning that Context's responsibility.**

---

# 29. Target Boundary Decisions

The following target-level business decisions are established.

### Decision 1

**Learning is broader than Course.**

Course remains inside Learning but is not the definition of Learning.

### Decision 2

**Learning Structure and Learning Activities are different responsibilities.**

What can be learned is different from what the learner does.

### Decision 3

**Course Progress and Learner State are different responsibilities.**

Course completion does not fully represent learner state.

### Decision 4

**Learning Evidence is distinct from Learner State.**

Evidence contributes to understanding the learner but is not necessarily the learner state itself.

### Decision 5

**Learner Current Level is distinct from Course Level.**

Content classification and learner ability are different concepts.

### Decision 6

**Learner Current Level is distinct from Certification.**

A system-estimated learning state is not an official external certification.

### Decision 7

**Learning Direction is distinct from Learner State.**

State describes where the learner is; Direction guides what happens next.

### Decision 8

**The four dimensions do not automatically define four Bounded Contexts.**

They are groups of business responsibilities.

---

# 30. What Remains Undecided

Target Learning Boundaries intentionally does **not** decide:

* whether Course should remain in the same Bounded Context;
* whether Learning Activities need a separate Bounded Context;
* whether Assessment should become a separate Bounded Context;
* whether Learner State should be an Aggregate, domain service, or derived/read model;
* whether Learning Direction should be domain logic or an application-level capability;
* whether Vocabulary and Grammar require independent domain boundaries;
* whether Skills should have their own model;
* exact Aggregate boundaries;
* exact Entity / Value Object structure;
* database ownership;
* API boundaries;
* package structure.

These decisions should only be made after responsibilities, invariants, ownership, and dependencies have been analyzed.

---

# 31. Target Boundary Map

The Target Learning Context can therefore be summarized as:

```text
                           LEARNING
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
     STRUCTURE            ACTIVITIES          LEARNER STATE
          │                   │                   │
      Course              Practice            Progress
      Vocabulary          Review              Competency
      Grammar             Listening            Current Level
      Skills              Speaking             Evidence
                          Reading              Statistics
                          Writing              Achievement
          │                   │                   │
          └───────────────────┼───────────────────┘
                              │
                              ▼
                     LEARNING DIRECTION
                              │
                ┌─────────────┼─────────────┐
                │             │             │
             Daily         Goals       Recommendation
            Learning                      / Next Step
                │
                └─────────────┬─────────────┘
                              ↓
                       Next Activity
```

---

# 32. Target Learning Boundary Statement

The Target Learning Context can be described as:

> **The Learning Context is responsible for enabling and organizing learning, supporting learning activities, representing the learner's learning state and evidence, and guiding the learner toward appropriate next learning actions.**

The four responsibilities can be summarized as:

```text
Learning Structure
=
What can be learned

Learning Activities
=
What the learner does

Learner State
=
What is known about the learner

Learning Direction
=
What the learner should do next
```

Course is an important part of Learning Structure, but it does not represent the entire Learning Context.

---

# 33. Conclusion

The Target Learning Context does not aim to replace the current Course-centered implementation with an entirely different model.

Instead, it expands the business scope from:

```text
Course
   ↓
Enrollment
   ↓
Progress
```

toward a broader Learning Experience loop:

```text
Structure
   ↓
Activity
   ↓
Evidence
   ↓
Learner State
   ↓
Learning Direction
   ↓
Next Activity
```

The most important change is:

> **Learning is no longer defined only by the learner's completion of content.**

The Target Learning Context should be capable of:

```text
Organize learning
        +
Enable learning activities
        +
Understand learner state
        +
Guide future learning
```

However, this target boundary **does not yet determine the technical structure**.

The next design stages should only proceed after confirming that these four responsibility groups accurately represent the business scope DeutschHub intends to build.

````

current-learning-boundaries.md
        │
        │  What exists today?
        ▼
target-learning-boundaries.md
        │
        │  What should Learning own?
        ▼
       GAP
        │
        ▼
Architecture / DDD Design

