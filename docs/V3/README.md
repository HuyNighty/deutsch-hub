# V3 — Learning Context

1. Overview
2. Why V3?
3. Version Context
4. Scope
5. Objectives
6. V3 Approach
7. Documentation
8. Current Status

---

## 1. Overview

V3 revisits the learning-related implementation originally developed in V1.

The current implementation is organized under the `learning` module, but its domain model is primarily centered around the Course business. Course, Section, Lesson, Enrollment, Completion, and Progress are modeled around the Course as the main organizing concept.

Therefore, the existing implementation should not be assumed to represent a complete Learning Context.

V3 begins by investigating what the Learning Context of DeutschHub actually represents from the current domain perspective. The purpose is to distinguish the concepts that currently exist in the Course-centered implementation from the broader responsibilities that may belong to Learning.

The goal is not to extend the existing model blindly, nor to continue V1 assumptions. V3 will first determine the actual domain boundaries, responsibilities, relationships, and business concepts of Learning before defining its target architecture.

The exploration of DeutschTiger is used as a reference for discovering possible learning-oriented experiences and domain concepts. It is a reference for discovery, not a specification to be copied directly into DeutschHub.

---

## 2. Why V3?

The learning-related implementation was originally developed in V1, but its current structure is primarily Course-oriented.

The existing `learning` module contains concepts such as Course, Section, Lesson, Enrollment, Lesson Completion, Progress, Quiz, Quiz Attempt, and Certificate. However, the presence of these concepts does not by itself establish a coherent Learning Context or clearly defined domain boundaries.

As the project's domain and architectural understanding have matured through subsequent development, the existing learning implementation needs to be reassessed rather than extended based on its original assumptions.

V3 therefore exists to answer a fundamental question:

> What is the Learning Context of DeutschHub?

To answer this question, V3 will:

* reassess the current learning-related implementation;
* identify the actual business responsibilities represented by the existing model;
* distinguish Course-oriented responsibilities from broader Learning responsibilities;
* examine relationships and boundaries between learning-related concepts;
* evaluate the current implementation from the project's current DDD and Hexagonal Architecture perspective;
* determine what should be kept, changed, removed, or further developed.

The target Learning Context is intentionally not defined in advance. It will emerge from the discovery and domain analysis process.

---

## 3. Version Context

DeutschHub is developed through multiple versions, with each version having its own context and purpose.

| Version | Focus                                    | Role                                                                                     |
| ------- | ---------------------------------------- | ---------------------------------------------------------------------------------------- |
| V1      | Course-oriented Learning implementation  | Introduced the initial learning-related implementation centered around Course            |
| V2      | Content Context                          | Developed the Content domain as a separate context                                       |
| V3      | Learning Context discovery and evolution | Reassesses the Course-oriented implementation and determines the actual Learning Context |

V2 is not the direct continuation of Learning. It represents the Content Context.

V3 intentionally returns to the Learning work from V1 because the Learning Context needs to evolve independently and should not remain structurally behind the project's current architecture.

---

## 4. Scope

### In Scope

The scope of V3 is the **Learning Context**.

The work includes understanding and improving the Learning domain and its current implementation from the perspective of the project's current domain model and architecture.

The existing Course-oriented implementation is part of this investigation because it is the current representation of Learning in the project.

### Out of Scope

V3 does not include other contexts of DeutschHub.

In particular:

- Content Context is outside the scope of V3.
- Communication Context is outside the scope of V3.
- Other future contexts are outside the scope unless they become directly necessary to understand the Learning Context.

V3 should remain focused on Learning Context and should not become a general refactoring version for the entire backend.

---

## 5. Objectives

V3 does not begin by defining new entities, aggregates, value objects, or features.

The first objective is to understand the domain before deciding what the implementation should become.

### Understand

Understand the Learning domain and the business concepts that belong to it.

### Evaluate

Evaluate the current Learning implementation from the project's current DDD and Hexagonal Architecture perspective.

### Identify

Identify what should be:

- kept;
- changed;
- removed;
- clarified;
- further developed.

### Define

Define the target domain and architectural model based on the findings from the discovery and analysis phases.

### Improve

Improve the Learning Context according to the target model that is agreed upon during V3.

The final structure of the Learning Context is intentionally left open until the domain analysis provides enough evidence to make those decisions.

---

## 6. V3 Approach

V3 follows a discovery-first approach.

The implementation should not be refactored simply because a different design appears cleaner. Each significant change should be supported by an understanding of the domain, the current implementation, and the problem that the change is intended to solve.

The overall process is:

```text
Discovery
    ↓
Current State Analysis
    ↓
Domain Analysis
    ↓
Architecture Analysis
    ↓
Decision
    ↓
Target Model
    ↓
Refactoring
    ↓
Validation
```
### Discovery

Understand what Learning means for DeutschHub and what responsibilities currently exist in the Learning Context.

### Current State Analysis

Document how the current implementation works before deciding whether it should change.

### Domain Analysis

Examine business responsibilities, boundaries, relationships, and domain concepts.

### Architecture Analysis

Evaluate how the current implementation expresses those domain responsibilities through the project's DDD and Hexagonal Architecture structure.

### Decision

For each significant finding, determine whether it should be kept, changed, removed, or investigated further.

### Target Model

Define the target domain and architecture only after sufficient evidence has been gathered.

### Refactoring

Apply implementation changes according to the agreed target model.

### Validation

Verify that the resulting Learning Context remains consistent with its domain responsibilities and architectural boundaries.

---
## 7. Documentation

This directory acts as the documentation space for V3.

The documentation will be built incrementally. A document should only be introduced when its purpose, problem, and scope have been clearly established.

| Document	 | Purpose	                                          |Status|
|-----------|---------------------------------------------------|----|
| README.md | Entry point and overall direction of V3           |	Active|

Additional documents will be added here as the V3 discovery, analysis, decision-making, and implementation process progresses.

---
## 8. Current Status

V3 is currently in the discovery and analysis preparation stage.

| Phase                      | Status      |
| -------------------------- | ----------- |
| V3 scope definition        | Completed   |
| V3 objectives definition   | Completed   |
| V3 approach definition     | Completed   |
| Learning Context discovery | In progress |
| Current State Analysis     | Pending     |
| Domain Analysis            | Pending     |
| Architecture Analysis      | Pending     |
| Target Model               | Pending     |
| Refactoring                | Pending     |
| Validation                 | Pending     |

**The current priority is to understand the Learning Context before making implementation decisions.**

