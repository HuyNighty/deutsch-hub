# Assessment Domain Model

## 1. Purpose

Assessment represents a business concept used to evaluate a learner within a defined assessment purpose.

Assessment is broader than Quiz. A Quiz may be used as one of the mechanisms or tasks within an Assessment, but an Assessment is not itself a Quiz.

An Assessment may evaluate multiple skill dimensions through a set of Assessment Components, where each Component may contain multiple Assessment Tasks.

For Level Assessment, the resulting assessment outcome may establish or update the learner's Competency and Current Level.

---

## 2. Business Meaning

An Assessment represents a structured evaluation designed for a specific purpose.

The purpose of an Assessment determines:

- which Components are included;
- which Skill Dimensions are evaluated;
- which Tasks are performed;
- how completion is determined;
- how results are evaluated;
- what type of Outcome is produced.

Assessment is therefore a general domain concept and is not restricted to pass/fail examinations.

Examples of possible Assessment purposes include:

- Level Assessment;
- Diagnostic Assessment;
- Placement Assessment;
- other assessment purposes defined by the system.

The exact set of Assessment types is not fixed at this stage.

---

## 3. Assessment Structure

An Assessment may contain multiple Assessment Components.

The set of Components is not fixed. An Assessment does not have to evaluate every available Skill Dimension.

For example:

```text
Assessment
├── Reading Component
├── Listening Component
└── Grammar Component
````

Another Assessment may contain:

```text
Assessment
├── Reading Component
├── Listening Component
├── Writing Component
└── Speaking Component
```

The Components included in an Assessment depend on its purpose.

---

## 4. Assessment Component

An Assessment Component represents a distinct part of an Assessment that evaluates one specific Skill Dimension.

Each Assessment Component is associated with exactly one Skill Dimension.

Examples of Skill Dimensions currently identified for DeutschHub include:

* Grammar;
* Vocabulary;
* Reading;
* Listening;
* Speaking;
* Writing.

These dimensions are not separate Competencies by default.

The current Skill Dimension set is not considered exhaustive.

### 4.1 Component Boundary

A Component provides a business boundary around the evaluation of one Skill Dimension within a specific Assessment.

For example:

```text
Reading Component
├── Reading Task 1
├── Reading Task 2
└── Reading Task 3
```

A Component must not represent multiple Skill Dimensions simultaneously.

If an Assessment needs to evaluate both Reading and Listening, these are represented by separate Components.

---

## 5. Assessment Task

An Assessment Component may contain multiple Assessment Tasks.

An Assessment Task represents a specific activity or task that the learner performs as part of the evaluation of the Component's Skill Dimension.

For example:

```text
Reading Component
├── Task 1
├── Task 2
└── Task 3
```

Different Tasks may use different evaluation mechanisms depending on their nature.

The specific evaluation mechanisms are intentionally not fixed at this stage.

Examples such as automatic evaluation, manual evaluation, rubric-based evaluation, or other mechanisms are possible interpretations, but are not current domain decisions.

### 5.1 Assessment Task and Learning Activity

Assessment Task is not currently equated with the broader Learning Activity concept.

The two concepts may have similarities, but their business meanings are not considered identical at this stage.

---

## 6. Evaluation Mechanism

An Assessment Task may use an evaluation mechanism appropriate to the nature of that Task.

Different Tasks may therefore be evaluated in different ways.

The exact evaluation mechanisms are currently OPEN.

The domain does not currently require a single evaluation mechanism shared by all Assessment Tasks.

The following are intentionally not fixed:

* automatic versus manual evaluation;
* scoring mechanism;
* rubric structure;
* evaluation algorithm;
* detailed evaluation rules for Writing or Speaking;
* other task-specific evaluation mechanisms.

---

## 7. Component Result

Each Assessment Component produces a Component Result when that Component has been evaluated.

Component Result represents the result of evaluating a specific Component within a specific Assessment Attempt.

For example:

```text
Assessment Attempt
├── Reading Component
│   └── Reading Component Result
├── Listening Component
│   └── Listening Component Result
└── Speaking Component
    └── Speaking Component Result
```

Component Result is historical data associated with the corresponding Assessment Attempt.

The exact representation of a Component Result is not fixed at this stage.

A Component Result is not a Competency and does not independently establish the learner's Current Level.

---

## 8. Assessment Completion Policy

Each Assessment has its own Completion Policy or Completion Rule.

The Completion Policy determines when an Assessment Attempt satisfies the conditions required for the Assessment to be considered complete and for an official Assessment Result to be produced.

Completion requirements may differ between Assessments because different Assessments may serve different purposes.

The system does not currently require one global completion policy for all Assessments.

### 8.1 Unanswered Components

A learner may submit an Assessment even when one or more Components have not been answered if the Assessment's Completion Policy allows the submission.

Submitting the Assessment represents the learner's decision to finish the Attempt without completing those Components.

An unanswered Component is therefore not automatically treated as an invalid Assessment Attempt.

The exact result assigned to an unanswered Component depends on the Assessment's evaluation rules and remains OPEN.

---

## 9. Assessment Time Limit

Assessment has a Time Limit.

The Time Limit belongs to the Assessment definition used by an Assessment Attempt.

When an Assessment Attempt starts, its expiration is determined from the start time and the applicable Assessment Time Limit.

Conceptually:

```text
expiresAt = startedAt + timeLimit
```

The exact technical representation is not specified by this document.

An Assessment Attempt must retain the Time Limit associated with the Assessment definition used when the Attempt started.

Later changes to the Assessment do not change the Time Limit or definition of an already started Attempt.

---

## 10. Assessment Attempt

An Assessment Attempt represents one execution of an Assessment by a specific User.

Each time a User chooses to take an Assessment as a new execution, a new Assessment Attempt is created.

For example:

```text
Assessment
├── Assessment Attempt #1
├── Assessment Attempt #2
└── Assessment Attempt #3
```

Each Attempt has its own execution state and historical result.

### 10.1 Multiple Devices and Sessions

Using multiple devices or sessions does not by itself create multiple Assessment Attempts.

If the same User accesses the same active Assessment Attempt from multiple devices, the business meaning remains one Assessment Attempt.

Conceptually:

```text
User
└── Assessment Attempt #1
    ├── Device A
    └── Device B
```

The handling of concurrent technical sessions is an implementation concern and is not defined by this domain model.

### 10.2 Active Attempt

For a given User and Assessment, at most one Assessment Attempt may be active at a time.

Starting a new Attempt requires the existing active Attempt to have ended.

---

## 11. Assessment Attempt Lifecycle

The basic Assessment Attempt lifecycle is:

```text
IN_PROGRESS
     │
     ├── User Submit ───────► COMPLETED
     │
     └── Time Expires ──────► COMPLETED
```

### 11.1 Resuming an Attempt

An `IN_PROGRESS` Assessment Attempt can be resumed.

Closing a browser, leaving the current session, losing network connectivity, or changing devices does not automatically create a new Attempt or complete the current Attempt.

The same Attempt remains available for continuation while it remains valid.

### 11.2 Submit

When the User submits an Assessment Attempt, the Attempt is completed according to the Assessment's Completion Policy.

After completion:

* the Attempt is no longer editable;
* Component Results are historical;
* the Assessment Result is historical;
* the completed Attempt cannot be continued.

### 11.3 Timeout

When an Assessment Attempt reaches its expiration time before the User submits it, the system automatically ends the Attempt and produces an Assessment Result according to the Assessment's Completion and Evaluation rules.

Timeout itself does not determine whether an individual Component is passed or failed.

Uncompleted Components are processed according to the applicable Assessment rules.

---

## 12. Assessment Definition and Revision

An Assessment may have different definitions or versions over time.

An Assessment Attempt must be associated with one specific Assessment definition from the moment the Attempt starts until it ends.

Changes made to the Assessment after an Attempt has started do not change the definition used by that Attempt.

Conceptually:

```text
Assessment Revision 1
        │
        ▼
Assessment Attempt #1
        │
        └── remains based on Revision 1

Assessment Revision 2
        │
        ▼
New Assessment Attempt
```

This ensures that the historical Assessment Result can always be understood in the context in which the Assessment was actually performed.

The exact Assessment Revision model and lifecycle remain OPEN.

---

## 13. Assessment Result

Assessment Result represents the official result of one completed Assessment Attempt.

An official Assessment Result is produced when the Assessment Attempt satisfies the conditions required by the Assessment's Completion Policy.

Conceptually:

```text
Assessment Attempt
        │
        ├── Component Results
        │
        ▼
Assessment Result
```

Assessment Result is historical.

Once produced, an Assessment Result does not change because of:

* later Assessment Attempts;
* later Assessment Results;
* changes to the Assessment definition;
* changes to the learner's Current Level.

### 13.1 Assessment Outcome

Assessment does not have one universal Outcome model.

The Outcome depends on the purpose of the Assessment.

For example:

```text
Level Assessment
    → PASSED / FAILED

Diagnostic Assessment
    → Diagnostic Outcome / Profile

Placement Assessment
    → Recommended Level
```

These examples illustrate the business direction and do not define a complete universal Outcome enumeration.

A global `PASSED / FAILED` model is therefore not imposed on all Assessments.

---

## 14. Component Results and Assessment Result

Component Results provide detailed results for the individual Components.

Assessment Result provides the overall result of the Assessment Attempt.

Conceptually:

```text
Component Results
        │
        ▼
Assessment Result
```

The exact rules for aggregating Component Results into an Assessment Result are not fixed at this stage.

The following remain OPEN:

* whether Component Results use scores;
* whether scores are percentages;
* whether Components have different weights;
* minimum Component requirements;
* aggregation formulas;
* other Assessment-specific evaluation rules.

---

## 15. Level Assessment

Level Assessment is an Assessment whose purpose is to establish a learner's proficiency level for a relevant Competency.

A Level Assessment may contain multiple Components covering different Skill Dimensions.

For example:

```text
B2 Level Assessment
├── Grammar
├── Vocabulary
├── Reading
├── Listening
├── Writing
└── Speaking
```

A Level Assessment does not require the learner to complete a Course before taking the Assessment.

Course completion is not a prerequisite for taking a Level Assessment.

### 15.1 Established Level

For a Level Assessment, the Assessment Result records the level established by that particular Assessment.

For example:

```text
Assessment Result
├── Outcome = PASSED
├── Established Level = B2
└── Component Results
```

Established Level is historical information belonging to that Assessment Result.

It is distinct from the learner's current `Competency.Current Level`.

---

## 16. Relationship with Competency

Only a valid `PASSED` Assessment Result from a Level Assessment is authoritative for establishing or updating the learner's Competency and Current Level.

Conceptually:

```text
Level Assessment
        │
        ▼
Assessment Attempt
        │
        ▼
Assessment Result
        │
        ├── FAILED
        │     └── does not reduce Current Level
        │
        └── PASSED
              │
              ▼
        Established Level
              │
              ▼
          Competency
              │
              ▼
        Current Level
```

Assessment Result is historical evidence.

Competency represents the learner's demonstrated capability, while Current Level represents the current CEFR classification assigned to that Competency.

Assessment Result and Current Level are therefore distinct concepts.

---

## 17. Level Assessment and Current Level Rules

The following rules apply to the relationship between a passed Level Assessment and Current Level:

1. A learner may take a Level Assessment without completing a Course first.
2. A learner may establish a Current Level directly from a valid passed Level Assessment.
3. A Level Assessment may establish any applicable CEFR level; the learner does not need to progress through CEFR levels sequentially.
4. Passing a higher level may increase the learner's Current Level directly.
5. Passing the same level does not change the Current Level.
6. Passing a lower level does not reduce an already established higher Current Level.
7. Failing a Level Assessment does not reduce an already established Current Level.
8. A later Assessment Result does not modify historical Assessment Results.

For example:

```text
UNKNOWN → pass B2 → B2

B2 → pass B2 → B2

B2 → pass C1 → C1

B2 → pass A2 → B2

B2 → fail C1 → B2
```

---

## 18. Assessment Attempt Limit

An Assessment may optionally define a limit on the number of Attempts a User may make.

Attempt Limit is therefore OPTIONAL.

An Assessment may have:

```text
Attempt Limit = N
```

or:

```text
Attempt Limit = Unlimited
```

The exact policies governing:

* the number of allowed Attempts;
* cooldown periods;
* paid retakes;
* time-based restrictions;
* other retake policies

are not fixed at this stage.

Assessment Attempt Limit is independent from the `maxAttempts` rule of Quiz.

---

## 19. Historical Integrity

Assessment execution and results are historical.

The following relationships must remain understandable after the Assessment definition changes:

```text
Assessment Definition
        │
        ▼
Assessment Attempt
        │
        ├── Component Results
        │
        └── Assessment Result
```

A later Assessment definition does not rewrite a completed historical Attempt or its Result.

A learner's later Competency or Current Level changes also do not rewrite historical Assessment Results.

---

## 20. Domain Distinctions

The following distinctions are important:

### Assessment vs Quiz

```text
Assessment
    └── may use Quiz-based Tasks
```

Assessment is broader than Quiz.

Quiz is not the universal model for all Assessment Tasks.

### Assessment Component vs Competency

An Assessment Component evaluates one Skill Dimension within an Assessment.

It is not itself a Competency.

### Component Result vs Competency

A Component Result is historical evidence produced by one Assessment Attempt.

It does not independently establish Competency or Current Level.

### Assessment Result vs Current Level

Assessment Result is historical.

Current Level is the current state/value of a learner's Competency.

### Assessment Attempt vs Assessment

Assessment is the assessment definition/concept.

Assessment Attempt represents one execution of that Assessment by a User.

---

## 21. Confirmed Decisions

The following decisions are confirmed for the current V3 domain model:

1. Assessment is an independent business concept from Quiz.
2. An Assessment may contain multiple Assessment Components.
3. An Assessment does not have to include every Skill Dimension.
4. Each Assessment Component represents exactly one Skill Dimension.
5. An Assessment Component may contain multiple Assessment Tasks.
6. Assessment Tasks may use different evaluation mechanisms.
7. The specific Evaluation Mechanism remains OPEN.
8. Each evaluated Component produces a Component Result.
9. Each Assessment has its own Completion Policy.
10. An Assessment has a Time Limit.
11. Each User has at most one active Assessment Attempt for a given Assessment.
12. Multiple devices or sessions do not inherently create multiple Attempts.
13. An `IN_PROGRESS` Attempt can be resumed.
14. Submit completes the Attempt.
15. A completed Attempt is immutable.
16. Timeout automatically ends an Attempt and produces an Assessment Result according to the applicable rules.
17. An Attempt is bound to a specific Assessment definition/version from start to completion.
18. Assessment Result is historical and immutable.
19. Assessment Outcome depends on Assessment purpose.
20. Level Assessment records an Established Level in its Assessment Result.
21. Only a valid passed Level Assessment may establish or update Competency.Current Level.
22. Level Assessment does not require prior Course completion.
23. Level Assessment may establish a level non-sequentially.
24. Attempt Limit is OPTIONAL.

---

## 22. Open / Deferred Decisions

The following decisions remain OPEN or DEFERRED:

* Exact Assessment type taxonomy.
* Exact Assessment Revision model and lifecycle.
* Exact Evaluation Mechanisms.
* Exact Assessment Task model.
* Exact Component Result representation.
* Exact Assessment Result structure.
* Exact rules for aggregating Component Results.
* Exact handling/value of unanswered Tasks or Components.
* Detailed timeout evaluation rules.
* Exact Time Limit representation.
* Attempt Limit values and policies.
* Cooldown and paid-retake policies.
* Detailed evaluation rules for Speaking, Writing, and other non-Quiz Tasks.
* Exact Skill Dimension representation.
* Exact relationship between Assessment and Learning Activity at the implementation level.
* Technical persistence representation.
* Aggregate boundary for Assessment and Assessment Attempt.

These items should only be closed when a concrete business requirement or implementation constraint requires the decision.

---

## 23. Implementation Status

Assessment is a confirmed target domain concept for the DeutschHub V3 Learning domain.

The business model is sufficiently defined to establish the Assessment domain concept and its major relationships with:

* Assessment Component;
* Assessment Task;
* Component Result;
* Assessment Attempt;
* Assessment Result;
* Level Assessment;
* Competency;
* Current Level.

Detailed implementation structure remains intentionally deferred until the remaining OPEN decisions become implementation-relevant.
