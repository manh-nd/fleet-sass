# Fleet SaaS — Infrastructure Module

The `infrastructure` module provides the concrete implementations of the ports defined in the `domain` and `application` modules. It handles all technological concerns, such as API endpoints, database persistence, caching, and external communication.

## 🏛 Hexagonal Architecture Principles
- **Adapters Over Implementations**: This module contains **Inbound Adapters** (REST controllers) that convert outside requests into use case calls, and **Outbound Adapters** that implement domain/application ports.
- **Technology Isolation**: The choice of framework (Spring Boot), database (PostgreSQL), and cache (Redis) is restricted to this module, ensuring that technology changes do not impact the business logic.
- **Dependency Rule**: This module depends on both `application` and `domain`. It is the "outer shell" of the system.

---

## 🛠 Tech Stack & Core Adapters

### 1. Web Layer (Inbound)
- **`RuleController`**: Exposes REST endpoints for managing and evaluating notification rules.
- **`EntitlementController`**: Exposes REST endpoints for checking tenant service permissions.

### 2. Persistence Layer (Outbound)
- **PostgreSQL**: Used for long-term storage of rules and notification actions.
  - **`PostgresRuleRepositoryAdapter`**: Implements rule CRUD operations.
  - **`PostgresActionRepositoryAdapter`**: For retrieving notification actions.
- **Redis**: Used for high-speed, transient data.
  - **`RedisCooldownAdapter`**: Tracks rule-vehicle cooldowns to prevent alert flooding.

### 3. Messaging & Events (Outbound)
- **`SpringRuleEventPublisherAdapter`**: Publishes internal application events using the Spring `ApplicationEventPublisher`.
- **`ConsoleNotificationAdapter`**: Current implementation for alert dispatching, logging messages to the system console for development and local testing.

### 4. Logic Extraction
- **`RuleAstParser`**: A critical infrastructure component that handles the complex (de)serialization between JSON (stored in PostgreSQL) and the domain's AST objects (`RuleNode`).

---

## 🏗 Directory Structure
```text
infrastructure/src/main/java/com/fleet/infrastructure/
├── adapter/
│   ├── in/web/             # Controllers and Web DTOs
│   └── out/                # Persistence, Messaging, and Event implementations
│       ├── db/             # PostgreSQL Adapters & RuleAstParser
│       ├── event/          # Internal Event Publishers
│       ├── inmemory/       # Stub adapters for local development
│       ├── messaging/      # Alert Dispatchers
│       └── redis/          # Redis-based Cooldown management
├── config/                 # Spring configuration (Beans, Redis, Database)
└── FleetApplication.java   # Main Spring Boot Entry Point
```

## 🧪 Testing Policy
- **Integration Tests**: This module contains `Integration Tests` that use **Testcontainers** (PostgreSQL/Redis) to verify that adapters interact correctly with the real infrastructure.
- **MockMvc Tests**: Controllers are tested using `MockMvc` to verify API contracts, error handling, and correct mapping to Application Use Cases.
