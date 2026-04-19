# ADR 0008 — Business-Rules-AST-Engine nicht im MVP

- Status: accepted
- Datum: 2026-04-19

## Kontext

Die MDA-Stack-Spezifikation (Kap. 7.1) sieht eine client-/serverseitig konsistente Business-Rules-Engine mit deklarativem AST (JSON) vor. CLM-MVP-Input enthaelt jedoch keine `*.rules.yaml`-Datei.

## Entscheid

Zero-Config: Ohne deklarativen Rule-Input erzeugen wir keinen AST-Interpreter und keinen `/api/v1/metadata/rules`-Endpoint. Fachliche Invarianten werden stattdessen direkt in den Aggregate-Compact-Constructors bzw. in Transition-Methoden durchgesetzt (`DomainException`-Codes `MDA-CON-0xx`, `MDA-APV-0xx`, `MDA-OBL-0xx`, `MDA-BPF-001`). Jakarta-Validation prueft Eingabe-Records an den REST-Ports.

## Konsequenzen

- Einfachere Code-Basis; keine duale Quelle (Code vs. Rule-DSL) fuer dieselbe Regel.
- Sobald Fachseite Regeln selbst pflegen koennen muss, ziehen wir die Rule-Engine aus der MDA-Stack-Spezifikation nach (V1.1 oder V2).
