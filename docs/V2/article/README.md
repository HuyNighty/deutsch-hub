# DeutschHub V2

## Overview

DeutschHub V2 focuses on building a production-oriented platform with Domain-Driven Design (DDD), Clean Architecture, and a scalable modular architecture.

Unlike V1, which focused on implementing core learning features, V2 emphasizes clear business boundaries, maintainability, and future extensibility.

---

## Product Vision

DeutschHub aims to become a platform that helps Vietnamese learners explore Germany, learn German, prepare for studying abroad, and connect with the community.

Current product modules:

- Learning German
- Explore Germany
- Study in Germany (planned)
- Experiences (planned)
- My Learning
- Account

---

## Current Development Scope

### Completed

- Identity Context
- Learning Context
- Media Support Context

### In Progress

- Explore Germany (Content Context)

### Planned

- Study Abroad Planning Context
- Community Experience Context

---

## Architecture Principles

DeutschHub V2 follows several design principles:

- Domain-Driven Design
- Clean Architecture
- Vertical Slice Features
- Rich Domain Model
- Business-first Design
- Infrastructure Independence

Every business decision is designed before implementation.

Business behavior
→ Business rules
→ Domain model
→ Application layer
→ Infrastructure
→ API
→ Frontend

---

## Development Process

For every major feature, DeutschHub follows the same process:

1. Discover the business problem.
2. Define business rules.
3. Build ubiquitous language.
4. Design aggregates and entities.
5. Implement application services.
6. Build infrastructure.
7. Expose APIs.
8. Integrate frontend.
9. Write automated tests.

Implementation always comes after the domain model.

---

## Current Phase

Phase 3

Content Context Discovery

Current goal:

Design a maintainable editorial content system for Explore Germany before writing any domain code.
