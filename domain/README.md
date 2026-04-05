# Fleet SaaS — Domain Module

The `domain` module is the core of the Fleet SaaS Notification and Rule Engine. It follows the **Hexagonal Architecture** principles, ensuring that all business logic is isolated from infrastructure, databases, and external frameworks.

## 🏛 Clean Architecture Principles
- **Zero External Dependencies**: The `build.gradle` file contains no implementation-level dependencies, only internal Java libraries. This ensures that business logic doesn't leak into technology choices (like Spring or Jackson).
- **Inversion of Control**: The domain module defines **Outbound Ports** (interfaces) that must be implemented by the `infrastructure` layer.
- **Value Objects & Aggregates**: Uses Java `record` for immutable value objects (like `TenantId`, `RuleId`) and robust aggregate models for business logic.

---

## 🛠 Core Components

### 1. Rule Engine (AST-based)
The engine evaluates incoming event payloads against flexible user-defined rules.
- **Aggregate**: `NotificationRule` (stores condition tree, active status, and cooldown).
- **Composite Pattern**:
  - `RuleNode`: Common interface for all nodes in the tree.
  - `ConditionNode`: Leaf nodes for field-level comparisons (e.g., `speed > 80`).
  - `LogicalNode`: Composite nodes for combining conditions using `AND` or `OR`.
- **Enums**:
  - `Operator`: Supported comparison types (`GT`, `LT`, `EQ`, `IN`, `NOT_IN`, etc.).
  - `LogicalOperator`: `AND`, `OR`.

### 2. Entitlement (Subscription & Access Control)
Determines if a tenant is allowed to use specific notification services.
- **Entity**: `TenantSubscription` — immutable model tracking status (`ACTIVE`, `EXPIRED`, `SUSPENDED`) and validity dates.
- **Port**: `SubscriptionRepositoryPort` for retrieving subscription data from persistence.

### 3. Notification & Anti-Spam
Handles global and rule-specific communication policies.
- **Aggregate**: `EmailSubscription` — manages user opt-in/opt-out status to comply with GDPR/anti-spam regulations.
- **Model**: `NotificationAction` — defines how an alert is dispatched (`EMAIL`, `SMS`, `WEBHOOK`) and to whom.
- **Ports**: 
  - `NotificationDispatcherPort`: Interface for sending actual messages.
  - `SubscriptionCheckPort`: Interface to verify recipient's opt-in status.

---

## 🏗 Directory Structure
```text
domain/src/main/java/com/fleet/domain/
├── entitlement/        # Service access logic
│   ├── model/          # TenantSubscription & Status
│   ├── port/out/       # Repository interfaces
│   └── vo/             # TenantId, ServiceId records
├── notification/       # Dispatch and GDPR logic
│   ├── model/          # NotificationAction, EmailSubscription
│   ├── port/out/       # Dispatcher & Subscription check ports
│   └── vo/             # EmailAddress records
└── rule/               # Rule engine logic
    ├── ast/            # AST components (ConditionNode, LogicalNode, Operators)
    ├── model/          # NotificationRule (Main Aggregate)
    ├── port/out/       # Rule storage & Cooldown interfaces
    └── vo/             # RuleId, EventPayload
```

## 🧪 Testing Policy
- **Unit Tests Only**: All tests in this module are located in `src/test` and are pure Java JUnit 5 tests.
- **No Mocking Frameworks in Domain Logic**: Business logic tests (like `ConditionNodeTest` or `NotificationRuleTest`) use direct instantiation to verify purely deterministic calculations.
- **Mocking for Ports**: Mocks are only used for testing application services correctly interacting with outbound ports.
