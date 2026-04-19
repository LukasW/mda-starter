# Fachliche Spezifikation – Contract Lifecycle Management (CLM)

**Dokument-ID:** CLM-SPEC-001
**Version:** 1.1
**Status:** Draft
**Erstellt:** 2026-04-19
**Autoren:** Architekt / Fachbereich

---

## 1. Zweck

Das CLM unterstützt den vollständigen Lebenszyklus von Verträgen — von der Erstellung über die rechtliche Prüfung und digitale Signatur bis zur revisionssicheren Archivierung. Das System ist eigenständig lauffähig; externe Systeme (Archiv, Personenverwaltung, Signatur) sind optionale Erweiterungen.

## 2. Bounded Contexts

| BC | Aggregate | Beschreibung |
|---|---|---|
| `contract` | `Vertrag` (mit `VertragsVersion`, `VertragsPartei`) | Verträge, Versionen, Parteien, Lifecycle |
| `person` | `Person` | Interne Personen + Cache von externer Personenverwaltung |

## 3. Akteure / Rollen

| Rolle | Kernfunktion |
|---|---|
| `SACHBEARBEITER` | Vertrag erstellen, Metadaten setzen, Prüfung einreichen |
| `RECHTSABTEILUNG` | Prüfen, Freigabe oder Korrekturbedarf |
| `GESCHAEFTSFUEHRUNG` | Digital unterzeichnen, Archivierung freigeben |
| `LESERECHT` | Lesezugriff auf Verträge |
| `ADMINISTRATOR` | Benutzer- und Systemadministration |

AuthN/Z: Hybrid OIDC + lokale Fallback-Accounts (AE-01). Im Erstentwurf nicht verdrahtet — ADR 0003.

## 4. Statusmodell (BPF — `contract`)

```
ENTWURF ──einreichen──▶ IN_PRUEFUNG ──freigeben──▶ FREIGEGEBEN ──zurSignaturSenden──▶ ZUR_SIGNATUR
                              │                                                            │
                              └─korrekturbeantragen─▶ KORREKTURBEDARF ──einreichen──▶ IN_PRUEFUNG
                                                                                            │
                                                                                  unterzeichnen
                                                                                            ▼
                                                                                    UNTERZEICHNET
                                                                                            │
                                                                                     archivieren
                                                                                            ▼
                                                                                     ARCHIVIERT
                                                                                    ┌──┴──┐
                                                                             ablaufen    kuendigen
                                                                                 ▼           ▼
                                                                            ABGELAUFEN   GEKUENDIGT
```

Ungueltige Transition → `DomainException("MDA-BPF-001")`, HTTP 422 Problem+JSON.

## 5. Domänenmodell (Quick Reference)

### Vertrag (contract)

Pflichtfelder: `titel` (≤ 200), `typ` (`VertragsTyp`), `erstellerId` (UUID). Optionale Gültigkeit (`gueltigVon`, `gueltigBis`). Stage ist BPF-authoritativ.

Invarianten:
- mindestens eine `VertragsVersion`, sobald ein Dokument hochgeladen wird.
- `parteien` sind Liste von `(rolle, personId)` — Person-Referenz per UUID (Cross-BC nur per ID).
- Nach `ARCHIVIERT` oder finalen Stages nicht mehr editierbar.

### VertragsTyp

`LIEFERANTENVERTRAG | KUNDENVERTRAG | ARBEITSVERTRAG | KOOPERATIONSVERTRAG | SONSTIGES`

### DokumentReferenz

`speicherTyp` (`INTERN | ARCHIV_EXTERN`) + `pfadLokal` (Pflicht bei intern) + `archivExternId` (Pflicht bei extern) + `mimeType` + `groesseByte` + `inhaltHash`.

### Person (person)

Pflichtfelder: `vorname`, `nachname`, `email` (Regex-validiert, normalisiert). Optional: `organisation`, `funktion`. `quelleTyp` ∈ {INTERN, EXTERN_API}; bei EXTERN_API ist `externeId` Pflicht.

## 6. REST-API (Auszug)

Basis: `/api/v1/`. Fehler: RFC 7807 `application/json` mit Feldern `code`, `status`, `title`, `detail`, `errors[]`.

### Vertrag

| Methode | Pfad | Zweck |
|---|---|---|
| POST | `/api/v1/vertraege` | Vertrag erstellen (→ 201 + Location) |
| GET | `/api/v1/vertraege` | Liste (`$top`, `$skip`, `tenantId`) |
| GET | `/api/v1/vertraege/{id}` | Detail |
| PUT | `/api/v1/vertraege/{id}/metadaten` | Titel + Gültigkeit setzen |
| POST | `/api/v1/vertraege/{id}/dokument` | Version + Dokumentreferenz anhängen |
| POST | `/api/v1/vertraege/{id}/parteien` | Person mit Rolle zuordnen |
| POST | `/api/v1/vertraege/{id}/process/contract/trigger/{trigger}` | BPF-Trigger (`einreichen`, `freigeben`, …) |

### Person

| Methode | Pfad | Zweck |
|---|---|---|
| POST | `/api/v1/personen` | Interne Person erfassen |
| GET | `/api/v1/personen?query=…&limit=…` | Suche (lokal + optional extern) |
| GET | `/api/v1/personen/{id}` | Detail |

## 7. Externe Systeme (optional)

| System | Feature-Flag | Fallback |
|---|---|---|
| Archiv | `clm.archiv.extern.enabled` | Dokument bleibt intern (`SpeicherTyp.INTERN`) |
| Personenverwaltung | `clm.person.externe-verwaltung.enabled` | Lokale Personen; externe Treffer als `EXTERN_API`-Snapshot gecacht |
| Signatur | `clm.signatur.anbieter=stub` | Keine qualifizierte Signatur im Erstentwurf |

Im Erstentwurf sind diese Ports definiert, aber nur Stub-Implementierungen verdrahtet. Konkrete Anbieter werden per `/mda-plan` → `/mda-implement`-Zyklus ergänzt (OP-A, OP-B).

## 8. Architekturentscheide (Kurzfassung)

- **AE-01 AuthN:** Hybrid OIDC + lokale Accounts (ADR 0003).
- **AE-02 Archiv:** Generischer Adapter (ADR 0007).
- **AE-03 Signatur:** Austauschbarer Adapter (ADR 0008).
- **AE-04 i18n:** Architektur DE-only im Release 1 (ADR 0009).
- **AE-05 Mandantenfähigkeit:** Schema-isolation via `tenant_id` + später Schema pro Mandant (ADR 0006).
- **AE-06 SLA:** 99.9% Backend; externe Systeme Best-Effort mit 5s Timeout (ADR 0004).
- **AE-07 DSFA:** nicht erforderlich — formale Begründung durch DSB vor Go-Live (Pendenz OP-C).

## 9. Compliance

- Aufbewahrungsfrist 10 Jahre (Schweizer OR Art. 958f) für archivierte Verträge.
- Audit-Events werden als Domain-Events publiziert (Outbox-Pattern später).

## 10. Offene Punkte

| ID | Thema | Zuständig | ADR |
|---|---|---|---|
| OP-A | Archiv-Anbieter evaluieren | Einkauf / Architektur | AE-02 / ADR 0007 |
| OP-B | Signatur-Anbieter evaluieren | Einkauf / IT | AE-03 / ADR 0008 |
| OP-C | DSFA-Begründung formal dokumentieren | Datenschutzbeauftragter | AE-07 |
