# Hexagonale Architektur (Port & Adapter) — Pflicht-Layout

## Paket-Layout je Bounded Context

```
<root-pkg>
├── <bc>                               # z. B. "contract"
│   ├── domain/
│   │   ├── Aggregate.java             # Aggregate Root
│   │   ├── AggregateId.java           # record XxxId
│   │   ├── <ValueObject>.java         # record mit Compact-Constructor-Validierung
│   │   ├── event/
│   │   │   └── XxxDomainEvent.java    # sealed interface + permits
│   │   ├── error/
│   │   │   └── DomainException.java
│   │   └── process/                   # optional: BPF-Definition
│   │       └── XxxLifecycle.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                    # UseCase-Interfaces + Command/Query-Records
│   │   │   └── out/                   # Repository- + EventPublisher-Ports
│   │   └── service/
│   │       └── XxxApplicationService.java   # @ApplicationScoped, @Transactional
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── rest/                  # JAX-RS-Resource + DTO + Mapper
│   │   │   └── scheduler/             # optional: @Scheduled-Jobs
│   │   └── out/
│   │       └── persistence/           # Panache-Entity, Repository-Impl, Mapper
│   └── config/                        # CDI-Producer, Properties, Module-Wiring
└── shared/
    ├── problem/                       # ProblemDetail + ExceptionMapper
    ├── events/                        # InMemoryDomainEventPublisher (Default)
    └── process/                       # BpfService, BpfInstance, TransitionLog
```

## Abhaengigkeitsregeln (ArchUnit-erzwungen)

| Schicht | Erlaubte Imports | Verbotene Imports |
|---|---|---|
| `domain/**` | `java.*` | alles Framework (`jakarta.*`, `io.quarkus.*`, Logger-Libs) |
| `application/port/**` | `java.*`, `<bc>/domain/**` | alles Framework |
| `application/service/**` | `jakarta.transaction.Transactional`, `jakarta.enterprise.context.ApplicationScoped`, Ports, Domain | Persistenz-Details, REST |
| `adapter/in/rest/**` | `jakarta.ws.rs.*`, `jakarta.validation.*`, Ports (nur `port.in`) | `port.out`, andere BCs direkt, Persistenz |
| `adapter/out/persistence/**` | `jakarta.persistence.*`, `io.quarkus.hibernate.orm.panache.*`, `port.out` | REST-Annotationen |

## Weitere Regeln

- **Transaktionen**: nur Application-Services tragen `@Transactional`. Rollback bei `DomainException`, `ConstraintViolationException`, `PersistenceException`.
- **Cross-BC-Kopplung**: **nur** ueber IDs oder Events. Niemals via Service-Calls oder direkter Adapter.
- **Fehlermodell**: `DomainException`-Subklassen werden zentral in RFC 7807 `application/problem+json` uebersetzt. Code-Konvention `MDA-<AREA>-<NR>`, z. B. `MDA-CON-001`, `MDA-BPF-001`.
- **CDI-Verdrahtung**: Producer in `config/`. Events ueber `InMemoryDomainEventPublisher` (Default; `@DefaultBean`, damit Tests stubben koennen).
- **Idempotenz-Marker**: `// mda-generator: manual-edits-below` in generierten Klassen — darunter wird NIE ueberschrieben.

## DDD-Konventionen

- **Aggregate Root** = Klasse, die die Invariante haelt. Oeffentliche Methoden = Command-Namen der UseCases.
- **Typisierte IDs** als `record XxxId(UUID value) {}` mit Compact-Constructor-Validierung.
- **Value Objects** als `record` mit Compact-Constructor (`Objects.requireNonNull`, Bereichsprueng, Regex).
- **Domain Events** als `sealed interface XxxDomainEvent permits A, B, C` + konkrete Records.
- **Domain Services** nur fuer Regeln, die nicht zu einem einzelnen Aggregat gehoeren.

## REST-Adapter

- **URL**: `/api/v1/<plural-entity>` in der Fachsprache.
- **Aktionen** als Sub-Resources: `POST .../{id}/<aktion>`.
- **Request-Records** tragen Bean-Validation-Annotationen (`@NotBlank`, `@Size`, ...).
- **Response-DTO** nur, wenn UI-Shape von Domain-Shape abweicht — sonst direkt mapped.
- **Exception-Mapping**: `DomainExceptionMapper` in `shared/problem/` als `@Provider`.
