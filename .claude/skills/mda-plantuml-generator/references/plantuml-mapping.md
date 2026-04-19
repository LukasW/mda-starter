# PlantUML → DDD/Hexagonal Mapping

## Klassen-Stereotypen

| Stereotyp | Ziel | Paket |
|---|---|---|
| `<<aggregate>>` / `<<aggregate root>>` | Aggregate Root | `domain` |
| `<<entity>>` | Entity (intra-Aggregate) | `domain` |
| `<<value object>>` / `<<vo>>` | Value Object | `domain` |
| `<<domain event>>` / `<<event>>` | Domain Event | `domain.event` |
| `<<service>>` | Domain Service | `domain.service` |
| `<<repository>>` | Out-Port | `application.port.out` |
| `<<use case>>` | In-Port | `application.port.in` |

Ohne Stereotyp: Heuristik (nur Wert-Felder → VO; ID-Feld + Verhalten → Aggregate).

## Attribut-Typen

```
+ fullName : String     →  String
+ email    : Email      →  Value Object Email
+ age      : int        →  int
+ createdAt: DateTime   →  java.time.Instant
+ status   : Status     →  enum Status
```

## Beziehungen

| Syntax | Semantik | Umsetzung |
|---|---|---|
| `A --> B` | Assoziation | Lookup-ID in A, kein Navigations-Property ins andere Aggregate |
| `A o-- B` | Aggregation | Nur wenn B eigenes Aggregate ist: per ID |
| `A *-- B` | Composition | B ist Teil von A (gleiches Aggregate) |
| `A "1" -- "*" B` | Kardinalität | 1:n → Liste in A; n:m → Zwischen-Aggregate |

## Kommentare als Invarianten

```
class Contact <<aggregate>> {
  ' invariant: email ist eindeutig pro tenant
  ' invariant: active erfordert email
  + activate()
  + deactivate()
}
```

Zeilen mit `' invariant:` werden zu Guards in den jeweiligen Methoden (bei Factory oder Transition).

## Use Cases aus Methoden

Methoden an Aggregate Roots mit Seiteneffekten werden Use Cases:

- `+ register(...) : ContactId` → `RegisterContactUseCase` mit `RegisterContactCommand`.
- `+ deactivate()` → `DeactivateContactUseCase` mit `DeactivateContactCommand(ContactId)`.

Read-Use-Cases kommen aus `<<query>>`-stereotypisierten Methoden oder werden für jedes Aggregate pauschal als `GetXxxByIdQuery`, `ListXxxQuery` erzeugt.
