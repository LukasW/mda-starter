# ADR 0007 — Camunda 7 embedded nicht im MVP

- Status: accepted
- Datum: 2026-04-19

## Kontext

Die MDA-Spezifikation unterstuetzt optional Camunda 7 embedded fuer echte BPMN-Workflows mit External-Task-Workern. Die CLM-MVP-Spezifikation enthaelt jedoch keine BPMN-Dateien und keine mehrstufigen Genehmigungsketten (Out of Scope, Spec §1.3).

## Entscheid

Zero-Config-Prinzip: Da keine `.bpmn`-Datei als Input vorliegt, wird **keine** Camunda-Integration generiert. Der Starter bleibt schlank. Wenn in V1.1 mehrstufige Freigabeketten benoetigt werden, pruefen wir zuerst eine BPF-Erweiterung (ADR 0006); Camunda wird nur gezogen, wenn die Komplexitaet es rechtfertigt.

## Konsequenzen

- Kein Camunda-Runtime-Footprint (~20 MB, zusaetzliche DB-Tabellen, Admin-UI).
- Workflow-Artefakte muessten bei Einfuehrung nachtraeglich hinzugefuegt werden.
