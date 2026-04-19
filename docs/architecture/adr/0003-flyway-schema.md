# ADR 0003 — Flyway-basiertes Schema-Management

- Status: accepted
- Datum: 2026-04-19

## Kontext

H2 im Dev-Modus, PostgreSQL in Prod. Hibernate auto-ddl fuehrt in Prod zu verbotenen Schema-Driftern. Compliance verlangt nachvollziehbare DB-Aenderungen.

## Entscheid

Flyway migriert beim Start (`quarkus.flyway.migrate-at-start=true`). Hibernate laeuft mit `schema-management.strategy=none`. Migrationen liegen unter `src/main/resources/db/migration/V*__*.sql`, H2- und PostgreSQL-kompatibel (keine DB-spezifischen Typen).

## Konsequenzen

- Schema-Aenderungen sind reviewbar und versionierbar.
- `dev`, `test`, `prod` nutzen identische Migrationen.
- DDL mit `GENERATED ALWAYS AS IDENTITY` o.ae. ist untersagt (H2/PG-Drift).
