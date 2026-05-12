# DeutschHub 🇩🇪

DeutschHub is a full-stack German learning and cultural exploration platform built with **Java Spring Boot**, following **Hexagonal Architecture** and **Domain-Driven Design (DDD)** principles.

The project focuses on building a clean, scalable, and maintainable backend architecture while delivering an interactive learning experience for German learners from A1 → B2.

---

# Vision

DeutschHub is not just a CRUD learning platform.

The goal is to build a real-world enterprise-style system that combines:

- German language learning
- Cultural exploration
- User progress tracking
- Quiz and assessment systems
- Personalized learning experiences
- Modular and maintainable architecture

---

# Core Concepts

This project is heavily focused on:

- Domain-Driven Design (DDD)
- Hexagonal Architecture (Ports & Adapters)
- Rich Domain Models
- Clean Architecture Boundaries
- Business-first design
- Modular Monolith architecture

---

# Tech Stack

## Backend

| Technology | Description |
|---|---|
| Java 21 | Main programming language |
| Spring Boot 3.4 | Backend framework |
| Spring Security | Authentication & Authorization |
| JWT | Stateless authentication |
| Spring Data JPA | ORM abstraction |
| Hibernate | JPA implementation |
| MySQL | Primary relational database |
| MapStruct | DTO mapping |
| Lombok | Boilerplate reduction |
| Jakarta Validation | Request validation |
| Jackson | JSON serialization |
| Maven | Build tool |

---

## Frontend (Planned)

| Technology | Description |
|---|---|
| React + Vite | Frontend framework |
| TypeScript | Type-safe frontend |
| TailwindCSS | UI styling |
| Shadcn/UI | UI components |
| TanStack Query | API state management |

---

## DevOps & Tools

| Tool | Purpose |
|---|---|
| Docker | Containerization |
| Docker Compose | Local environment |
| JUnit 5 | Unit testing |
| JaCoCo | Code coverage |
| Spotless | Code formatting |

---

# Architecture

DeutschHub follows:

# Hexagonal Architecture + Domain-Driven Design (DDD)

The project is organized around business domains instead of technical layers.

---

# Project Structure

```bash
src/main/java/com/deutschhub
│
├── common/                        # Shared utilities and cross-cutting concerns
│   ├── exception/
│   ├── util/
│   └── annotation/
│
├── domain/                        # Pure business logic (NO Spring)
│   ├── identity/
│   ├── learning/
│   ├── content/
│   └── shared/
│
├── application/                   # Use cases and orchestration layer
│   ├── identity/
│   ├── learning/
│   └── content/
│
├── infrastructure/                # Technical implementation layer
│   ├── identity/
│   ├── learning/
│   ├── web/
│   ├── config/
│   └── external/
│
└── resources/
    ├── i18n/
    └── application.yml