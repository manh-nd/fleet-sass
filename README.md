# Fleet SaaS — Notification & Rule Engine

A robust, enterprise-grade notification engine and rule processing system built using **Hexagonal Architecture** (Ports and Adapters). This engine evaluates real-time event payloads against flexible user-defined rules and dispatches alerts across multiple channels.

---

## 🏗 Modular Architecture

The project is structured into three strictly isolated layers to ensure high maintainability, testability, and technology independence:

### ⚙️ [Domain Module](./domain/README.md)
**The business core.**
- Contains Entities, Value Objects, and the Rule Engine AST (`RuleNode`, `ConditionNode`).
- **Policy**: Zero external dependencies (Pure Java). No infrastructure/framework leak.
- **Role**: Defines business logic and outbound ports (Repository/Dispatcher interfaces).

### 🛠 [Application Module](./application/README.md)
**The orchestration layer.**
- Implements Use Cases (Inbound Ports) and handles the flow between domain logic and ports.
- **Policy**: Depends only on the Domain module.
- **Role**: Manages complex workflows (e.g., Evaluating rules → checking cooldowns → triggering alerts).

### 🏛 [Infrastructure Module](./infrastructure/README.md)
**The technology implementations.**
- Contains REST controllers, database adapters (Postgres), cache adapters (Redis), and event publishers.
- **Policy**: Depends on Application and Domain modules.
- **Role**: Bridges the system to the outside world (Spring Boot, Spring Data JDBC, Redis).

---

## 🚀 Key Features

- **AST Rule Engine**: Build complex notification rules with nested `AND/OR` logic using 8+ comparison operators (`GT`, `LT`, `EQ`, `IN`, `NOT_IN`, etc.).
- **Cooldown Management**: Prevents alert fatigue using Redis-based TTL cooldown counters per vehicle and rule.
- **Multi-Channel Dispatch**: Scalable alerting via Email, SMS, and Webhooks.
- **GDPR Compliance**: Integrated opt-in/opt-out subscription checks for all recipients.
- **SaaS First**: Multi-tenant design leveraging `TenantId` and `ServiceId` for isolation.

---

## 🛠 Tech Stack

- **Java 25**: Leveraging modern Java features and records.
- **Spring Boot 4.0.5**: Core framework and autoconfiguration.
- **PostgreSQL**: Robust persistence for rules and actions.
- **Redis**: Low-latency cooldown tracking and lock management.
- **Lombok**: Reduced boilerplate for data models.
- **Testcontainers**: Comprehensive integration testing with real Postgres and Redis instances.
- **Jacoco**: Automated test coverage reporting.

---

## 🏗 Getting Started

### Prerequisites
- **Java 25 SDK**
- **Docker Desktop** (Required for running integration tests via Testcontainers)
- **Gradle** (Wrapper included)

### Build and Test
To clean, compile, and run all tests (Unit + Integration):
```bash
./gradlew clean build
```

### Run Locally
The infrastructure module contains the main Spring Boot application:
```bash
./gradlew :infrastructure:bootRun
```

---

## 🧪 Testing Policy
- **Domain**: Pure unit tests ensuring logic correctness.
- **Application**: Service-level behavioral tests using Mocks for outbound ports.
- **Infrastructure**: Full-stack integration tests using **Testcontainers** for PostgreSQL and Redis to ensure reliable database/cache interactions.

---

## 📂 Project Structure
```text
fleet-sass/
├── application/       # Orchestration & Use Case logic
├── domain/            # Core business logic & AST
└── infrastructure/    # Frameworks, DBs, and CI/CD config
```
