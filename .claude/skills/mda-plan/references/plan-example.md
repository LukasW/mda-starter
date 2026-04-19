# Plan-Beispiel — auftrag-notiz (neutrale Demo-Domaene)

Dies ist ein Muster-Plan fuer ein `add-field`-Feature. Wird nicht rausgeschrieben; dient als Referenz. Die Domaene `Auftrag`/`order` ist bewusst generisch — ersetze sie in realen Plaenen durch die fachspezifischen Begriffe aus `specs/model/*.md`.

---

```markdown
# Feature Plan — auftrag-notiz

Bezogen auf: `specs/features/auftrag-notiz.md`
Bounded Context: order
Kind: add-field

## Uebersicht
Bearbeiter koennen beim Bestaetigen eine optionale Notiz (max. 2000 Zeichen) erfassen. Notiz ist Teil des Auftrags-Aggregats und read-only ab Status `VERSANDT`.

## Betroffene Schichten
- Domain: ja (`Auftrag.java`, evtl. VO `BestaetigungsNotiz.java`)
- Application: ja (`AuftragBestaetigenUseCase`, `AuftragApplicationService`)
- Adapter in (REST): ja (`AuftragResource`, Request-Record)
- Adapter out (Persistence): ja (JPA-Entity + Mapper)
- Frontend: ja (`auftrag-detail`)
- Migration: `V3__auftrag_bestaetigungsnotiz.sql` (ja)

## Zu erstellende Dateien
- [ ] `src/main/resources/db/migration/V3__auftrag_bestaetigungsnotiz.sql` — Flyway additiv
- [ ] `src/test/java/.../order/domain/AuftragNotizTest.java` — Unit auf Aggregate
- [ ] `src/test/resources/features/service/auftrag-notiz.feature` — BDD `@service`
- [ ] `src/test/java/.../bdd/service/AuftragNotizSteps.java`

## Zu aendernde Dateien
- [ ] `src/main/java/.../order/domain/Auftrag.java` — neues Feld + Accessor; **Wrap unter Marker**: ja
- [ ] `src/main/java/.../order/application/port/in/AuftragBestaetigenUseCase.java` — Command-Record erweitern; **Wrap unter Marker**: nein (neuer Record-Component)
- [ ] `src/main/java/.../order/adapter/in/rest/AuftragResource.java` — Request-Record erweitern; **Wrap unter Marker**: ja
- [ ] `src/main/java/.../order/adapter/out/persistence/AuftragJpaEntity.java` — neues Column-Feld; **Wrap unter Marker**: ja
- [ ] `src/main/webui/src/app/pages/auftrag-detail/auftrag-detail.html` — Textarea-Block + Bindung
- [ ] `src/main/webui/src/app/pages/auftrag-detail/auftrag-detail.ts` — Signal + FormControl

## BPF-Delta
keine

## Test-Strategie
- [ ] Unit: `AuftragTest` — 2 Szenarien (Notiz gesetzt / Notiz leer)
- [ ] Integration (`@QuarkusTest`): `AuftragResourceTest` — POST bestaetigen mit und ohne Notiz
- [ ] BDD `@service`: "Bearbeiter bestaetigt mit Notiz"
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
- `V<n+1>__undo_bestaetigungsnotiz.sql` mit `ALTER TABLE auftrag DROP COLUMN ...`.
- `Auftrag.java`, JPA-Entity, DTO, UI zurueckdrehen; alte Unit/BDD-Features loeschen.

## Offene Fragen
keine
```
