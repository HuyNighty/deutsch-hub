# DeutschHub

DeutschHub is a backend project for a German learning platform built with **Java Spring Boot**, following **Hexagonal Architecture** and **Domain-Driven Design (DDD)** principles.

The primary goal of this project is to practice enterprise-level backend architecture while building a scalable and maintainable application. The current implementation focuses on the **Identity** domain, with additional learning features being developed incrementally.

---

# Current Status

## Completed

* Hexagonal Architecture (Ports & Adapters)
* Domain-Driven Design (DDD) structure
* Identity domain
* JWT Authentication
* User registration
* User login
* Domain models with Value Objects
* Separation between Domain, Application, and Infrastructure layers

## In Progress

* Learning module
* Course management
* Lesson management
* Vocabulary management

## Planned

* Quiz & Assessment
* User Progress Tracking
* German Culture content
* Search
* File storage
* Notification system
* Frontend application

---

# Project Goals

This project aims to:

* Learn and apply Domain-Driven Design (DDD)
* Build a backend using Hexagonal Architecture
* Develop scalable and maintainable software
* Practice clean code and business-oriented design
* Serve as a long-term portfolio project

---

# Core Concepts

The project is built around the following architectural principles:

* Domain-Driven Design (DDD)
* Hexagonal Architecture (Ports & Adapters)
* Rich Domain Models
* Value Objects
* Clean Architecture boundaries
* Business-first design
* Modular Monolith architecture

---

# Tech Stack

## Backend

| Technology         | Description                    |
| ------------------ | ------------------------------ |
| Java 21            | Main programming language      |
| Spring Boot 3.4    | Backend framework              |
| Spring Security    | Authentication & Authorization |
| JWT                | Stateless authentication       |
| Spring Data JPA    | ORM abstraction                |
| Hibernate          | JPA implementation             |
| MySQL              | Primary relational database    |
| MapStruct          | DTO mapping                    |
| Lombok             | Boilerplate reduction          |
| Jakarta Validation | Request validation             |
| Jackson            | JSON serialization             |
| Maven              | Build tool                     |

---

## Frontend (Planned)

| Technology     | Description          |
| -------------- | -------------------- |
| React + Vite   | Frontend framework   |
| TypeScript     | Type-safe frontend   |
| Tailwind CSS   | UI styling           |
| Shadcn/UI      | UI components        |
| TanStack Query | API state management |

---

## DevOps & Tools (Planned)

| Tool           | Purpose                       |
| -------------- | ----------------------------- |
| Docker         | Containerization              |
| Docker Compose | Local development environment |
| JUnit 5        | Unit testing                  |
| JaCoCo         | Code coverage                 |
| Spotless       | Code formatting               |

---

# Architecture

DeutschHub follows **Hexagonal Architecture** combined with **Domain-Driven Design (DDD)**.

The application is organized around business domains instead of technical layers, allowing the core business logic to remain independent from frameworks and infrastructure.

---

# Project Structure

```text
src/main/java/com/deutschhub
│
├── common/                        # Shared utilities and cross-cutting concerns
│   ├── exception/
│   ├── util/
│   └── annotation/
│
├── domain/                        # Pure business logic (independent of Spring)
│   ├── identity/
│   ├── learning/
│   ├── content/
│   └── shared/
│
├── application/                   # Use cases and application services
│   ├── identity/
│   ├── learning/
│   └── content/
│
├── infrastructure/                # Frameworks and technical implementations
│   ├── identity/
│   ├── learning/
│   ├── web/
│   ├── config/
│   └── external/
│
└── resources/
    ├── i18n/
    └── application.yml
```

---

# Roadmap

* [x] Project architecture
* [x] Identity domain
* [x] JWT authentication
* [x] User registration & login
* [ ] Learning module
* [ ] Course & lesson management
* [ ] Vocabulary module
* [ ] Quiz & assessment
* [ ] Progress tracking
* [ ] German culture content
* [ ] Docker support
* [ ] Unit & integration testing
* [ ] CI/CD pipeline
* [ ] Frontend application
