# Fleet SaaS — Application Module

The `application` module acts as the orchestration layer for the Fleet SaaS system. It implements the **Use Cases** (Inbound Ports) and defines the interaction between the `domain` logic and the external `infrastructure`.

## 🏛 Hexagonal Architecture Principles
- **Use Case Orchestration**: All business processes are coordinate here. This module interacts with the `domain` aggregates and calls `domain` ports for external effects.
- **Dependency Rule**: This module depends **only** on the `domain` module. It must remain free of infrastructure details like HTTP configurations, databases, or third-party message brokers.
- **Inbound Ports Implementation**: Services here implement the `Port In` interfaces that the `infrastructure` (e.g., Controllers, Listeners) invokes.

---

## 🛠 Core Services

### 1. Rule Evaluation (`EvaluateRulesService`)
- **Use Case**: `EvaluateRulesUseCase`
- **Responsibility**: Orchestrates the evaluation of active rules for a given event, checking for **cooldowns** in Redis and triggering events if any conditions match.

### 2. Rule Management (`ManageNotificationRuleService`)
- **Use Case**: `ManageNotificationRuleUseCase`
- **Responsibility**: Handles CRUD operations for notification rules, converting user input into domain models and interacting with the `RuleRepositoryPort`.

### 3. Entitlement Checking (`CheckEntitlementService`)
- **Use Case**: `CheckEntitlementUseCase`
- **Responsibility**: Simple check to verify a tenant's permissions for a service, consulting the `SubscriptionRepositoryPort`.

### 4. Alert Dispatching (`DispatchAlertService`)
- **Use Case**: `DispatchAlertUseCase`
- **Responsibility**: Manages the multi-channel dispatch of alerts (Email, SMS, Webhook), ensuring **GDPR compliance** by checking the user's subscription status through the `SubscriptionCheckPort`.

---

## 🏗 Directory Structure
```text
application/src/main/java/com/fleet/application/
├── entitlement/        # Entitlement check implementation
│   └── usecase/        # CheckEntitlementUseCase interface
├── notification/       # Alert dispatching orchestration
│   └── usecase/        # DispatchAlertUseCase interface
├── rule/               # Rule evaluation and management
│   ├── port/out/       # Rule specific application outbound ports
│   └── usecase/        # EvaluateRulesUseCase, ManageNotificationRuleUseCase
└── shared/             # Cross-cutting application types (e.g., Events)
```

## 🧪 Testing Policy
- **Service Layer Tests**: Services are tested using Mockito to mock outbound ports (Repositories, Dispatchers).
- **Behavior Driven**: Focused on verifying that the business orchestration (workflow) is correct, while detailed business logic remains in the `domain` unit tests.
