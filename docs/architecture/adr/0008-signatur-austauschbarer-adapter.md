# ADR 0008 — Signatur-Adapter: austauschbar, Anbieter ausstehend

**Status:** akzeptiert (AE-03), OP-B offen
**Datum:** 2026-04-19

## Kontext
Verträge werden digital unterzeichnet. Kandidaten: Skribble (ZertES CH), DocuSign (eIDAS), Adobe Sign. Anbieter-Entscheid ist noch nicht gefallen.

## Entscheidung
- Signatur-Adapter kapselt Webhook-Format, Status-Mapping, Authentifizierung.
- Mindestanforderungen: qualifizierte elektronische Signatur (QES) gemaess ZertES/eIDAS; Schweizer Datenhaltung bevorzugt.
- Default-Adapter (`clm.signatur.anbieter=stub`) liefert eine Mock-Implementierung fuer Dev/Test.

## Konsequenzen
- Fachlicher BPF-Trigger `unterzeichnen` bleibt stabil; der Adapter liefert die tatsaechlichen Signaturdaten.
- Sobald Anbieter gewaehlt, implementiert ein neues Feature Adapter + konkrete Webhook-Spezifikation (OP-B).
