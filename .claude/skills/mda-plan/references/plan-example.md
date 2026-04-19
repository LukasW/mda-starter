# Plan-Beispiel — vertrag-kommentar

Dies ist ein Muster-Plan fuer ein `add-field`-Feature. Wird nicht rausgeschrieben; dient als Referenz.

---

```markdown
# Feature Plan — vertrag-kommentar

Bezogen auf: `specs/features/vertrag-kommentar.md`
Bounded Context: contract
Kind: add-field

## Uebersicht
Antragsteller koennen beim Einreichen einen optionalen Kommentar (max. 2000 Zeichen) erfassen. Kommentar ist Teil des Vertrags-Aggregats und read-only ab Status `IN_PRUEFUNG`.

## Betroffene Schichten
- Domain: ja (`Vertrag.java`, evtl. VO `EinreichungsKommentar.java`)
- Application: ja (`VertragEinreichenUseCase`, `VertragApplicationService`)
- Adapter in (REST): ja (`VertragResource`, Request-Record)
- Adapter out (Persistence): ja (JPA-Entity + Mapper)
- Frontend: ja (`vertrag-detail`)
- Migration: `V3__vertrag_einreichungskommentar.sql` (ja)

## Zu erstellende Dateien
- [ ] `src/main/resources/db/migration/V3__vertrag_einreichungskommentar.sql` — Flyway additiv
- [ ] `src/test/java/.../contract/domain/VertragKommentarTest.java` — Unit auf Aggregate
- [ ] `src/test/resources/features/service/vertrag-kommentar.feature` — BDD `@service`
- [ ] `src/test/java/.../bdd/service/VertragKommentarSteps.java`

## Zu aendernde Dateien
- [ ] `src/main/java/.../contract/domain/Vertrag.java` — neues Feld + Accessor; **Wrap unter Marker**: ja
- [ ] `src/main/java/.../contract/application/port/in/VertragEinreichenUseCase.java` — Command-Record erweitern; **Wrap unter Marker**: nein (neuer Record-Component)
- [ ] `src/main/java/.../contract/adapter/in/rest/VertragResource.java` — Request-Record erweitern; **Wrap unter Marker**: ja
- [ ] `src/main/java/.../contract/adapter/out/persistence/VertragJpaEntity.java` — neues Column-Feld; **Wrap unter Marker**: ja
- [ ] `src/main/webui/src/app/pages/vertrag-detail/vertrag-detail.html` — Textarea-Block + Bindung
- [ ] `src/main/webui/src/app/pages/vertrag-detail/vertrag-detail.ts` — Signal + FormControl

## BPF-Delta
keine

## Test-Strategie
- [ ] Unit: `VertragTest` — 2 Szenarien (Kommentar gesetzt / Kommentar leer)
- [ ] Integration (`@QuarkusTest`): `VertragResourceTest` — POST einreichen mit und ohne Kommentar
- [ ] BDD `@service`: "Antragsteller reicht mit Kommentar ein"
- [ ] BDD `@process`: keine
- [ ] BDD `@ui`: keine

## Drift-Guard-Check
- [x] Flyway additiv — V3__
- [x] Keine Aggregate-Root-Public-API umbenannt/entfernt
- [x] Kein `permits`-Subtyp entfernt
- [x] Keine ArchUnit-Regel aufgeweicht
- [x] Kein `/api/v1/...`-Pfad umbenannt
- [x] Cross-BC: kein neuer Service-Call

## Rollback-Strategie
- `V<n+1>__undo_einreichungskommentar.sql` mit `ALTER TABLE vertrag DROP COLUMN ...`.
- `Vertrag.java`, JPA-Entity, DTO, UI zurueckdrehen; alte Unit/BDD-Features loeschen.

## Offene Fragen
keine
```
