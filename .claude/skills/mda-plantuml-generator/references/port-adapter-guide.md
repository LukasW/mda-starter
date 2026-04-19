# Port-und-Adapter-Guide (Quarkus)

## Paket-Layout

```
ch.grudligstrasse.mda
├── <bc>                               # z. B. "contact"
│   ├── domain
│   │   ├── Contact.java
│   │   ├── ContactId.java
│   │   ├── Email.java
│   │   ├── ContactStatus.java
│   │   ├── event
│   │   │   └── ContactRegistered.java
│   │   └── error
│   │       └── DomainException.java
│   ├── application
│   │   ├── port
│   │   │   ├── in
│   │   │   │   ├── RegisterContactUseCase.java
│   │   │   │   ├── DeactivateContactUseCase.java
│   │   │   │   └── GetContactByIdQuery.java
│   │   │   └── out
│   │   │       ├── ContactRepository.java
│   │   │       └── DomainEventPublisher.java
│   │   └── service
│   │       └── ContactApplicationService.java
│   ├── adapter
│   │   ├── in
│   │   │   └── rest
│   │   │       ├── ContactResource.java
│   │   │       ├── ContactDto.java
│   │   │       └── ContactRestMapper.java
│   │   └── out
│   │       └── persistence
│   │           ├── ContactJpaEntity.java
│   │           ├── ContactPanacheRepository.java
│   │           └── ContactPersistenceMapper.java
│   └── config
│       └── ContactModuleConfig.java
└── shared
    ├── problem
    │   ├── ProblemDetail.java
    │   └── DomainExceptionMapper.java
    └── events
        └── InMemoryDomainEventPublisher.java
```

## Abhängigkeitsregeln

- **`domain`**: reiner Java-Code, 0 Framework-Imports. Nur `java.*`.
- **`application.port`**: Interfaces + Records. 0 Framework-Imports.
- **`application.service`**: `@ApplicationScoped`, `@Transactional`. Darf `domain` + `application.port.*` importieren.
- **`adapter.in.rest`**: `jakarta.ws.rs.*`, DTOs/Mapper. Ruft nur `application.port.in`.
- **`adapter.out.persistence`**: `jakarta.persistence.*`, `io.quarkus.hibernate.orm.panache.*`. Implementiert nur `application.port.out`.

## CDI-Verdrahtung

Produzenten (wenn nötig) liegen in `config`. Für `DomainEventPublisher` gibt es eine In-Memory-Default-Implementierung unter `shared.events`, die per CDI den Port produziert (`@DefaultBean`, damit Tests einen Stub injizieren können).

## Transaktionen

Einzig die Anwendungsservices tragen `@Transactional`. Adapter und Domain nicht. Rollback auf `DomainException`, `ConstraintViolationException`, `PersistenceException`.

## Fehlermodell

Alle `DomainException`-Subklassen werden durch einen zentralen `ExceptionMapper` in RFC 7807 Problem+JSON (Content-Type `application/problem+json`) übersetzt. Code-Konvention: `MDA-<area>-<nr>`, z. B. `MDA-CON-001`.
