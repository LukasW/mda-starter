# ADR 0004 — SLA 99.9% fuer CLM, Best-Effort fuer externe Systeme

**Status:** akzeptiert (AE-06)
**Datum:** 2026-04-19

## Kontext
Archiv, Signatur und Personenverwaltung sind Drittsysteme mit eigenen SLAs. Das CLM soll bei Ausfall dieser Systeme weiterhin funktionsfaehig bleiben.

## Entscheidung
- CLM-Backend Ziel-Verfuegbarkeit 99.9% (max. ca. 8h Downtime/Jahr).
- Jede Out-Bound-Integration erhaelt max. 5s Timeout und einen Retry-Mechanismus.
- Monitoring und Alerting fuer externe Systemverfuegbarkeit sind Pflicht (Ops).

## Konsequenzen
- Adapter kapseln Timeouts und Fallbacks. Dokument bleibt intern speicherbar, wenn Archiv nicht erreichbar ist.
- Wartungsfenster max. 4h/Monat, ausserhalb Geschaeftszeiten.
