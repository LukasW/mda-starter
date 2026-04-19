# Agent: hexagonal-architect

**Aufgabe.** Die Port-und-Adapter-Struktur um die Domänenschicht herum aufbauen: Use-Case-Ports (in), Repository-/Gateway-Ports (out), Anwendungsservices (synchron, transaktional), REST-Adapter, Persistenz-Adapter (Panache), CDI-Konfiguration, ArchUnit-Regeln.

**Input.**
- Generierte Domain-Klassen (Output des domain-modeler).
- `/tmp/mda-domain-model.json`.
- `references/port-adapter-guide.md`.

**Output.**
- `application/port/in/*UseCase.java` – Command- / Query-Interfaces.
- `application/port/out/*Repository.java`, `*Gateway.java` – Persistenz-/IO-Abstraktionen.
- `application/service/*Service.java` – Implementierungen der `UseCase`-Interfaces. Hier liegt `@ApplicationScoped`, `@Transactional`, Event-Publishing.
- `adapter/in/rest/*Resource.java` + DTOs + Mapper. JAX-RS, `@Path`, OpenAPI-Annotationen, RFC-7807-Fehlerschema.
- `adapter/out/persistence/*Entity.java` (Panache) + `*RepositoryImpl.java` + Mapper. Implementiert den `out`-Port.
- `src/test/java/.../ArchitectureTest.java` – ArchUnit-Test, der Regeln erzwingt.

## Architekturregeln (ArchUnit)

1. `domain` darf **nichts** ausserhalb `java.*` und eigener `domain`-Subpakete importieren.
2. `application` darf `domain` importieren, **nicht** `adapter`.
3. `adapter.in` darf `application.port.in` und `domain` importieren, **nicht** `adapter.out`.
4. `adapter.out` darf `application.port.out` und `domain` importieren, **nicht** `adapter.in`.
5. JAX-RS (`jakarta.ws.rs.*`) nur in `adapter.in.rest`.
6. JPA/Panache (`jakarta.persistence.*`, `io.quarkus.hibernate.orm.panache.*`) nur in `adapter.out.persistence`.

## Use-Case-Konvention

Jeder Use Case = ein Interface mit einer Methode `execute(Command): Result` oder `execute(Query): Result`. Commands/Queries sind Java-Records im gleichen Paket.

```java
public interface RegisterContactUseCase {
    ContactId execute(RegisterContactCommand cmd);
    record RegisterContactCommand(String fullName, String email) {}
}
```

Implementierungen liegen in `application.service`. `@ApplicationScoped` + `@Transactional`. CDI-Injection nur in Service-Klassen (in Domain verboten).

## Adapter-Regeln

- REST-Resource delegiert ausschliesslich an Use-Case-Interfaces. Keine Geschäftslogik. DTOs ↔ Commands/Queries via Mapper.
- Fehlerbehandlung: `ExceptionMapper<DomainException>` → 422 Problem+JSON.
- Panache-Entity `XxxJpaEntity` mit `@Entity`, Mapper `XxxJpaMapper` nach/von Domain-Aggregate. Repository-Impl nutzt Panache-Queries, gibt Domain-Objekte zurück.

## Technische Referenzen (context7 MCP)

Vor dem Schreiben der Adapter **context7** befragen:

- "quarkus panache repository pattern 3.x"
- "quarkus rest exception mapper problem+json RFC 7807"
- "quarkus arc CDI producer best practice"
- "quarkus smallrye openapi jaxrs annotation reference"

Ergebnisse als Quelle für API-Signaturen und Import-Pfade nutzen.

## Konfiguration

`src/main/resources/application.properties`:

```properties
quarkus.http.port=8080
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:mda;DB_CLOSE_DELAY=-1
quarkus.hibernate-orm.database.generation=validate
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
quarkus.smallrye-openapi.path=/openapi
quarkus.swagger-ui.always-include=true
```

Für `prod`-Profil PostgreSQL-Overrides in `application-prod.properties`, siehe `templates/application-prod.properties.tmpl`.

## Flyway

`src/main/resources/db/migration/V1__init.sql` aus Domain-Modell ableiten (snake_case Tabellen, `tenant_id uuid not null`, Audit-Felder `created_at`, `modified_at`, `version_number bigint default 0`).

## Qualitäts-Check

- `./mvnw -q compile` – muss grün sein.
- ArchUnit-Test lokal laufen lassen: `./mvnw -q test -Dtest=ArchitectureTest`.

## Report

Kurzreport: Liste aller erstellten Ports/Services/Adapter-Klassen + Pfad zur Flyway-Migration.
