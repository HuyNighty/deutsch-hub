# Competency Domain Model

## 1. Purpose

This document defines the business meaning, identity, lifecycle, invariants, consistency rules, and aggregate-boundary decision for Competency within the DeutschHub Learning Context.

Competency represents a learner's demonstrated capability within a defined Competency Scope. In the current V3 scope, German Language is the primary demonstrated competency scope.

This document is a domain model specification, not an implementation specification.

---

## 2. Business Meaning

Competency represents a learner's demonstrated capability within a defined learning domain.

Competency is not equivalent to:

- Course Progress
- Learning Evidence
- Learning Activity
- Assessment Result
- Current Level
- Certification Level

Competency represents the learner's demonstrated capability, while Current Level represents the proficiency classification assigned to that capability.

For example:

```text
German Competency
    Current Level = B2
````

B2 is therefore the classification of the learner's demonstrated German competency, not the competency itself.

---

## 3. Competency Scope

A learner may have multiple Competencies.

Each Competency is distinguished by its defined Competency Scope.

Conceptually:

```text
User
├── Competency
│     Scope = German Language
│
├── Competency
│     Scope = ...
│
└── Competency
      Scope = ...
```

The same User may therefore have multiple Competencies when their scopes are different.

In the current DeutschHub V3 scope, German Language is the confirmed competency scope.

The domain does not introduce additional competency scopes unless required by the business scope.

### Scope Identity Rule

For a given User and Competency Scope, at most one Competency may exist.

```text
User + Competency Scope
        ↓
   one Competency
```

A change in Current Level does not create a new Competency.

For example:

```text
German Competency
Current Level = B1

        ↓
Pass B2 Level Assessment

        ↓

German Competency
Current Level = B2
```

This remains the same Competency.

---

## 4. Skill Dimensions

Skill dimensions are dimensions used by a Level Assessment to evaluate the learner's competency.

Examples include:

* Vocabulary
* Grammar
* Reading
* Listening
* Speaking
* Writing

These dimensions are not modeled as separate Competencies by default.

Conceptually:

```text
German Level Assessment
├── Vocabulary
├── Grammar
├── Reading
├── Listening
├── Speaking
└── Writing
```

The dimensions provide assessment information that contributes to the overall assessment result.

They do not independently establish Competency or Current Level.

---

## 5. Assessment-Based Competency

Competency is determined through Level Assessment evidence.

Only Level Assessment results are authoritative for establishing or updating Competency and Current Level.

Ordinary learning and practice activities do not directly establish Competency.

Examples of non-authoritative learning activities include:

* Course quizzes
* External quizzes
* Practice exercises
* Other learning activities

These activities support learning and skill development but do not directly establish Competency or Current Level.

The conceptual flow is:

```text
Learning Activities
    ↓
Learning / Practice Support
    ↓
Skill Development


Level Assessment
    ↓
Assessment Result
    ↓
Learning Evidence
    ↓
Competency
    ↓
Current Level
```

---

## 6. Level Assessment

A Level Assessment evaluates a learner against a defined proficiency level through its configured skill dimensions.

A Level Assessment produces an Assessment Result.

The Assessment Result is determined using the assessment's passing threshold.

Conceptually:

```text
Level Assessment
    ↓
Score
    ↓
Passing Threshold
    ↓
Assessment Result
```

If the learner achieves the required threshold:

```text
Score >= Passing Threshold
        ↓
PASSED
```

A passed Level Assessment provides sufficient evidence to establish the corresponding proficiency level.

---

## 7. Competency and Learning Evidence

Learning Evidence represents historical and observable facts about the learner's learning or assessment outcomes.

Competency represents the current learner-state conclusion derived from authoritative assessment evidence.

Therefore:

```text
Learning Evidence
    ≠
Competency
```

The relationship is:

```text
Level Assessment
    ↓
Assessment Result
    ↓
Learning Evidence
    ↓
Competency State
```

Assessment evidence remains historical.

A new assessment does not replace or erase previous assessment attempts.

---

## 8. Assessment Retake Rules

A learner may retake the same Level Assessment multiple times.

For example:

```text
B2 Level Assessment

Attempt 1 → 62% → FAILED
Attempt 2 → 68% → FAILED
Attempt 3 → 74% → PASSED
```

The first valid PASSED attempt is sufficient to establish the corresponding level.

The system does not require:

* the highest score;
* the latest attempt; or
* a minimum number of attempts.

Once a valid attempt is PASSED, the corresponding level may be established.

Previous attempts remain historical evidence.

---

## 9. Current Level

Current Level is the learner's current proficiency classification for the relevant Competency Scope.

Competency and Current Level are distinct concepts:

```text
Competency
    = demonstrated capability

Current Level
    = proficiency classification of that capability
```

For example:

```text
German Competency
    Current Level = B2
```

A passed higher-level Level Assessment may update Current Level.

A passed lower-level assessment does not change an already established higher Current Level.

Example:

```text
Current Level = B2

Pass A2 Assessment
    ↓
Current Level remains B2
```

---

## 10. Competency Lifecycle

Competency is created for the learner's relevant Competency Scope but is initially unassessed.

### Initial State

```text
UNASSESSED
Current Level = UNKNOWN
```

At this stage, the system has not established the learner's proficiency level through a valid Level Assessment.

### Assessment

When a valid Level Assessment attempt is PASSED:

```text
UNASSESSED
    ↓
PASSED Level Assessment
    ↓
ASSESSED
```

The corresponding Current Level is then established.

Example:

```text
UNASSESSED
Current Level = UNKNOWN

        ↓
B2 Level Assessment
Score = 82%
Result = PASSED

        ↓

ASSESSED
Current Level = B2
```

### Subsequent Assessment

A subsequent higher-level passed assessment may update the Current Level:

```text
ASSESSED
Current Level = B1

        ↓
Pass B2 Assessment

        ↓

ASSESSED
Current Level = B2
```

The Competency itself remains the same.

---

## 11. Failed Assessment

A failed Level Assessment does not automatically reduce an already established Competency or Current Level.

Example:

```text
Current Level = B2

B2 Assessment
    ↓
FAILED

        ↓

Current Level remains B2
```

Likewise, failing an assessment for a higher level does not reduce the learner's existing level.

```text
Current Level = B2

C1 Assessment
    ↓
FAILED

        ↓

Current Level remains B2
```

A failed assessment therefore produces no automatic downgrade transition.

---

## 12. No Course Completion Prerequisite

Course Progress is not a prerequisite for taking a Level Assessment.

A learner may use a Level Assessment to establish their current proficiency regardless of how much of the corresponding course content they have completed.

For example:

```text
User enters DeutschHub
Current Level = UNKNOWN

        ↓

Take Level Assessment

        ↓

Pass B2

        ↓

Current Level = B2
```

The learner is not required to complete A1, A2, and B1 courses before being assessed for B2.

This allows the system to recognize learners who already possess knowledge or experience outside DeutschHub.

---

## 13. Competency Invariants

### Invariant 1 — Unique Competency per User and Scope

For a given User and Competency Scope, at most one Competency may exist.

```text
User + Scope → one Competency
```

A level change updates the existing Competency instead of creating another Competency.

---

### Invariant 2 — UNASSESSED Consistency

A Competency with status `UNASSESSED` must not have an established Current Level.

Valid:

```text
UNASSESSED
Current Level = UNKNOWN
```

Invalid:

```text
UNASSESSED
Current Level = B2
```

---

### Invariant 3 — No Automatic Downgrade

An Assessment Result must not cause an existing Current Level to decrease.

```text
Current Level = B2

Assessment Level = A2
Assessment Result = PASSED

        ↓

Current Level remains B2
```

Likewise:

```text
Current Level = B2

Assessment Level = C1
Assessment Result = FAILED

        ↓

Current Level remains B2
```

This rule does not prevent a future business rule from introducing a separate mechanism for changing or degrading competency.

No such mechanism is currently defined for V3.

---

### Invariant 4 — Current Level Requires Supporting Evidence

An established Current Level must be supported by a valid passed Level Assessment.

A Current Level must not exist solely because it was manually selected or inferred from ordinary learning activity.

A higher passed level can support the corresponding lower proficiency classification.

For example:

```text
C1 Assessment → PASSED
        ↓
Current Level = C1
```

does not require a separate B2 assessment to establish C1.

---

### Invariant 5 — Assessment Is Required to Establish Competency

A Competency can transition from `UNASSESSED` to `ASSESSED` only when a valid Level Assessment attempt is PASSED.

The following do not establish Competency:

```text
Course Quiz → PASSED
Practice → high score
Course Completion
User-selected level
```

The authoritative transition is:

```text
Level Assessment
    ↓
PASSED
    ↓
Competency = ASSESSED
```

---

## 14. Consistency

A passed Level Assessment produces an assessment outcome that must remain consistent with the resulting learner-state information.

Conceptually:

```text
Level Assessment
    ↓
Assessment Result = PASSED
    ↓
Learning Evidence
    ↓
Competency
    ↓
Current Level
```

The system must not establish a Current Level that lacks valid supporting Level Assessment evidence.

Assessment Evidence and learner-state updates are logically connected, but they are not assumed to share a single Aggregate boundary solely because one can update the other.

The exact technical transaction mechanism is not defined by this document.

---

## 15. Relationship with User Status

Competency does not inherit the lifecycle state of the User account.

For example:

```text
User
Status = BANNED

German Competency
Status = ASSESSED
Current Level = B2
```

This is a valid conceptual state.

A User being banned affects the User's ability to access or perform activities in the system; it does not mean that the learner's demonstrated competency becomes invalid.

Therefore:

```text
User Status
    ≠
Competency Status
```

Competency does not have a `BANNED` state corresponding to the User account.

---

## 16. Certification Distinction

DeutschHub Competency represents capability assessed from evidence available within the system.

It does not represent formal external certification.

Therefore:

```text
DeutschHub Competency
    ≠
External Certification
```

For example, passing a DeutschHub Level Assessment may establish:

```text
Current Level = B2
```

within the system, but it does not constitute an official external B2 certificate.

---

## 17. Aggregate Boundary

Competency is a learner-state domain concept/entity scoped to a User and Competency Scope.

Competency is not established as an independent Aggregate Root in V3.

The current model treats Learner State as a broader business responsibility rather than a single Aggregate Root.

Conceptually:

```text
Learner State
│
├── Competency
│     └── User + Competency Scope
│
├── Current Level
│
└── Other learner-state concepts
```

Competency is therefore not placed inside:

```text
QuizAttempt
Quiz
Course
Enrollment
```

even though Level Assessment evidence can cause Competency and Current Level to be updated.

The exact Aggregate boundary for Competency remains deferred until additional business invariants or consistency requirements justify an independent Aggregate.

---

## 18. Confirmed Decisions

The following decisions are confirmed for the current V3 domain model:

* A learner may have multiple Competencies.
* A Competency is distinguished by its Competency Scope.
* A given User and Competency Scope may have at most one Competency.
* German Language is the confirmed competency scope within the current DeutschHub V3 scope.
* Competency represents demonstrated capability.
* Current Level represents the proficiency classification of that capability.
* Competency and Current Level are distinct concepts.
* Vocabulary, Grammar, Reading, Listening, Speaking, and Writing are Skill Dimensions used for assessment, not separate Competencies by default.
* Level Assessment is the authoritative mechanism for establishing Competency.
* Ordinary quizzes, exercises, practices, and other Learning Activities do not directly establish Competency or Current Level.
* Course completion is not required before taking a Level Assessment.
* A Level Assessment uses a passing threshold to determine whether the learner passes.
* A passed Level Assessment provides sufficient evidence to establish the corresponding proficiency level.
* A learner may retake the same Level Assessment.
* Any valid passed attempt is sufficient; the highest score or latest attempt is not required.
* A failed assessment does not automatically downgrade Competency or Current Level.
* Passing a lower level does not reduce an already established higher Current Level.
* Competency begins in the `UNASSESSED` state.
* `UNASSESSED` means that the system has not established a Current Level through valid assessment evidence.
* A valid passed Level Assessment transitions Competency to `ASSESSED`.
* A passed higher-level assessment may update Current Level.
* Existing assessment evidence remains historical and is not invalidated merely because an Assessment Revision changes.
* Competency does not inherit the User's `BANNED` state.
* Competency is not formal external certification.
* Competency is not established as an independent Aggregate Root in V3.

---

## 19. Deferred / Open Decisions

The following decisions are intentionally not finalized:

* The exact representation of Competency Scope.
* Whether Competency requires a technical identifier separate from its natural User + Scope identity.
* The exact persistence representation of Competency state.
* The exact relationship between Competency and Current Level at the implementation/model level.
* Any future mechanism for Competency reset or revocation.
* Any future competency decay or expiration mechanism.
* Additional Competency Scopes beyond the current German Language scope.
* The exact Aggregate boundary for Competency if future business rules require an independent Aggregate.
* The detailed rules for how individual Skill Dimension results are represented within an Assessment.
* The exact source/content strategy for Level Assessment materials.

These decisions remain deferred because they are not required to establish the current V3 business meaning and rules of Competency.

---

## 20. Implementation Status

### Confirmed from current implementation

* `src/main/java/com/deutschhub/domain/learning/model/valueobject/CEFRLevel.java` provides the CEFR level value set used by the current domain model.
* The current implementation does not contain a dedicated Competency model.
* The current implementation does not contain a dedicated learner Current Level model corresponding to the target Competency model.
* The existing `CEFRLevel` usage must not be interpreted as evidence that Course Level and Learner Current Level are the same concept.

### Target Domain Direction

The target model introduces:

```text
User
    ↓
Competency
    ↓
Current Level
```

with:

```text
Level Assessment
    ↓
Assessment Evidence
    ↓
Competency
    ↓
Current Level
```

The target model is therefore not a direct description of the existing V1 implementation. Existing code is treated as implementation evidence and reference material, while the confirmed V3 domain decisions define the target business model.
