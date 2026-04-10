# DeutschHub

A full-stack German learning and culture exploration platform built with **Hexagonal Architecture** + **Domain-Driven Design (DDD)**.

Combines interactive German language courses (A1–B2) with rich cultural content about Germany, featuring progress tracking, quizzes, recommendations, and a clean, maintainable codebase.

---
## Tech Stack

### Backend
- **Language**: Java 21
- **Framework**: Spring Boot 3.4
- **Architecture**: Hexagonal Architecture (Ports & Adapters) + Domain-Driven Design (DDD)
- **Database**: MySQL (primary), MongoDB (planned for content/search)
- **ORM**: Spring Data JPA + Hibernate
- **Mapping**: MapStruct
- **Validation**: Jakarta Validation
- **Security**: Spring Security + JWT (OAuth2 Resource Server)
- **Utilities**: Lombok, Jackson (Java Time support)

### Frontend (Planned)
- React.js + Vite
- TypeScript
- Tailwind CSS + Shadcn/UI
- TanStack Query (for API state management)

### DevOps & Tools
- **Build Tool**: Maven
- **Containerization**: Docker + Docker Compose
- **Testing**: JUnit 5, Spring Boot Test, JaCoCo (planned)
- **Code Quality**: Spotless (code formatter)

---
## Architecture

This project follows **Hexagonal Architecture (Ports & Adapters)** combined with **Domain-Driven Design (DDD)** principles.

### Project Structure
Hexagonal Architecture v1
```bash
deutsch-hub/
deutsch-hub/
├── src/main/java/com/deutschhub/
│
├── common/                          ← Utilities 
│   ├── exception/
│   ├── util/
│   └── annotation/
│
├── domain/                          ← Core Domain (Pure Java)
│   ├── learning/                    ← Bounded Context: Learning (Core)
│   │   ├── model/
│   │   │   ├── aggregate/           ← Course (Aggregate Root)
│   │   │   ├── entity/              ← Lesson, Quiz...
│   │   │   ├── valueobject/         ← CEFRLevel, Progress...
│   │   │   └── exception/
│   │   │
│   │   └── service/                 ← Domain Services (business rules thuần)
│   │
│   ├── content/                     ← Bounded Context: Content
│   └── shared/                      ← Shared Value Objects & Domain Events
│
├── application/                     ← Application Layer
│   ├── learning/
│   │   ├── port/
│   │   │   ├── in/                  ← Input Ports (UseCase interfaces)
│   │   │   └── out/                 ← Output Ports (RepositoryPort, ...)
│   │   ├── usecase/                 ← Implementation của Use Cases
│   │   └── dto/                     ← Request / Response cho Learning
│   │
│   └── content/                     ← Content Context
│
├── infrastructure/                  ← Adapters
│   ├── persistence/
│   │   ├── learning/                ← Separate according to the context
│   │   │   ├── jpa/
│   │   │   └── adapter/
│   │   └── content/
│   │
│   ├── web/
│   │   ├── controller/
│   │   │   └── learning/
│   │   └── dto/
│   │
│   ├── config/                      ← BeanConfig, SecurityConfig...
│   └── external/                    ← TTS, Email, Payment...
│
└── resources/
    ├── i18n/
    └── application.yml
```
1. **domain/** → The heart of the application (Pure Java, no Spring dependencies)
   - Contains Entities, Aggregates, Value Objects, Domain Services, and Domain Exceptions.
   - This layer represents the Ubiquitous Language and core business rules.

2. **application/** → Use Cases and Ports Layer
   - Input Ports: Interfaces defining what the outside world can call (Use Cases).
   - Output Ports: Interfaces defining what the domain needs from the outside (e.g., ...).
   - UseCase Implementations: Actual business logic orchestration.

3. **infrastructure/** → Adapters Layer (Spring-specific code)
   - persistence/: JPA entities and Repository Adapters (implementation of Output Ports).
   - web/: REST Controllers (Primary/Driving Adapters).
   - config/: Configuration classes (BeanConfig, SecurityConfig, etc.).
   - external/: Adapters for third-party services (TTS, Email, Payment, etc.).

   4. **common/** → Shared utilities and cross-cutting concerns for the entire project