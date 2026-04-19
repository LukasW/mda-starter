# ADR 0007 — Externes Archiv: generischer Adapter, Anbieter ausstehend

**Status:** akzeptiert (AE-02), OP-A offen
**Datum:** 2026-04-19

## Kontext
ZertES-konforme Langzeitarchivierung ist eine fachliche Anforderung (Art. 958f OR). Die Anbieter-Evaluation ist noch nicht abgeschlossen.

## Entscheidung
- Adapter-API (siehe Fachspec Kap. 7.1) als Minimalkontrakt: `POST /api/v1/archive/documents`, `GET /api/v1/archive/documents/{archivId}`.
- Fallback: Dokument bleibt intern gespeichert (`SpeicherTyp.INTERN`) solange `clm.archiv.extern.enabled=false`.
- Adapter-Implementierungen werden austauschbar als separate Klasse (Port-Out) implementiert.

## Konsequenzen
- Im Erstentwurf ist das externe Archiv nicht verdrahtet — die entsprechenden Feature-Flags sind default `false`.
- Sobald Anbieter gewaehlt, wird Kapitel 7.1 der Fachspec mit konkreter API-Spezifikation ergaenzt (OP-A).
