# ADR 0006 — Mandantenisolation ueber tenant_id + spaetere Schema-Trennung

**Status:** akzeptiert (AE-05)
**Datum:** 2026-04-19

## Kontext
Mehrere Gesellschaften sollen logisch getrennt arbeiten. Gleichzeitig ist eine vollstaendige Datenbanktrennung initial zu kostspielig.

## Entscheidung
- Alle Tabellen (inkl. `bpf_instance`, `bpf_transition_log`) tragen eine `tenant_id`.
- Application-Code trennt Mandanten ueber `tenant_id`-Filter.
- Spaetere Phase: Schema-per-Mandant oder vollstaendige DB-Trennung per ADR-Erweiterung, wenn Datenschutzanforderungen das verlangen.

## Konsequenzen
- Default-Tenant `00000000-0000-0000-0000-000000000001` im Erstentwurf fuer Entwicklungs- und Standalone-Demos.
- Ein spaeteres Feature fuehrt Mandantenauswahl (Subdomain-Routing oder Claim-Based) ein.
